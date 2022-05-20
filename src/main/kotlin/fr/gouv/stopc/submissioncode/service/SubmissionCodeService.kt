package fr.gouv.stopc.submissioncode.service

import fr.gouv.stopc.submissioncode.configuration.SubmissionProperties
import fr.gouv.stopc.submissioncode.repository.SubmissionCodeRepository
import fr.gouv.stopc.submissioncode.repository.model.SubmissionCode
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

@Service
@Transactional
class SubmissionCodeService(
    private val submissionCodeRepository: SubmissionCodeRepository,
    private val random: RandomGenerator,
    private val submissionProperties: SubmissionProperties
) {
    private val log = LoggerFactory.getLogger(SubmissionCodeService::class.java)

    @Retryable(
        value = [DataIntegrityViolationException::class],
        maxAttempts = 10,
        backoff = Backoff(delay = 0),
        recover = "throwErrorTooManyAttempts"
    )
    fun generateShortCode(): SubmissionCode {
        val now = Instant.now()
        val code = random.upperCaseString(6)
        return submissionCodeRepository.save(
            SubmissionCode(
                code = code,
                dateGeneration = now,
                dateAvailable = now,
                dateEndValidity = now.plus(submissionProperties.shortCodeLifetime),
                type = SubmissionCode.Type.SHORT.dbValue,
                used = false
            )
        )
    }

    @Retryable(
        value = [DataIntegrityViolationException::class],
        maxAttempts = 10,
        backoff = Backoff(delay = 0),
        recover = "throwErrorTooManyAttempts"
    )
    fun generateTestCode(): SubmissionCode {
        val now = Instant.now()
        val code = random.upperCaseString(12)
        return submissionCodeRepository.save(
            SubmissionCode(
                code = code,
                dateGeneration = now,
                dateAvailable = now,
                dateEndValidity = now.plus(submissionProperties.testCodeLifetime).truncatedTo(DAYS),
                type = SubmissionCode.Type.TEST.dbValue,
                used = false
            )
        )
    }

    @Recover
    fun throwErrorTooManyAttempts(e: DataIntegrityViolationException): SubmissionCode {
        throw RuntimeException("No unique code could be generated after 10 random attempts")
    }

    fun validateAndUse(code: String): Boolean {
        val now = Instant.now()
        val unusedCode = submissionCodeRepository.findUnusedActiveCode(code, now) ?: return false
        submissionCodeRepository.save(
            unusedCode.copy(
                used = true,
                dateUse = now
            )
        )
        return true
    }
}
