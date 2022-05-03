package fr.gouv.stopc.submissioncode.controller

import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.PostgresqlManager.Companion.debugSubmissionCodes
import fr.gouv.stopc.submissioncode.test.PostgresqlManager.Companion.givenTableSubmissionCodeContainsCode
import fr.gouv.stopc.submissioncode.test.When
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpStatus.OK
import java.time.Instant

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
        debugSubmissionCodes()
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
            "00000000-1111-1111-1111-111111111111",
            "AAA000",
            "BBBBBB000000"
        ]
    )
    fun can_detect_a_short_code_doesnt_exist(unexistingCode: String) {
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
}
