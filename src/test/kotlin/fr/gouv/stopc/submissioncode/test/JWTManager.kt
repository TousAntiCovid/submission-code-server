package fr.gouv.stopc.submissioncode.test

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import groovy.util.logging.Slf4j
import org.springframework.test.context.TestExecutionListener
import java.security.NoSuchAlgorithmException
import java.security.interfaces.ECPrivateKey
import java.time.Instant
import java.util.Base64
import java.util.UUID

@Slf4j
class JWTManager : TestExecutionListener {

    companion object {

        private val JWT_KEY_PAIR: ECKey

        init {
            try {
                JWT_KEY_PAIR = ECKeyGenerator(Curve.P_256)
                    .keyID("TousAntiCovidKID")
                    .generate()
                val tacPublicKey = Base64.getEncoder()
                    .encodeToString(JWT_KEY_PAIR.toECPublicKey().encoded)

                val anotherEcKey = ECKeyGenerator(Curve.P_256)
                    .keyID("AnotherKID")
                    .generate()
                val anotherPublicKey = Base64.getEncoder()
                    .encodeToString(anotherEcKey.toECPublicKey().encoded)

                System.setProperty("submission.jwtPublicKeys.TousAntiCovidKID", tacPublicKey)
                System.setProperty("submission.jwtPublicKeys.AnotherKID", anotherPublicKey)
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e)
            }
        }

        fun givenJwt(
            iat: Any? = Instant.now().epochSecond,
            jti: Any? = UUID.randomUUID().toString(),
            kid: String? = "TousAntiCovidKID",
            privateKey: ECPrivateKey = JWT_KEY_PAIR.toECPrivateKey()
        ): String {
            val claims = JWTClaimsSet.Builder()
                .issuer("SIDEP")
            if (iat != null) {
                claims.claim("iat", iat)
            }
            if (jti != null) {
                claims.claim("jti", jti)
            }

            val header = JWSHeader.Builder(JWSAlgorithm.ES256)
                .type(JOSEObjectType.JWT)
            if (kid != null) {
                header.keyID(kid)
            }

            val jwt = SignedJWT(header.build(), claims.build())
            jwt.sign(ECDSASigner(privateKey))
            return jwt.serialize()
        }
    }
}
