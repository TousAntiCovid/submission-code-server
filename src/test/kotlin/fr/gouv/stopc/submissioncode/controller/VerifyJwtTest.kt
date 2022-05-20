package fr.gouv.stopc.submissioncode.controller

import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.JWTManager.Companion.givenValidJwt
import fr.gouv.stopc.submissioncode.test.When
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.OK
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS
import java.util.Date

@IntegrationTest
class VerifyJwtTest {

    @Test
    fun validate_a_valid_jWT() {

        val validJwt = givenValidJwt()

        When()
            .get("/api/v1/verifyJwt?jwt={jwt}", validJwt)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(true))
    }

    @Test
    fun reject_a_JWT_with_invalid_signature() {

        val wrongEcKey = ECKeyGenerator(Curve.P_256)
            .keyID("ecKey")
            .generate()

        val jwtWithInvalidSignature = givenValidJwt(privateKey = wrongEcKey.toECPrivateKey())

        When()
            .get("/api/v1/verifyJwt?jwt={jwt}", jwtWithInvalidSignature)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))
    }

    @Test
    fun reject_a_JWT_with_iat_from_more_than_7_days_in_the_past() {

        val jwtWithOutdatedIat = givenValidJwt(date = Date.from(Instant.now().minus(30, DAYS)))

        When()
            .get("/api/v1/verifyJwt?jwt={jwt}", jwtWithOutdatedIat)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))
    }

    @Test
    fun reject_a_JWT_with_iat_in_the_future() {

        val jwtWithIatInTheFuture = givenValidJwt(date = Date.from(Instant.now().plus(1, DAYS)))

        When()
            .get("/api/v1/verifyJwt?jwt={jwt}", jwtWithIatInTheFuture)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))
    }

    @Test
    fun reject_a_JWT_with_jti_already_used() {

        val validJwt = givenValidJwt()

        given()
            .get("/api/v1/verifyJwt?jwt={jwt}", validJwt)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(true))

        When()
            .get("/api/v1/verifyJwt?jwt={code}", validJwt)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))
    }

    @Test
    fun reject_a_JWT_with_kid_missing() {

        val jwtWithMissingKdi = givenValidJwt(kid = "")

        When()
            .get("/api/v1/verifyJwt?jwt={jwt}", jwtWithMissingKdi)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))
    }

    @Test
    fun reject_a_JWT_with_unknown_kid_value() {

        val jwtWithUnknownKdi = givenValidJwt(kid = "test")

        When()
            .get("/api/v1/verifyJwt?jwt={jwt}", jwtWithUnknownKdi)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))
    }

    @Test
    fun reject_a_JWT_with_a_kid_associate_to_a_wrong_key() {

        val jwtWithWrongKdi = givenValidJwt(kid = "D99DA4422914F5E8")

        When()
            .get("/api/v1/verifyJwt?jwt={jwt}", jwtWithWrongKdi)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))
    }
}
