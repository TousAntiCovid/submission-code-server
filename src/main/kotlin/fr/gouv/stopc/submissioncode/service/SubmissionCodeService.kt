package fr.gouv.stopc.submissioncode.service

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jwt.SignedJWT
import fr.gouv.stopc.submissioncode.configuration.SubmissionProperties
import fr.gouv.stopc.submissioncode.repository.JwtRepository
import fr.gouv.stopc.submissioncode.repository.SubmissionCodeRepository
import fr.gouv.stopc.submissioncode.repository.model.SubmissionCode
import fr.gouv.stopc.submissioncode.service.model.CodeType
import fr.gouv.stopc.submissioncode.service.model.CodeType.JWT
import fr.gouv.stopc.submissioncode.service.model.CodeType.LONG
import fr.gouv.stopc.submissioncode.service.model.CodeType.SHORT
import fr.gouv.stopc.submissioncode.service.model.CodeType.TEST
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

    fun validateAndUse(code: String): Boolean = when (val codeType = CodeType.ofCode(code)) {
        LONG, SHORT, TEST -> validateCode(codeType, code)
        JWT -> validateJwt(code)
        else -> {
            log.info("Code $code does not match any code type and can't be validated")
            false
        }
    }

    private fun validateCode(codeType: CodeType, code: String): Boolean {

        val now = Instant.now()
        val submissionCode = submissionCodeRepository.findByCode(code)

        if (submissionCode == null) {
            metricsService.countCodeUsed(codeType, false)
            log.info("Code $code doesn't exist")
            return false
        }
        if (submissionCode.type != codeType.databaseRepresentation?.dbValue) {
            metricsService.countCodeUsed(codeType, false)
            log.info("'$code' seems to be a $codeType code but database knows it as a ${submissionCode.getType()} code")
            return false
        }
        if (submissionCode.used || submissionCode.dateUse != null) {
            metricsService.countCodeUsed(codeType, false)
            log.info("$codeType code '$code' has already been used")
            return false
        }
        if (submissionCode.dateAvailable.isAfter(now)) {
            metricsService.countCodeUsed(codeType, false)
            log.info("$codeType code '$code' is not yet available (availability date is ${submissionCode.dateAvailable})")
            return false
        }
        if (submissionCode.dateEndValidity.isBefore(now)) {
            metricsService.countCodeUsed(codeType, false)
            log.info("$codeType code '$code' has expired on ${submissionCode.dateEndValidity}")
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

        val signedJwt = try {
            SignedJWT.parse(jwt)
        } catch (e: Exception) {
            metricsService.countCodeUsed(JWT, false)
            log.info("JWT could not be parsed: ${e.message ?: e::class.simpleName}, $jwt")
            return false
        }

        val claims = try {
            signedJwt.jwtClaimsSet
        } catch (e: ParseException) {
            metricsService.countCodeUsed(JWT, false)
            log.info("JWT claims set could not be parsed: ${e.message}, $jwt")
            return false
        }

        val jti = claims.jwtid
        val iat = claims.issueTime?.toInstant()
        val exp = iat?.plus(submissionProperties.jwtCodeLifetime)
        val now = Instant.now()

        if (jti.isNullOrBlank() || iat == null || exp == null) {
            metricsService.countCodeUsed(JWT, false)
            log.info("JWT is missing claim jti ($jti) or iat ($iat): $jwt")
            return false
        }

        if (iat.isAfter(now)) {
            metricsService.countCodeUsed(JWT, false)
            log.info("JWT is issued at a future time ($iat): $jwt")
            return false
        }

        if (exp.isBefore(now)) {
            metricsService.countCodeUsed(JWT, false)
            log.info("JWT has expired, it was issued at $iat, so it expired on $exp: $jwt")
            return false
        }

        val kid = signedJwt.header.keyID
        if (!submissionProperties.jwtPublicKeys.containsKey(kid)) {
            metricsService.countCodeUsed(JWT, false)
            log.info("No public key found in configuration for kid '$kid': $jwt")
            return false
        }

        try {
            if (!signedJwt.verify(jwtSignatureVerifiers[kid])) {
                metricsService.countCodeUsed(JWT, false)
                log.info("JWT signature is invalid: $jwt")
                return false
            }
        } catch (e: JOSEException) {
            metricsService.countCodeUsed(JWT, false)
            log.info("JWT signature can't be verified: ${e.message}, $jwt")
            return false
        }

        return if (1 == jwtRepository.saveUsedJti(jti, now)) {
            metricsService.countCodeUsed(JWT, true)
            true
        } else {
            metricsService.countCodeUsed(JWT, false)
            log.info("JWT with jti '$jti' has already been used: $jwt")
            false
        }
    }
}
