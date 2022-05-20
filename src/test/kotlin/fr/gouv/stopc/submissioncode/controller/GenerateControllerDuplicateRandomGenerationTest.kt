package fr.gouv.stopc.submissioncode.controller

import fr.gouv.stopc.submissioncode.repository.model.SubmissionCode.Type.SHORT
import fr.gouv.stopc.submissioncode.repository.model.SubmissionCode.Type.TEST
import fr.gouv.stopc.submissioncode.service.RandomGenerator
import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.PostgresqlManager.Companion.givenTableSubmissionCodeContainsCode
import fr.gouv.stopc.submissioncode.test.When
import fr.gouv.stopc.submissioncode.test.isoDateTimeWithin
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.OK
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.HOURS
import java.time.temporal.ChronoUnit.SECONDS

@IntegrationTest
class GenerateControllerDuplicateRandomGenerationTest(@Autowired @MockBean private val randomGenerator: RandomGenerator) {

    @Test
    fun can_generate_short_code_even_the_2_firsts_already_exists() {
        givenTableSubmissionCodeContainsCode(SHORT.dbValue, "AAAAAA")
        `when`(randomGenerator.upperCaseString(6))
            .thenReturn("AAAAAA", "AAAAAA", "BBBBBB")

        When()
            .get("/api/v1/generate/short")

            .then()
            .statusCode(OK.value())
            .body("code", equalTo("BBBBBB"))
            .body("validFrom", isoDateTimeWithin(5, SECONDS, Instant.now()))
            .body("validUntil", isoDateTimeWithin(5, SECONDS, Instant.now().plus(1, HOURS)))
            .body("dateGenerate", isoDateTimeWithin(5, SECONDS, Instant.now()))
    }

    @Test
    fun can_generate_test_code_even_the_2_firsts_already_exists() {
        givenTableSubmissionCodeContainsCode(TEST.dbValue, "AAAAAAAAAAAA")
        `when`(randomGenerator.upperCaseString(12))
            .thenReturn("AAAAAAAAAAAA", "AAAAAAAAAAAA", "BBBBBBBBBBBB")

        When()
            .get("/api/v1/generate/test")

            .then()
            .statusCode(OK.value())
            .body("code", equalTo("BBBBBBBBBBBB"))
            .body("validFrom", isoDateTimeWithin(5, SECONDS, Instant.now()))
            .body("validUntil", isoDateTimeWithin(5, SECONDS, Instant.now().plus(15, DAYS).truncatedTo(DAYS)))
            .body("dateGenerate", isoDateTimeWithin(5, SECONDS, Instant.now()))
    }

    @Test
    fun returns_server_error_after_10_generations_of_already_existing_short_codes() {
        givenTableSubmissionCodeContainsCode(SHORT.dbValue, "AAAAAA")
        `when`(randomGenerator.upperCaseString(6))
            .thenReturn("AAAAAA")

        When()
            .get("/api/v1/generate/short")

            .then()
            .statusCode(INTERNAL_SERVER_ERROR.value())
            .body("status", equalTo(500))
            .body("error", equalTo("Internal Server Error"))
            .body("message", equalTo("No unique code could be generated after 10 random attempts"))
            .body("path", equalTo("/api/v1/generate/short"))
    }

    @Test
    fun returns_server_error_after_10_generations_of_already_existing_test_codes() {
        givenTableSubmissionCodeContainsCode(SHORT.dbValue, "AAAAAAAAAAAA")
        `when`(randomGenerator.upperCaseString(12))
            .thenReturn("AAAAAAAAAAAA")

        When()
            .get("/api/v1/generate/test")

            .then()
            .statusCode(INTERNAL_SERVER_ERROR.value())
            .body("status", equalTo(500))
            .body("error", equalTo("Internal Server Error"))
            .body("message", equalTo("No unique code could be generated after 10 random attempts"))
            .body("path", equalTo("/api/v1/generate/test"))
    }
}
