package fr.gouv.stopc.submissioncode.controller

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.JWTManager.Companion.givenJwt
import fr.gouv.stopc.submissioncode.test.JWTManager.Companion.givenJwtBuilder
import fr.gouv.stopc.submissioncode.test.PostgresqlManager.Companion.givenTableSubmissionCodeContainsCode
import fr.gouv.stopc.submissioncode.test.When
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpStatus.OK
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID
import java.util.stream.Stream

@IntegrationTest
class VerifyControllerTest {

    @BeforeEach
    fun `given some valid codes exists`() {
        givenTableSubmissionCodeContainsCode("1", "0000000a-0000-0000-0000-000000000000")
        givenTableSubmissionCodeContainsCode("2", "AAAAAA")
        givenTableSubmissionCodeContainsCode("3", "BBBBBBBBBBBB")
        val expiredInstant = Instant.now().minusSeconds(1)
        givenTableSubmissionCodeContainsCode("1", "00000000-1111-1111-1111-111111111111", expiresOn = expiredInstant)
        givenTableSubmissionCodeContainsCode("2", "EXP000", expiresOn = expiredInstant)
        givenTableSubmissionCodeContainsCode("3", "EXP000000000", expiresOn = expiredInstant)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "0000000a-0000-0000-0000-000000000000",
            "AAAAAA",
            "BBBBBBBBBBBB"
        ]
    )
    fun can_detect_a_code_is_valid(validCode: String) {
        When()
            .get("/api/v1/verify?code={code}", validCode)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(true))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "0000000A-0000-0000-0000-000000000000",
            "aaaaaa",
            "bbbbbbbbbbbb"
        ]
    )
    fun codes_are_case_sensitive(validCodeButWrongCase: String) {
        When()
            .get("/api/v1/verify?code={code}", validCodeButWrongCase)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "",
            "    ",
            "a",
            "bbbbbbbbbbbbbbbb",
            "00000000-1111-1111-1111-111111111111",
            "AAA000",
            "BBBBBB000000",
            "aaaaaaaa.aaaaaaaa",
            "aaaaaaaaaa.aaaaaaaaa.aaaaaaaaa"
        ]
    )
    fun can_detect_a_short_code_doesnt_exist_and_is_not_a_valid_JWT(unexistingCode: String) {
        When()
            .get("/api/v1/verify?code={code}", unexistingCode)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "11111111-1111-1111-1111-111111111111",
            "EXP000",
            "EXP000000000"
        ]
    )
    fun can_detect_a_short_code_is_expired(expiredCode: String) {
        When()
            .get("/api/v1/verify?code={code}", expiredCode)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "0000000a-0000-0000-0000-000000000000",
            "AAAAAA",
            "BBBBBBBBBBBB"
        ]
    )
    fun a_code_can_be_used_only_once(validCode: String) {
        // given a code has been used
        given()
            .get("/api/v1/verify?code={code}", validCode)
            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(true))

        // the next time it is used it should be invalid
        When()
            .get("/api/v1/verify?code={code}", validCode)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))
    }

    @TestInstance(PER_CLASS)
    @Nested
    inner class JwtTest {

        private fun generateInvalidJwt(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    "reject a JWT issued more than 7 days in the past",
                    givenJwt(issuedAt = Instant.now().minus(7, ChronoUnit.DAYS))
                ),
                Arguments.of(
                    "reject a JWT issued in the future",
                    givenJwt(issuedAt = Instant.now().plus(1, ChronoUnit.MINUTES))
                ),
                Arguments.of(
                    "reject a JWT with an iat field as an empty string instead of a Date",
                    givenJwtBuilder(
                        claimsBuilder = JWTClaimsSet.Builder()
                            .claim("iat", "")
                            .claim("jti", UUID.randomUUID())
                    )
                ),
                Arguments.of(
                    "reject a JWT with an iat field as a blank string instead of a Date",
                    givenJwtBuilder(
                        claimsBuilder = JWTClaimsSet.Builder()
                            .claim("iat", " ")
                            .claim("jti", UUID.randomUUID())
                    )
                ),
                Arguments.of(
                    "reject a JWT with an iat field as a string instead of a Date",
                    givenJwtBuilder(
                        claimsBuilder = JWTClaimsSet.Builder()
                            .claim("iat", "123456")
                            .claim("jti", UUID.randomUUID())
                    )
                ),
                Arguments.of(
                    "reject a JWT with missing iat field",
                    givenJwtBuilder(
                        claimsBuilder = JWTClaimsSet.Builder()
                            .jwtID("TousAntiCovidJti")
                    )
                ),
                Arguments.of(
                    "reject a JWT with an empty jti field",
                    givenJwt(jti = "")
                ),
                Arguments.of(
                    "reject a JWT with a blank jti field",
                    givenJwt(jti = " ")
                ),
                Arguments.of(
                    "reject a JWT with missing jti field",
                    givenJwtBuilder(
                        claimsBuilder = JWTClaimsSet.Builder()
                            .issueTime(Date.from(Instant.now()))
                    )
                ),
                Arguments.of(
                    "reject a JWT with jti field as a number instead of a string",
                    givenJwtBuilder(
                        claimsBuilder = JWTClaimsSet.Builder()
                            .claim("jti", 1234)
                            .issueTime(Date.from(Instant.now()))
                    )
                ),
                Arguments.of(
                    "reject a JWT with an empty kid field ",
                    givenJwt(kid = "")
                ),
                Arguments.of(
                    "reject a JWT with a blank kid field ",
                    givenJwt(kid = " ")
                ),
                Arguments.of(
                    "reject a JWT with missing kid field ",
                    givenJwtBuilder(
                        headerBuilder = JWSHeader.Builder(JWSAlgorithm.ES256)
                            .type(JOSEObjectType.JWT)
                    )
                ),
                Arguments.of(
                    "reject a JWT with unknown kid value",
                    givenJwt(kid = "test")
                ),
                Arguments.of(
                    "reject a JWT with a kid value associate to a wrong key",
                    givenJwt(kid = "AnotherKID")
                ),
            )
        }

        @Test
        fun validate_a_valid_JWT() {

            val validJwt = givenJwt()

            When()
                .get("/api/v1/verify?code={jwt}", validJwt)

                .then()
                .statusCode(OK.value())
                .body("valid", equalTo(true))
        }

        @Test
        fun reject_a_JWT_sign_with_unknown_private_key() {

            val unknownKeys = ECKeyGenerator(Curve.P_256)
                .keyID("unknownKey")
                .generate()

            val jwtWithUnknownSignature = givenJwt(privateKey = unknownKeys.toECPrivateKey())

            When()
                .get("/api/v1/verify?code={jwt}", jwtWithUnknownSignature)

                .then()
                .statusCode(OK.value())
                .body("valid", equalTo(false))
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("generateInvalidJwt")
        fun reject_JWT_with_invalid_value_or_structure(title: String, serializedJwt: String) {

            When()
                .get("/api/v1/verify?code={jwt}", serializedJwt)

                .then()
                .statusCode(OK.value())
                .body("valid", equalTo(false))
        }

        @Test
        fun reject_a_JWT_with_jti_already_used() {

            val validJwt = givenJwt()

            given()
                .get("/api/v1/verify?code={jwt}", validJwt)

                .then()
                .statusCode(OK.value())
                .body("valid", equalTo(true))

            When()
                .get("/api/v1/verify?code={jwt}", validJwt)

                .then()
                .statusCode(OK.value())
                .body("valid", equalTo(false))
        }
    }
}
