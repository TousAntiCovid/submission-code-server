package fr.gouv.stopc.submissioncode.controller

import fr.gouv.stopc.submission.code.server.data.repository.SubmissionCodeRepository
import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.When
import fr.gouv.stopc.submissioncode.test.isoDateTimeWithin
import org.exparity.hamcrest.date.OffsetDateTimeMatchers.within
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.matchesPattern
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.OffsetDateTime.now
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.SECONDS

@IntegrationTest
class GenerateControllerTest(@Autowired val submissionCodeRepository: SubmissionCodeRepository) {

    @Test
    fun can_generate_a_short_code() {
        val shortCode = When()
            .get("/api/v1/generate/short")

            .then()
            .body("code", matchesPattern("[A-Z0-9]{6}"))
            .body("validFrom", isoDateTimeWithin(5, SECONDS, now()))
            .body("validUntil", isoDateTimeWithin(5, SECONDS, now().plusHours(1)))
            .body("dateGenerate", isoDateTimeWithin(5, SECONDS, now()))

            .extract()
            .jsonPath()
            .getString("code")

        val generatedCode = submissionCodeRepository.findByCode(shortCode)
        assertThat(generatedCode.id, notNullValue())
        assertThat(generatedCode.code, matchesPattern("[A-Z0-9]{6}"))
        assertThat(generatedCode.type, equalTo("2"))
        assertThat(generatedCode.dateGeneration, within(5, SECONDS, now()))
        assertThat(generatedCode.dateAvailable, within(5, SECONDS, now()))
        assertThat(generatedCode.dateEndValidity, within(5, SECONDS, now().plusHours(1)))
        assertThat(generatedCode.dateUse, nullValue())
        assertThat(generatedCode.used, equalTo(false))
        assertThat(generatedCode.lotkey, notNullValue())
    }

    @Test
    fun can_generate_a_test_code() {
        val shortCode = When()
            .get("/api/v1/generate/test")

            .then()
            .body("code", matchesPattern("[a-zA-Z0-9]{12}"))
            .body("validFrom", isoDateTimeWithin(5, SECONDS, now()))
            .body("validUntil", isoDateTimeWithin(5, SECONDS, now().plusDays(15).truncatedTo(DAYS)))
            .body("dateGenerate", isoDateTimeWithin(5, SECONDS, now()))

            .extract()
            .jsonPath()
            .getString("code")

        val generatedCode = submissionCodeRepository.findByCode(shortCode)
        assertThat(generatedCode.id, notNullValue())
        assertThat(generatedCode.code, matchesPattern("[a-zA-Z0-9]{12}"))
        assertThat(generatedCode.type, equalTo("3"))
        assertThat(generatedCode.dateGeneration, within(5, SECONDS, now()))
        assertThat(generatedCode.dateAvailable, within(5, SECONDS, now()))
        assertThat(generatedCode.dateEndValidity, within(5, SECONDS, now().plusDays(15).truncatedTo(DAYS)))
        assertThat(generatedCode.dateUse, nullValue())
        assertThat(generatedCode.used, equalTo(false))
        assertThat(generatedCode.lotkey, nullValue())
    }
}
