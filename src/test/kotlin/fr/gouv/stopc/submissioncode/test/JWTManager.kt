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
import java.util.UUID

@Slf4j
class JWTManager : TestExecutionListener {

    companion object {

        private val defaultEcKey: ECKey

        init {

            try {

                defaultEcKey = ECKeyGenerator(Curve.P_256)
                    .keyID("TousAntiCovidKID")
                    .generate()

                val publicECKey = Base64.getEncoder()
                    .encodeToString(defaultEcKey.toECPublicKey().encoded)

                System.setProperty(
                    "submission.code.server.publicKeys.TousAntiCovidKID",
                    publicECKey
                )
                System.setProperty(
                    "submission.code.server.publicKeys.D99DA4422914F5E8",
                    "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEosj0ewm2rrYCaDtkcw9aL+c5y8jX8MRTBDr+QagGvqYoSfeaT1p+nmn30VhRsfPj3pH6qyZTltgatLdvlsv4QA=="
                )
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e)
            }
        }

        private fun generateJwtClaims(date: Date, jti: String): JWTClaimsSet {
            return JWTClaimsSet.Builder()
                .jwtID(jti)
                .issueTime(date)
                .issuer("SIDEP")
                .build()
        }

        private fun generateJwtHeader(kid: String): JWSHeader {
            return JWSHeader.Builder(JWSAlgorithm.ES256)
                .keyID(kid)
                .type(JOSEObjectType.JWT)
                .build()
        }

        fun givenJwtWithIncorrectIat(iat: String): String {
            val jwtClaim = JWTClaimsSet.Builder()
                .claim("iat", iat)
                .claim("iss", "SIDEP")
                .claim("jti", UUID.randomUUID().toString())
                .build()
            val jwtHeader = generateJwtHeader("TousAntiCovidKID")
            val jwt = SignedJWT(jwtHeader, jwtClaim)
            jwt.sign(ECDSASigner(defaultEcKey.toECPrivateKey()))

            return jwt.serialize()
        }

        fun givenValidJwt(
            issuedAt: Instant = Instant.now(),
            jti: String = "TousAntiCovidJti",
            kid: String = "TousAntiCovidKID",
            privateKey: ECPrivateKey = defaultEcKey.toECPrivateKey()
        ): String {

            val jwtClaim = generateJwtClaims(Date.from(issuedAt), jti)
            val jwtHeader = generateJwtHeader(kid)

            val jwt = SignedJWT(jwtHeader, jwtClaim)
            jwt.sign(ECDSASigner(privateKey))

            return jwt.serialize()
        }
    }
}
