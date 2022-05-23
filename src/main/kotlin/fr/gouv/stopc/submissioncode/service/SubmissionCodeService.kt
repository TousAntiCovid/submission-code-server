package fr.gouv.stopc.submissioncode.service

import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jwt.SignedJWT
import fr.gouv.stopc.submissioncode.configuration.SubmissionJWTConfiguration
import fr.gouv.stopc.submissioncode.repository.SubmissionCodeJWTRepository
import fr.gouv.stopc.submissioncode.repository.SubmissionCodeRepository
import fr.gouv.stopc.submissioncode.repository.model.JWT
import fr.gouv.stopc.submissioncode.repository.model.SubmissionCode
import org.apache.commons.text.RandomStringGenerator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.time.temporal.ChronoUnit
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

        if (code.length == 6 || code.length == 12 || code.length == 38) {
            return validateCode(code)
        } else {
            return validateJwt(code)
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

        val signedJwt = SignedJWT.parse(jwt)
        val signedJwtPayload = signedJwt.payload.toJSONObject()

        val jwtJti = signedJwtPayload["jti"].toString()
        val jwtDate = signedJwtPayload["iat"].toString()

        val jwtInstant = Instant.ofEpochSecond(jwtDate.toLong())

        val now = Instant.now()

        val isValid = jwtInstant.isBefore(now) &&
            jwtInstant.plus(7, ChronoUnit.DAYS).isAfter(now) &&
            submissionCodeJWTRepository.findByJti(jwtJti) == null &&
            !(signedJwt.header.keyID.isNullOrEmpty() || submissionJWTConfiguration.publicKeys[signedJwt.header.keyID].isNullOrEmpty()) &&
            verifySignature(signedJwt)

        if (isValid) {
            submissionCodeJWTRepository.save(
                JWT(
                    jti = jwtJti
                )
            )
        }

        return isValid
    }

    private fun verifySignature(signedJwt: SignedJWT): Boolean {

        val encodedPublicKey = submissionJWTConfiguration.publicKeys[signedJwt.header.keyID] ?: return false
        val decodedPublicKey = Base64.getMimeDecoder().decode(encodedPublicKey)

        val publicKey = KeyFactory.getInstance("EC")
            .generatePublic(X509EncodedKeySpec(decodedPublicKey)) as java.security.interfaces.ECPublicKey

        val key = ECKey.Builder(Curve.P_256, publicKey).build()
        val verifier = ECDSAVerifier(key)

        return signedJwt.verify(verifier)
    }
}
