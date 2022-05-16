package fr.gouv.stopc.submissioncode.controller

import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.JWTManager.Companion.defaultEcKey
import fr.gouv.stopc.submissioncode.test.JWTManager.Companion.generateJwt
import fr.gouv.stopc.submissioncode.test.JWTManager.Companion.generateJwtClaims
import fr.gouv.stopc.submissioncode.test.JWTManager.Companion.generateJwtHeader
import fr.gouv.stopc.submissioncode.test.When
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.OK
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

@IntegrationTest
class VerifyJwtTest {

    @Test
    fun validate_a_valid_jWT() {

        val validJwt = generateJwt(
            generateJwtClaims(Date.from(Instant.now()), "TousAntiCovidJti"),
            generateJwtHeader("TousAntiCovidKID"),
            defaultEcKey.toECPrivateKey()
        )

        When()
            .get("/api/v1/verifyJwt?jwt={code}", validJwt)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(true))
    }

    @Test
    fun reject_a_JWT_with_invalid_signature() {

        val wrongEcKey = ECKeyGenerator(Curve.P_256)
            .keyID("ecKey")
            .generate()

        val jwtWithInvalidSignature = generateJwt(
            generateJwtClaims(Date.from(Instant.now()), "TousAntiCovidJti"),
            generateJwtHeader("TousAntiCovidKID"),
            wrongEcKey.toECPrivateKey()
        )

        When()
            .get("/api/v1/verifyJwt?jwt={code}", jwtWithInvalidSignature)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))
    }

    @Test
    fun reject_a_JWT_with_iat_from_more_than_7_days_in_the_past() {

        val jwtWithOutdatedIat = generateJwt(
            generateJwtClaims(Date.from(Instant.now().minus(30, ChronoUnit.DAYS)), "TousAntiCovidJti"),
            generateJwtHeader("TousAntiCovidKID"),
            defaultEcKey.toECPrivateKey()
        )

        When()
            .get("/api/v1/verifyJwt?jwt={code}", jwtWithOutdatedIat)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))
    }

    @Test
    fun reject_a_JWT_with_iat_in_the_future() {

        val jwtWithIatInTheFuture = generateJwt(
            generateJwtClaims(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)), "TousAntiCovidJti"),
            generateJwtHeader("TousAntiCovidKID"),
            defaultEcKey.toECPrivateKey()
        )

        When()
            .get("/api/v1/verifyJwt?jwt={code}", jwtWithIatInTheFuture)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))
    }

    @Test
    fun reject_a_JWT_with_jti_already_used() {

        val validJwt = generateJwt(
            generateJwtClaims(Date.from(Instant.now()), "TousAntiCovidJti"),
            generateJwtHeader("TousAntiCovidKID"),
            defaultEcKey.toECPrivateKey()
        )

        given()
            .get("/api/v1/verifyJwt?jwt={code}", validJwt)

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

        val jwtWithMissingKdi = generateJwt(
            generateJwtClaims(Date.from(Instant.now()), "TousAntiCovidJti"),
            generateJwtHeader(""),
            defaultEcKey.toECPrivateKey()
        )

        When()
            .get("/api/v1/verifyJwt?jwt={code}", jwtWithMissingKdi)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))
    }

    @Test
    fun reject_a_JWT_with_unknown_kid_value() {

        val jwtWithUnknownKdi = generateJwt(
            generateJwtClaims(Date.from(Instant.now()), "TousAntiCovidJti"),
            generateJwtHeader("test"),
            defaultEcKey.toECPrivateKey()
        )

        When()
            .get("/api/v1/verifyJwt?jwt={code}", jwtWithUnknownKdi)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))
    }

    @Test
    fun reject_a_JWT_with_a_kid_associate_to_a_wrong_key() {

        val jwtWithWrongKdi = generateJwt(
            generateJwtClaims(Date.from(Instant.now()), "TousAntiCovidJti"),
            generateJwtHeader("D99DA4422914F5E8"),
            defaultEcKey.toECPrivateKey()
        )

        When()
            .get("/api/v1/verifyJwt?jwt={code}", jwtWithWrongKdi)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(false))
    }
}
