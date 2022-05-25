package fr.gouv.stopc.submissioncode.service

import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jwt.SignedJWT
import fr.gouv.stopc.submissioncode.configuration.SubmissionJWTConfiguration
import fr.gouv.stopc.submissioncode.repository.SubmissionCodeJWTRepository
import fr.gouv.stopc.submissioncode.repository.SubmissionCodeRepository
import fr.gouv.stopc.submissioncode.repository.model.JtiUsed
import fr.gouv.stopc.submissioncode.repository.model.SubmissionCode
import org.apache.commons.text.RandomStringGenerator
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.text.ParseException
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.HOURS
import java.util.Base64

@Service
@Transactional
class SubmissionCodeService(
    private val submissionCodeRepository: SubmissionCodeRepository,
    private val randomUppercaseString: RandomStringGenerator,
    private val submissionCodeJWTRepository: SubmissionCodeJWTRepository,
    private val submissionJWTConfiguration: SubmissionJWTConfiguration
) {

    private val signatureVerifiers: Map<String, ECDSAVerifier> =
        submissionJWTConfiguration.publicKeys.mapValues { generateVerifier(it.value) }

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

        return if (code.length == 6 || code.length == 12 || code.length == 36) {
            validateCode(code)
        } else {
            validateJwt(code)
        }
    }

    private fun validateCode(code: String): Boolean {
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

    private fun validateJwt(jwt: String): Boolean {

        val signedJwt = try {
            SignedJWT.parse(jwt)
        } catch (e: ParseException) {
            return false
        }

        val jwtKid = signedJwt.header.keyID.ifBlank { return false }

        if (jwtKid.isNullOrBlank() ||
            submissionJWTConfiguration.publicKeys[jwtKid].isNullOrBlank() ||
            !signedJwt.verify(signatureVerifiers[jwtKid])
        ) {
            return false
        }

        val jwtIatAsInstant = try {
            signedJwt.jwtClaimsSet.issueTime.toInstant()
        } catch (e: ParseException) {
            return false
        }
        val jwtJti = if (signedJwt.jwtClaimsSet.jwtid.isNullOrBlank()) return false else signedJwt.jwtClaimsSet.jwtid

        val now = Instant.now()

        val isValid = jwtIatAsInstant.isBefore(now) &&
            jwtIatAsInstant.plus(7, DAYS).isAfter(now) &&
            !submissionCodeJWTRepository.existsByJti(jwtJti)

        if (isValid) {
            try {
                submissionCodeJWTRepository.save(JtiUsed(jti = jwtJti))
            } catch (e: DataIntegrityViolationException) {
                return false
            }
        }

        return isValid
    }

    private fun generateVerifier(publicKey: String): ECDSAVerifier {
        val decodedPublicKey = Base64.getMimeDecoder().decode(publicKey)

        val ecPublicKey = KeyFactory.getInstance("EC")
            .generatePublic(X509EncodedKeySpec(decodedPublicKey)) as java.security.interfaces.ECPublicKey

        val ecKey = ECKey.Builder(Curve.P_256, ecPublicKey).build()
        return ECDSAVerifier(ecKey)
    }
}
