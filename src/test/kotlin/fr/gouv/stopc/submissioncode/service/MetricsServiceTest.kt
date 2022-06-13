package fr.gouv.stopc.submissioncode.service

import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.PostgresqlManager
import fr.gouv.stopc.submissioncode.test.When
import io.micrometer.core.instrument.MeterRegistry
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import java.time.Instant

@IntegrationTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class MetricsServiceTest(@Autowired val meterRegistry: MeterRegistry) {

    @BeforeEach
    fun `given some valid codes exists`() {
        PostgresqlManager.givenTableSubmissionCodeContainsCode("1", "0000000a-0000-0000-0000-000000000000")
        PostgresqlManager.givenTableSubmissionCodeContainsCode("3", "BBBBBBBBBBBB")
        PostgresqlManager.givenTableSubmissionCodeContainsCode("2", "AAAAAA")
        val expiredInstant = Instant.now().minusSeconds(1)
        PostgresqlManager.givenTableSubmissionCodeContainsCode("1", "0000000a-0000-0000-0000-000000000001", expiresOn = expiredInstant)
        PostgresqlManager.givenTableSubmissionCodeContainsCode("3", "BBBBBBBBBBBA", expiresOn = expiredInstant)
        PostgresqlManager.givenTableSubmissionCodeContainsCode("2", "AAAAAB", expiresOn = expiredInstant)
    }

    @ParameterizedTest
    @CsvSource(
        "AAAAAA, AAAAAB, SHORT",
        "BBBBBBBBBBBB, BBBBBBBBBBBA, TEST",
        "0000000a-0000-0000-0000-000000000000, 0000000a-0000-0000-0000-000000000001, LONG"
    )
    fun increment_counters(validCode: String, invalidCode: String, codeType: String) {

        When()
            .get("/api/v1/verify?code={code}", validCode)

            .then()
            .statusCode(HttpStatus.OK.value())
            .body("valid", Matchers.equalTo(true))

        When()
            .get("/api/v1/verify?code={code}", invalidCode)

            .then()
            .statusCode(HttpStatus.OK.value())
            .body("valid", Matchers.equalTo(false))

        val expectedShortCodeCounter = listOf(tuple("submission.verify.code", codeType, "true"), tuple("submission.verify.code", codeType, "false"))

        assertThat(meterRegistry.meters)
            .extracting({ it.id.name }, { it.id.getTag("code type") }, { it.id.getTag("valid") })
            .containsAll(expectedShortCodeCounter)

        assertThat(meterRegistry.get("submission.verify.code").tags("valid", "true", "code type", codeType).counter().count()).isEqualTo(1.0)
        assertThat(meterRegistry.get("submission.verify.code").tags("valid", "false", "code type", codeType).counter().count()).isEqualTo(1.0)
    }
}
