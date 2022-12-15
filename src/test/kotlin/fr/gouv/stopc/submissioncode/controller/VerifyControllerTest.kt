package fr.gouv.stopc.submissioncode.controller

import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.JWTManager.Companion.givenJwt
import fr.gouv.stopc.submissioncode.test.When
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.http.HttpStatus.OK
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.MINUTES
import java.time.temporal.ChronoUnit.SECONDS
import java.util.Base64
import java.util.UUID
import java.util.stream.Stream

@IntegrationTest
@ExtendWith(OutputCaptureExtension::class)
@TestInstance(PER_CLASS)
class VerifyControllerTest {

    private fun generateInvalidJwt(): Stream<Arguments> {
        val tenDaysAgo = Instant.now().minus(10, DAYS).truncatedTo(SECONDS)
        val oneMinuteAgo = Instant.now().minus(1, MINUTES).truncatedTo(SECONDS)
        val oneMinuteLater = Instant.now().plus(1, MINUTES).truncatedTo(SECONDS)
        return Stream.of(
            Arguments.of(
                "the JWT is issued more than 10 days in the past",
                givenJwt(iat = tenDaysAgo.epochSecond),
                "JWT has expired, it was issued at $tenDaysAgo, so it expired on ${tenDaysAgo.plus(Duration.ofDays(10))}:"
            ),
            Arguments.of(
                "the JWT is issued in the future",
                givenJwt(iat = oneMinuteLater.epochSecond),
                "JWT is issued at a future time ($oneMinuteLater):"
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
                givenJwt(jti = "some-jwt-identifier", iat = null),
                "JWT is missing claim jti (some-jwt-identifier) or iat (null):"
            ),
            Arguments.of(
                "the jti field is empty",
                givenJwt(jti = "", iat = oneMinuteAgo.epochSecond),
                "JWT is missing claim jti () or iat ($oneMinuteAgo):"
            ),
            Arguments.of(
                "the jti field is blank",
                givenJwt(jti = " ", iat = oneMinuteAgo.epochSecond),
                "JWT is missing claim jti ( ) or iat ($oneMinuteAgo):"
            ),
            Arguments.of(
                "the jti field is missing",
                givenJwt(jti = null, iat = oneMinuteAgo.epochSecond),
                "JWT is missing claim jti (null) or iat ($oneMinuteAgo):"
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
            Arguments.of(
                "the alg field is missing",
                givenJwt().replaceBefore(".", Base64.getEncoder().encodeToString("""{"kid": "TousAntiCovidKID","typ":"JWT"}""".toByteArray())),
                "JWT could not be parsed: Invalid JWS header: Missing \"alg\" in header JSON object,"
            ),
            Arguments.of(
                "the JWT header is corrupt",
                givenJwt().replaceBefore(".", "Z"),
                "JWT could not be parsed: Invalid JWS header: Invalid JSON object,"
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
            .contains("$logMessage $serializedJwt")
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
            .contains("JWT could not be parsed: Invalid JWS header: Invalid JSON: java.lang.IllegalStateException: Expected BEGIN_OBJECT but was STRING at line 1 column 1 path \$, aaaaaaa.aaaaaaa.aaaaaaa")
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
