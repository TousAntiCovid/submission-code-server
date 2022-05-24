package fr.gouv.stopc.submissioncode.service

import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jwt.SignedJWT
import fr.gouv.stopc.submissioncode.configuration.SubmissionJWTConfiguration
import fr.gouv.stopc.submissioncode.configuration.SubmissionProperties
import fr.gouv.stopc.submissioncode.repository.SubmissionCodeJWTRepository
import fr.gouv.stopc.submissioncode.repository.SubmissionCodeRepository
import fr.gouv.stopc.submissioncode.repository.model.JWT
import fr.gouv.stopc.submissioncode.repository.model.SubmissionCode
import fr.gouv.stopc.submissioncode.repository.model.SubmissionCode.Type.SHORT
import fr.gouv.stopc.submissioncode.repository.model.SubmissionCode.Type.TEST
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.text.ParseException
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS
import java.util.Base64

@Service
@Transactional
class SubmissionCodeService(
    private val submissionCodeRepository: SubmissionCodeRepository,
    private val randomUppercaseString: RandomStringGenerator,
    private val submissionCodeJWTRepository: SubmissionCodeJWTRepository,
    private val submissionJWTConfiguration: SubmissionJWTConfiguration,
    private val submissionProperties: SubmissionProperties
) {

    private val signatureVerifiers: MutableMap<String, ECDSAVerifier> = mutableMapOf()

    init {
        submissionJWTConfiguration.publicKeys.forEach { (kid, key) ->
            val decodedPublicKey = Base64.getMimeDecoder().decode(key)

            val publicKey = KeyFactory.getInstance("EC")
                .generatePublic(X509EncodedKeySpec(decodedPublicKey)) as java.security.interfaces.ECPublicKey

            val key = ECKey.Builder(Curve.P_256, publicKey).build()
            val verifier = ECDSAVerifier(key)
            signatureVerifiers[kid] = verifier
        }
    }

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

        val jwtJti = signedJwt.jwtClaimsSet.jwtid
        val jwtIatAsInstant = signedJwt.jwtClaimsSet.issueTime.toInstant()

        val now = Instant.now()

        val isValid = jwtIatAsInstant.isBefore(now) &&
            jwtIatAsInstant.plus(7, DAYS).isAfter(now) &&
            !submissionCodeJWTRepository.existsByJti(jwtJti) &&
            !(signedJwt.header.keyID.isNullOrEmpty() || submissionJWTConfiguration.publicKeys[signedJwt.header.keyID].isNullOrEmpty()) &&
            signedJwt.verify(signatureVerifiers[signedJwt.header.keyID])

        if (isValid) {
            submissionCodeJWTRepository.save(
                JWT(
                    jti = jwtJti
                )
            )
        }

        return isValid
    }
}
