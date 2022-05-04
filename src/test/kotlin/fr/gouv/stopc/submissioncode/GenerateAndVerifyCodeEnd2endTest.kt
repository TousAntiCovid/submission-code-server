package fr.gouv.stopc.submissioncode

import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.When
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpStatus.OK

@IntegrationTest
class GenerateAndVerifyCodeEnd2endTest {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/api/v1/generate/short",
            "/api/v1/generate/test"
        ]
    )
    fun a_code_generated_with_the_api_can_be_verified(generatePath: String) {
        val code = given()
            .get(generatePath)
            .then()
            .extract()
            .jsonPath()
            .getString("code")

        When()
            .get("/api/v1/verify?code={code}", code)

            .then()
            .statusCode(OK.value())
            .body("valid", equalTo(true))
    }
}
