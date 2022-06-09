package fr.gouv.stopc.submissioncode.service

import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jwt.SignedJWT
import fr.gouv.stopc.submissioncode.configuration.SubmissionProperties
import fr.gouv.stopc.submissioncode.repository.JwtRepository
import fr.gouv.stopc.submissioncode.repository.SubmissionCodeRepository
import fr.gouv.stopc.submissioncode.repository.model.JwtUsed
import fr.gouv.stopc.submissioncode.repository.model.SubmissionCode
import fr.gouv.stopc.submissioncode.repository.model.SubmissionCode.Type.SHORT
import fr.gouv.stopc.submissioncode.repository.model.SubmissionCode.Type.TEST
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.text.ParseException
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

@Service
@Transactional
class SubmissionCodeService(
    private val random: RandomGenerator,
    private val submissionProperties: SubmissionProperties,
    private val jwtSignatureVerifiers: Map<String, JWSVerifier>,
    private val submissionCodeRepository: SubmissionCodeRepository,
    private val jwtRepository: JwtRepository
) {

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
                type = SHORT.dbValue,
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
                type = TEST.dbValue,
                used = false
            )
        )
    }

    @Recover
    fun throwErrorTooManyAttempts(e: DataIntegrityViolationException): SubmissionCode {
        throw RuntimeException("No unique code could be generated after 10 random attempts")
    }

    fun validateAndUse(code: String): Boolean {

        return if (code.length == 6 || code.length == 12 || code.length == 36) {
            validateCode(code)
        } else {
            validateJwt(code)
        }
    }

    private fun validateCode(code: String): Boolean {
        val now = Instant.now()
        val updatedEntities = submissionCodeRepository.verifyAndUse(code, now)
        return updatedEntities == 1
    }

    private fun validateJwt(jwt: String): Boolean {

        val signedJwt = try {
            SignedJWT.parse(jwt)
        } catch (e: ParseException) {
            return false
        }

        val jwtClaims = try {
            signedJwt.jwtClaimsSet
        } catch (e: ParseException) {
            return false
        }

        val jwtKid = signedJwt.header.keyID

        if (jwtKid.isNullOrBlank() ||
            jwtClaims.jwtid.isNullOrBlank() ||
            jwtClaims.issueTime == null ||
            submissionProperties.jwtPublicKeys[jwtKid].isNullOrBlank() ||
            !signedJwt.verify(jwtSignatureVerifiers[jwtKid])
        ) {
            return false
        }

        val iat = jwtClaims.issueTime.toInstant()
        val exp = iat.plus(submissionProperties.jwtCodeLifetime)
        val jti = jwtClaims.jwtid
        val now = Instant.now()

        val isValid = now in iat..exp && !jwtRepository.existsByJti(jti)

        if (isValid) {
            try {
                jwtRepository.save(JwtUsed(jti = jti, dateUse = now))
            } catch (e: DataIntegrityViolationException) {
                return false
            }
        }

        return isValid
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun purgeOldJwtUsed() {
        jwtRepository.deleteJwtUsedByDateUse(Instant.now().plus(submissionProperties.jwtRetentionTime))
    }
}
