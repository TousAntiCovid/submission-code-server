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
import fr.gouv.stopc.submissioncode.service.model.CodeType
import fr.gouv.stopc.submissioncode.service.model.CodeType.JWT
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
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
    private val jwtRepository: JwtRepository,
    private val metricsService: MetricsService
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

    fun validateAndUse(code: String) = when (CodeType.ofCode(code)) {
        JWT -> validateJwt(code)
        else -> validateCode(code)
    }

    private fun validateCode(code: String): Boolean {
        val now = Instant.now()
        val updatedEntities = submissionCodeRepository.verifyAndUse(code, now)
        val codeType = CodeType.ofCode(code)
        val valid = updatedEntities == 1
        metricsService.countCodeUsed(codeType, valid)
        return valid
    }

    private fun validateJwt(jwt: String): Boolean {

        val signedJwt = try {
            SignedJWT.parse(jwt)
        } catch (e: ParseException) {
            metricsService.countCodeUsed(JWT, false)
            return false
        }

        val jwtClaims = try {
            signedJwt.jwtClaimsSet
        } catch (e: ParseException) {
            metricsService.countCodeUsed(JWT, false)
            return false
        }

        val jwtKid = signedJwt.header.keyID

        if (jwtKid.isNullOrBlank() ||
            jwtClaims.jwtid.isNullOrBlank() ||
            jwtClaims.issueTime == null ||
            submissionProperties.jwtPublicKeys[jwtKid].isNullOrBlank() ||
            !signedJwt.verify(jwtSignatureVerifiers[jwtKid])
        ) {
            metricsService.countCodeUsed(JWT, false)
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
                metricsService.countCodeUsed(JWT, true)
            } catch (e: DataIntegrityViolationException) {
                metricsService.countCodeUsed(JWT, false)
                return false
            }
        }

        return isValid
    }
}
