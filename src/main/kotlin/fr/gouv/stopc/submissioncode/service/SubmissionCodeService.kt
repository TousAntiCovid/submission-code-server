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
import org.slf4j.LoggerFactory
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

    private val log = LoggerFactory.getLogger(this::class.java)

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
        val codeType = CodeType.ofCode(code)

        if (!CodeType.matchPattern(code)) {
            metricsService.countCodeUsed(codeType, false)
            log.info("Code $code ($codeType) does not match the expected pattern")
            return false
        }

        val submissionCode = submissionCodeRepository.findByCode(code)

        if (submissionCode == null) {
            metricsService.countCodeUsed(codeType, false)
            log.info("Code $code ($codeType) was not found.")
            return false
        }
        if (submissionCode.used || submissionCode.dateUse != null) {
            metricsService.countCodeUsed(codeType, false)
            log.info("Code $code ($codeType) has already been used.")
            return false
        }
        if (submissionCode.dateAvailable.isAfter(now)) {
            metricsService.countCodeUsed(codeType, false)
            log.info("Code $code ($codeType) being used before validity period start: ${submissionCode.dateAvailable}")
            return false
        }
        if (submissionCode.dateEndValidity.isBefore(now)) {
            metricsService.countCodeUsed(codeType, false)
            log.info("Code $code ($codeType) being used after validity period end: ${submissionCode.dateAvailable}")
            return false
        }

        return try {
            submissionCodeRepository.save(
                submissionCode.copy(
                    used = true,
                    dateUse = now
                )
            )
            metricsService.countCodeUsed(codeType, true)
            true
        } catch (e: DataIntegrityViolationException) {
            metricsService.countCodeUsed(JWT, false)
            log.info("Code $code ($codeType) has already been used.")
            false
        }
    }

    private fun validateJwt(jwt: String): Boolean {

        if (!CodeType.matchPattern(jwt)) {
            metricsService.countCodeUsed(JWT, false)
            log.info("The JWT: $jwt does not match the expected pattern")
            return false
        }

        val signedJwt = try {
            SignedJWT.parse(jwt)
        } catch (e: ParseException) {
            metricsService.countCodeUsed(JWT, false)
            log.info("The JWT: $jwt could not be parsed")
            return false
        }

        val jwtClaims = try {
            signedJwt.jwtClaimsSet
        } catch (e: ParseException) {
            metricsService.countCodeUsed(JWT, false)
            log.info("The claim set of JWT: $jwt could not be parsed")
            return false
        }

        val jwtKid = signedJwt.header.keyID

        if (jwtKid.isNullOrBlank() ||
            jwtClaims.jwtid.isNullOrBlank() ||
            jwtClaims.issueTime == null
        ) {
            metricsService.countCodeUsed(JWT, false)
            log.info("The kid, jti or iat field is missing for  JWT: $jwt")
            return false
        }

        if (submissionProperties.jwtPublicKeys[jwtKid].isNullOrBlank()) {
            metricsService.countCodeUsed(JWT, false)
            log.info("No public key associated to the kid of JWT: $jwt was found")
            return false
        }

        if (!signedJwt.verify(jwtSignatureVerifiers[jwtKid])) {
            metricsService.countCodeUsed(JWT, false)
            log.info("Could not verify the signature of JWT: $jwt")
            return false
        }

        val iat = jwtClaims.issueTime.toInstant()
        val exp = iat.plus(submissionProperties.jwtCodeLifetime)
        val jti = jwtClaims.jwtid
        val now = Instant.now()

        if (iat.isAfter(now)) {
            metricsService.countCodeUsed(JWT, false)
            log.info("The JWT: $jwt is issued in the future, issued at: $iat")
            return false
        }

        if (exp.isBefore(now)) {
            metricsService.countCodeUsed(JWT, false)
            log.info("The JWT: $jwt is expired, issued at: $iat and expiration date: $exp")
            return false
        }

        if (jwtRepository.existsByJti(jti)) {
            metricsService.countCodeUsed(JWT, false)
            log.info("The jti of JWT: $jwt has already been used")
            return false
        }

        return try {
            jwtRepository.save(JwtUsed(jti = jti, dateUse = now))
            metricsService.countCodeUsed(JWT, true)
            true
        } catch (e: DataIntegrityViolationException) {
            metricsService.countCodeUsed(JWT, false)
            log.info("The jti of JWT: $jwt has already been used")
            false
        }
    }
}
