package fr.gouv.stopc.submissioncode

import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.When
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.awaitility.Awaitility
import org.awaitility.pollinterval.FibonacciPollInterval
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

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
        val shortCode = given()
            .get(generatePath)
            .then()
            .extract()
            .jsonPath()
            .getString("code")

        When()
            .get("/api/v1/verify?code={code}", shortCode)

            .then()
            .statusCode(HttpStatus.OK.value())
            .body("valid", Matchers.equalTo(true))
    }

    @Test
    fun a_long_code_generated_by_the_scheduler_can_be_verified(@Autowired jdbcTemplate: JdbcTemplate) {
        // TODO implementation based on old HTTP API, should be rewritten using the scheduled operation
        // @Autowired val LongCodesGeneratorScheduler generator
        // generator.generateCodes(...)
        // extract a long code from one the sftp archives
        // then verify it can be validated
        given()
            .contentType(ContentType.JSON)
            .body(
                mapOf(
                    "from" to OffsetDateTime.now().toString(),
                    "to" to OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS).plusDays(1).minusNanos(1).toString(),
                    "dailyAmount" to 100
                )
            )
            .post("/api/v1/back-office/codes/generate/request")

        val longCode = Awaitility.await("a long code has been generated")
            .ignoreExceptions()
            .pollInterval(FibonacciPollInterval.fibonacci())
            .until(
                {
                    jdbcTemplate.queryForObject(
                        "select code from submission_code where type_code = '1' and used is false and date_use is null limit 1",
                        String::class.java
                    )
                },
                Matchers.notNullValue()
            )

        When()
            .get("/api/v1/verify?code={code}", longCode)

            .then()
            .statusCode(HttpStatus.OK.value())
            .body("valid", Matchers.equalTo(true))
    }
}
