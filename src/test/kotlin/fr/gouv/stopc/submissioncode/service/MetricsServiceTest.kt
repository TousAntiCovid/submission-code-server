package fr.gouv.stopc.submissioncode.service

import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.JWTManager.Companion.givenJwt
import fr.gouv.stopc.submissioncode.test.PostgresqlManager
import fr.gouv.stopc.submissioncode.test.When
import io.micrometer.core.instrument.MeterRegistry
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
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
    fun increment_codes_counters(validCode: String, invalidCode: String, codeType: String) {

        When()
            .get("/api/v1/verify?code={code}", validCode)

        When()
            .get("/api/v1/verify?code={code}", invalidCode)

        val expectedShortCodeCounter = listOf(tuple("submission.verify.code", codeType, "true"), tuple("submission.verify.code", codeType, "false"))

        assertThat(meterRegistry.meters)
            .extracting({ it.id.name }, { it.id.getTag("code type") }, { it.id.getTag("valid") })
            .containsAll(expectedShortCodeCounter)

        assertThat(meterRegistry.get("submission.verify.code").tags("valid", "true", "code type", codeType).counter().count()).isEqualTo(1.0)
        assertThat(meterRegistry.get("submission.verify.code").tags("valid", "false", "code type", codeType).counter().count()).isEqualTo(1.0)
    }

    @Test
    fun increment_jwt_counters() {

        val validJwt = givenJwt()
        val invalidJwt1 = givenJwt(jti = null)
        val invalidJwt2 = givenJwt(kid = null)

        When()
            .get("/api/v1/verify?code={jwt}", validJwt)

        When()
            .get("/api/v1/verify?code={jwt}", invalidJwt1)

        When()
            .get("/api/v1/verify?code={jwt}", invalidJwt2)

        val expectedShortCodeCounter = listOf(tuple("submission.verify.jwt", "true"), tuple("submission.verify.jwt", "false"))

        assertThat(meterRegistry.meters)
            .extracting({ it.id.name }, { it.id.getTag("valid") })
            .containsAll(expectedShortCodeCounter)

        assertThat(meterRegistry.get("submission.verify.jwt").tag("valid", "true").counter().count()).isEqualTo(1.0)
        assertThat(meterRegistry.get("submission.verify.jwt").tag("valid", "false").counter().count()).isEqualTo(2.0)
    }
}
