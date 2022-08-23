package fr.gouv.stopc.submissioncode.service

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jwt.SignedJWT
import fr.gouv.stopc.submissioncode.configuration.SubmissionProperties
import fr.gouv.stopc.submissioncode.repository.JwtRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.text.ParseException
import java.time.Instant

@Service
@Transactional
class SubmissionCodeService(
    private val submissionProperties: SubmissionProperties,
    private val jwtSignatureVerifiers: Map<String, JWSVerifier>,
    private val jwtRepository: JwtRepository,
    private val metricsService: MetricsService
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun validateJwt(jwt: String): Boolean {

        val signedJwt = try {
            SignedJWT.parse(jwt)
        } catch (e: Exception) {
            metricsService.countCodeUsed(false)
            log.info("JWT could not be parsed: ${e.message ?: e::class.simpleName}, $jwt")
            return false
        }

        val claims = try {
            signedJwt.jwtClaimsSet
        } catch (e: ParseException) {
            metricsService.countCodeUsed(false)
            log.info("JWT claims set could not be parsed: ${e.message}, $jwt")
            return false
        }

        val jti = claims.jwtid
        val iat = claims.issueTime?.toInstant()
        val exp = iat?.plus(submissionProperties.jwtCodeLifetime)
        val now = Instant.now()

        if (jti.isNullOrBlank() || iat == null || exp == null) {
            metricsService.countCodeUsed(false)
            log.info("JWT is missing claim jti ($jti) or iat ($iat): $jwt")
            return false
        }

        if (iat.isAfter(now)) {
            metricsService.countCodeUsed(false)
            log.info("JWT is issued at a future time ($iat): $jwt")
            return false
        }

        if (exp.isBefore(now)) {
            metricsService.countCodeUsed(false)
            log.info("JWT has expired, it was issued at $iat, so it expired on $exp: $jwt")
            return false
        }

        val kid = signedJwt.header.keyID
        if (!submissionProperties.jwtPublicKeys.containsKey(kid)) {
            metricsService.countCodeUsed(false)
            log.info("No public key found in configuration for kid '$kid': $jwt")
            return false
        }

        try {
            if (!signedJwt.verify(jwtSignatureVerifiers[kid])) {
                metricsService.countCodeUsed(false)
                log.info("JWT signature is invalid: $jwt")
                return false
            }
        } catch (e: JOSEException) {
            metricsService.countCodeUsed(false)
            log.info("JWT signature can't be verified: ${e.message}, $jwt")
            return false
        }

        return if (1 == jwtRepository.saveUsedJti(jti, now)) {
            metricsService.countCodeUsed(true)
            true
        } else {
            metricsService.countCodeUsed(false)
            log.info("JWT with jti '$jti' has already been used: $jwt")
            false
        }
    }
}
