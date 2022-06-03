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
import java.util.Date

@Slf4j
class JWTManager : TestExecutionListener {

    companion object {

        private val tacEcKey: ECKey

        init {

            try {

                tacEcKey = ECKeyGenerator(Curve.P_256)
                    .keyID("TousAntiCovidKID")
                    .generate()

                val tacPublicECKey = Base64.getEncoder()
                    .encodeToString(tacEcKey.toECPublicKey().encoded)

                val anotherEcKey = ECKeyGenerator(Curve.P_256)
                    .keyID("AnotherKID")
                    .generate()

                val anotherPublicEcKey = Base64.getEncoder()
                    .encodeToString(anotherEcKey.toECPublicKey().encoded)

                System.setProperty(
                    "submission.jwtPublicKeys.tousanticovidkid",
                    tacPublicECKey
                )
                System.setProperty(
                    "submission.jwtPublicKeys.anotherkid",
                    anotherPublicEcKey
                )
                System.setProperty(
                    "submission.jwtPublicKeys.D99DA4422914F5E8",
                    "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEosj0ewm2rrYCaDtkcw9aL+c5y8jX8MRTBDr+QagGvqYoSfeaT1p+nmn30VhRsfPj3pH6qyZTltgatLdvlsv4QA=="
                )
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e)
            }
        }

        private fun generateJwtClaims(date: Date, jti: String): JWTClaimsSet.Builder {
            return JWTClaimsSet.Builder()
                .jwtID(jti)
                .issueTime(date)
                .issuer("SIDEP")
        }

        private fun generateJwtHeader(kid: String): JWSHeader.Builder {
            return JWSHeader.Builder(JWSAlgorithm.ES256)
                .keyID(kid)
                .type(JOSEObjectType.JWT)
        }

        fun givenJwtBuilder(
            headerBuilder: JWSHeader.Builder = generateJwtHeader("TousAntiCovidKID"),
            claimsBuilder: JWTClaimsSet.Builder = generateJwtClaims(Date.from(Instant.now()), "TousAntiCovidJti"),
        ): String {
            val jwt = SignedJWT(headerBuilder.build(), claimsBuilder.build())
            jwt.sign(ECDSASigner(tacEcKey.toECPrivateKey()))

            return jwt.serialize()
        }

        fun givenJwt(
            issuedAt: Instant = Instant.now(),
            jti: String = "TousAntiCovidJti",
            kid: String = "TousAntiCovidKID",
            privateKey: ECPrivateKey = tacEcKey.toECPrivateKey()
        ): String {

            val jwtClaim = generateJwtClaims(Date.from(issuedAt), jti)
            val jwtHeader = generateJwtHeader(kid)

            val jwt = SignedJWT(jwtHeader.build(), jwtClaim.build())
            jwt.sign(ECDSASigner(privateKey))

            return jwt.serialize()
        }
    }
}
