package fr.gouv.stopc.submissioncode.controller

import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.JWTManager.Companion.givenJwt
import fr.gouv.stopc.submissioncode.test.PostgresqlManager.Companion.givenTableSubmissionCodeContainsCode
import fr.gouv.stopc.submissioncode.test.When
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.http.HttpStatus.OK
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.MINUTES
import java.util.stream.Stream

@IntegrationTest
@ExtendWith(OutputCaptureExtension::class)
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
            "0000000A-0000-0000-0000-000000000000, LONG",
            "aaaaaa, SHORT",
            "bbbbbbbbbbbb, TEST"
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
    @CsvSource(
        "00000000-1111-1111-1111-111111111110, LONG",
        "AAA000, SHORT",
        "BBBBBB000000, TEST"

    )
    fun can_detect_a_code_doesnt_exist(unexistingCode: String, codeType: String, output: CapturedOutput) {
        When()
            .get("/api/v1/verify?code={code}", unexistingCode)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))

        Assertions.assertThat(output.all).contains(
            "Code $unexistingCode ($codeType) was not found."
        )
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "11111111-1111-1111-1111-111111111111",
            "EXP000",
            "EXP000000000"
        ]
    )
    fun can_detect_a_code_is_expired(expiredCode: String) {
        When()
            .get("/api/v1/verify?code={code}", expiredCode)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))
    }

    @ParameterizedTest
    @CsvSource(
        "0000000a-0000-0000-0000-000000000000, LONG",
        "AAAAAA, SHORT",
        "BBBBBBBBBBBB, TEST"

    )
    fun a_code_can_be_used_only_once(validCode: String, codeType: String, output: CapturedOutput) {
        given() // a code has been used
            .get("/api/v1/verify?code={code}", validCode)

        When() // it is used one more time
            .get("/api/v1/verify?code={code}", validCode)

            .then() // it should be rejected
            .statusCode(OK.value())
            .body("valid", equalTo(false))

        Assertions.assertThat(output.all).contains(
            "Code $validCode ($codeType) has already been used."
        )
    }

    @ParameterizedTest
    @CsvSource(
        "0000000a0000000000000000000000000000, LONG",
        "AAAAA@, SHORT",
        "BBBBBBBBBBB+, TEST"

    )
    fun can_reject_codes_not_matching_pattern(codeWithWrongPattern: String, codeType: String, output: CapturedOutput) {
        When()
            .get("/api/v1/verify?code={code}", codeWithWrongPattern)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))

        Assertions.assertThat(output.all).contains(
            "Code $codeWithWrongPattern ($codeType) does not match the expected pattern"
        )
    }

    @TestInstance(PER_CLASS)
    @Nested
    inner class JwtTest {

        private fun generateInvalidJwt() = Stream.of(
            Arguments.of(
                "the JWT is issued more than 10 days in the past",
                givenJwt(iat = Instant.now().minus(10, DAYS).epochSecond)
            ),
            Arguments.of(
                "the JWT is issued in the future",
                givenJwt(iat = Instant.now().plus(1, MINUTES).epochSecond)
            ),
            Arguments.of(
                "the JWT iat field is an empty string instead of a numeric Date",
                givenJwt(iat = "")
            ),
            Arguments.of(
                "the JWT has an iat field as a blank string instead of a numeric Date",
                givenJwt(iat = " ")
            ),
            Arguments.of(
                "the JWT has an iat field as a string instead of a numeric Date",
                givenJwt(iat = "123456")
            ),
            Arguments.of(
                "the iat field is missing",
                givenJwt(iat = null)
            ),
            Arguments.of(
                "the JWT has an empty jti field",
                givenJwt(jti = "")
            ),
            Arguments.of(
                "the JWT has a blank jti field",
                givenJwt(jti = " ")
            ),
            Arguments.of(
                "the jti field is missing",
                givenJwt(jti = null)
            ),
            Arguments.of(
                "the JWT has a jti field as a number instead of a string",
                givenJwt(jti = 1234)
            ),
            Arguments.of(
                "the JWT has an empty kid field ",
                givenJwt(kid = "")
            ),
            Arguments.of(
                "the JWT has a blank kid field ",
                givenJwt(kid = " ")
            ),
            Arguments.of(
                "the kid field is missing",
                givenJwt(kid = null)
            ),
            Arguments.of(
                "the JWT has an unknown kid value",
                givenJwt(kid = "test")
            ),
            Arguments.of(
                "the JWT has a kid value associate to a wrong key",
                givenJwt(kid = "AnotherKID")
            ),
        )

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
        fun reject_a_JWT_sign_with_unknown_private_key(output: CapturedOutput) {

            val unknownKeys = ECKeyGenerator(Curve.P_256)
                .keyID("unknownKey")
                .generate()

            val jwtWithUnknownSignature = givenJwt(privateKey = unknownKeys.toECPrivateKey())

            When()
                .get("/api/v1/verify?code={jwt}", jwtWithUnknownSignature)

                .then()
                .statusCode(OK.value())
                .body("valid", equalTo(false))

            Assertions.assertThat(output.all).contains(
                "Could not verify the signature of JWT: $jwtWithUnknownSignature"
            )
        }

        @DisplayName("A valid JWT must have a iat field as a Date, a unique jti field as a string, a kid field as a string which value is associated to a public key corresponding to the private key used to signed the JWT and is valid for 10 days ")
        @ParameterizedTest(name = "but {0}, so the JWT is not valid and the response is false")
        @MethodSource("generateInvalidJwt")
        fun reject_JWT_with_invalid_value_or_structure(title: String, serializedJwt: String) {

            When()
                .get("/api/v1/verify?code={jwt}", serializedJwt)

                .then()
                .statusCode(OK.value())
                .body("valid", equalTo(false))
        }

        @Test
        fun reject_a_JWT_with_jti_already_used(output: CapturedOutput) {

            val validJwt = givenJwt()

            given() // jwt is used once
                .get("/api/v1/verify?code={jwt}", validJwt)

            When() // jwt is used one more time
                .get("/api/v1/verify?code={jwt}", validJwt)

                .then() // it should be rejected
                .statusCode(OK.value())
                .body("valid", equalTo(false))

            Assertions.assertThat(output.all).contains(
                "The jti of JWT: $validJwt has already been used"
            )
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "",
                "    ",
                "a",
                "aaaaaaaa.aaaaaaaa",
            ]
        )
        fun reject_a_code_that_does_not_match_jwt_pattern(invalidJwt: String, output: CapturedOutput) {
            When()
                .get("/api/v1/verify?code={code}", invalidJwt)

                .then()
                .statusCode(OK.value())
                .body("valid", equalTo(false))

            Assertions.assertThat(output.all).contains(
                "The JWT: $invalidJwt does not match the expected pattern"
            )
        }

        @Test
        fun reject_a_code_that_is_not_a_valid_jwt(output: CapturedOutput) {
            When()
                .get("/api/v1/verify?code={code}", "aaaaaaa.aaaaaaa.aaaaaaa")

                .then()
                .statusCode(OK.value())
                .body("valid", equalTo(false))

            Assertions.assertThat(output.all).contains(
                "The JWT: aaaaaaa.aaaaaaa.aaaaaaa could not be parsed"
            )
        }
    }
}
