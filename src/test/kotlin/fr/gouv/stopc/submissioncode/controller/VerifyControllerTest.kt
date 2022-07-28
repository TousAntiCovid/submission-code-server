package fr.gouv.stopc.submissioncode.controller

import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import fr.gouv.stopc.submissioncode.service.model.CodeType
import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.JWTManager.Companion.givenJwt
import fr.gouv.stopc.submissioncode.test.PostgresqlManager.Companion.givenTableSubmissionCodeContainsCode
import fr.gouv.stopc.submissioncode.test.When
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
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
import java.util.Base64
import java.util.UUID
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

        assertThat(output.all)
            .contains("Code $unexistingCode doesn't exist")
    }

    @ParameterizedTest
    @CsvSource(
        "LONG,00000000-1111-1111-1111-111111111111",
        "SHORT,EXP000",
        "TEST,EXP000000000"
    )
    fun can_detect_a_code_is_expired(codeType: String, expiredCode: String, output: CapturedOutput) {
        When()
            .get("/api/v1/verify?code={code}", expiredCode)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))

        assertThat(output.all)
            .contains("$codeType code '$expiredCode' has expired on ")
    }

    @ParameterizedTest
    @CsvSource(
        "LONG,f1111111-1111-1111-1111-111111111111",
        "SHORT,FUT000",
        "TEST,FUT000000000"
    )
    fun can_detect_a_code_is_not_yet_available(codeType: CodeType, unavailableCode: String, output: CapturedOutput) {
        givenTableSubmissionCodeContainsCode(
            codeType.databaseRepresentation!!.dbValue,
            unavailableCode,
            availableFrom = Instant.now().plus(20, DAYS)
        )

        When()
            .get("/api/v1/verify?code={code}", unavailableCode)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))

        assertThat(output.all)
            .contains("$codeType code '$unavailableCode' is not yet available (availability date is ")
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

        assertThat(output.all)
            .contains("$codeType code '$validCode' has already been used")
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "",
            "  ",
            "____",
            "0000000a0000000000000000000000000000",
            "AAAAA@",
            "BBBBBBBBBBB+",
            "aaaaaaaa.aaaaaaaa",
        ]
    )
    fun can_reject_codes_not_matching_pattern(codeWithWrongPattern: String, output: CapturedOutput) {
        When()
            .get("/api/v1/verify?code={code}", codeWithWrongPattern)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))

        assertThat(output.all)
            .contains("Code $codeWithWrongPattern does not match any code type and can't be validated")
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "unknow",
            "unknownunkno",
            "0000000b-0000-0000-0000-000000000000"
        ]
    )
    fun can_reject_codes_unknown_from_database(unknownCode: String, output: CapturedOutput) {
        When()
            .get("/api/v1/verify?code={code}", unknownCode)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))

        assertThat(output.all)
            .contains("Code $unknownCode doesn't exist")
    }

    @Test
    fun can_report_inconsistent_code_in_database(output: CapturedOutput) {
        // "2" is SHORT code type
        // "AAAAAA123456" is a TEST code value
        givenTableSubmissionCodeContainsCode("2", "AAAAAA123456")

        When()
            .get("/api/v1/verify?code={code}", "AAAAAA123456")

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))

        assertThat(output.all)
            .contains("'AAAAAA123456' seems to be a TEST code but database knows it as a SHORT code")
    }

    @TestInstance(PER_CLASS)
    @Nested
    inner class JwtTest {

        private fun generateInvalidJwt(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    "the JWT is issued more than 10 days in the past",
                    givenJwt(iat = Instant.now().minus(10, DAYS).epochSecond),
                    "JWT has expired, it was issued at [^ ]+, so it expired on [^ ]+:"
                ),
                Arguments.of(
                    "the JWT is issued in the future",
                    givenJwt(iat = Instant.now().plus(1, MINUTES).epochSecond),
                    "JWT is issued at a future time \\([^)]+\\):"
                ),
                Arguments.of(
                    "the iat field is an empty string instead of a numeric Date",
                    givenJwt(iat = ""),
                    "JWT claims set could not be parsed: Unexpected type of JSON object member with key iat,"
                ),
                Arguments.of(
                    "the iat field is a blank string instead of a numeric Date",
                    givenJwt(iat = " "),
                    "JWT claims set could not be parsed: Unexpected type of JSON object member with key iat,"
                ),
                Arguments.of(
                    "the iat field is a string instead of a numeric Date",
                    givenJwt(iat = "123456"),
                    "JWT claims set could not be parsed: Unexpected type of JSON object member with key iat,"
                ),
                Arguments.of(
                    "the iat field is missing",
                    givenJwt(iat = null),
                    "JWT is missing claim jti \\([^)]+\\) or iat \\(null\\):"
                ),
                Arguments.of(
                    "the jti field is empty",
                    givenJwt(jti = ""),
                    "JWT is missing claim jti \\(\\) or iat \\([^)]+\\):"
                ),
                Arguments.of(
                    "the jti field is blank",
                    givenJwt(jti = " "),
                    "JWT is missing claim jti \\( \\) or iat \\([^)]+\\):"
                ),
                Arguments.of(
                    "the jti field is missing",
                    givenJwt(jti = null),
                    "JWT is missing claim jti \\(null\\) or iat \\([^)]+\\):"
                ),
                Arguments.of(
                    "the jti field is a number instead of a string",
                    givenJwt(jti = 1234),
                    "JWT claims set could not be parsed: Unexpected type of JSON object member with key jti,"
                ),
                Arguments.of(
                    "the kid field is empty",
                    givenJwt(kid = ""),
                    "No public key found in configuration for kid '':"
                ),
                Arguments.of(
                    "the kid field is blank",
                    givenJwt(kid = " "),
                    "No public key found in configuration for kid ' ':"
                ),
                Arguments.of(
                    "the kid field is missing",
                    givenJwt(kid = null),
                    "No public key found in configuration for kid 'null':"
                ),
                Arguments.of(
                    "the kid field is an unknown kid",
                    givenJwt(kid = "Unknown"),
                    "No public key found in configuration for kid 'Unknown':"
                ),
                Arguments.of(
                    "the kid field points to the wrong public key",
                    givenJwt(kid = "AnotherKID"),
                    "JWT signature is invalid:"
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

            assertThat(output.all)
                .contains("JWT signature is invalid: $jwtWithUnknownSignature")
        }

        @DisplayName("A valid JWT must have a iat field as a Date, a unique jti field as a string, a kid field as a string which value is associated to a public key corresponding to the private key used to signed the JWT and is valid for 10 days ")
        @ParameterizedTest(name = "but {0}, so the JWT is not valid and the response is false")
        @MethodSource("generateInvalidJwt")
        fun reject_JWT_with_invalid_value_or_structure(title: String, serializedJwt: String, logMessage: String, output: CapturedOutput) {

            When()
                .get("/api/v1/verify?code={jwt}", serializedJwt)

                .then()
                .statusCode(OK.value())
                .body("valid", equalTo(false))

            assertThat(output.all)
                .containsPattern("$logMessage $serializedJwt")
        }

        @Test
        fun reject_a_JWT_with_jti_already_used(output: CapturedOutput) {

            val jti = UUID.randomUUID().toString()
            val validJwt = givenJwt(jti = jti)

            given() // jwt is used once
                .get("/api/v1/verify?code={jwt}", validJwt)

            When() // jwt is used one more time
                .get("/api/v1/verify?code={jwt}", validJwt)

                .then() // it should be rejected
                .statusCode(OK.value())
                .body("valid", equalTo(false))

            assertThat(output.all)
                .contains("JWT with jti '$jti' has already been used: $validJwt")
        }

        @Test
        fun reject_a_code_that_is_not_a_valid_jwt(output: CapturedOutput) {
            When()
                .get("/api/v1/verify?code={code}", "aaaaaaa.aaaaaaa.aaaaaaa")

                .then()
                .statusCode(OK.value())
                .body("valid", equalTo(false))

            assertThat(output.all)
                .containsPattern("JWT could not be parsed: Invalid JWS header: Invalid JSON: Unexpected token [^ ]+ at position 5., aaaaaaa.aaaaaaa.aaaaaaa")
        }

        @Test
        fun reject_a_JWT_with_invalid_alg_header_field(output: CapturedOutput) {

            val invalidHeader = """
                {
                    "alg": "invalid alg",
                    "typ": "JWT",
                    "kid": "TousAntiCovidKID"
                }
            """.trimIndent()
                .toByteArray()
                .let { Base64.getEncoder().encodeToString(it) }

            val jwtWithInvalidHeader = givenJwt().replaceBefore(".", invalidHeader)

            When()
                .get("/api/v1/verify?code={jwt}", jwtWithInvalidHeader)

                .then()
                .statusCode(OK.value())
                .body("valid", equalTo(false))

            assertThat(output.all)
                .contains("JWT signature can't be verified: Unsupported JWS algorithm invalid alg, must be ES256, $jwtWithInvalidHeader")
        }
    }
}
