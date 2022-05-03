package fr.gouv.stopc.submissioncode.service

import fr.gouv.stopc.submissioncode.repository.SubmissionCodeRepository
import fr.gouv.stopc.submissioncode.repository.model.SubmissionCode
import org.apache.commons.text.RandomStringGenerator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.HOURS

@Service
@Transactional
class SubmissionCodeService(
    private val submissionCodeRepository: SubmissionCodeRepository,
    private val randomUppercaseString: RandomStringGenerator
) {

    fun generateShortCode(): SubmissionCode {
        for (i in 0..10) {
            val now = Instant.now()
            val code = randomUppercaseString.generate(6)
            return submissionCodeRepository.save(
                SubmissionCode(
                    code = code,
                    dateGeneration = now,
                    dateAvailable = now,
                    dateEndValidity = now.plus(1, HOURS),
                    type = SubmissionCode.Type.SHORT.dbValue,
                    used = false
                )
            )
        }
        throw IllegalStateException("Can't generate a non existing short code after 10 random generations")
    }

    fun generateTestCode(): SubmissionCode {
        for (i in 0..10) {
            val now = Instant.now()
            val code = randomUppercaseString.generate(12)
            return submissionCodeRepository.save(
                SubmissionCode(
                    code = code,
                    dateGeneration = now,
                    dateAvailable = now,
                    dateEndValidity = now.plus(15, DAYS).truncatedTo(DAYS),
                    type = SubmissionCode.Type.TEST.dbValue,
                    used = false
                )
            )
        }
        throw IllegalStateException("Can't generate a non existing test code after 10 random generations")
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
