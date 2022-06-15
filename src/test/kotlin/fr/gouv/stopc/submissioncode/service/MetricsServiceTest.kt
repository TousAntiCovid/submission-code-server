package fr.gouv.stopc.submissioncode.service

import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.JWTManager.Companion.givenJwt
import fr.gouv.stopc.submissioncode.test.PostgresqlManager.Companion.givenTableSubmissionCodeContainsCode
import fr.gouv.stopc.submissioncode.test.When
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.assertj.core.api.AbstractDoubleAssert
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

@IntegrationTest
class MetricsServiceTest(@Autowired val meterRegistry: MeterRegistry) {

    private val metricsSnapshot = mutableMapOf<Meter.Id, Double>()

    @BeforeEach
    fun `take kpi snaptshot`() {
        meterRegistry.meters
            .filterIsInstance<Counter>()
            .associateTo(metricsSnapshot) { it.id to it.count() }
    }

    private fun assertThatCounterIncrement(name: String, vararg tags: String): AbstractDoubleAssert<*> {
        val snapshotValue = metricsSnapshot
            .filter { it.key.name == name }
            .filter { Tags.of(*tags).toList().containsAll(it.key.tags) }
            .firstNotNullOf { it.value }
        val actualValue = meterRegistry.get(name).tags(*tags).counter().count()
        return assertThat(actualValue - snapshotValue).describedAs("Counter value $name${Tags.of(*tags)}")
    }

    @BeforeEach
    fun `given some valid codes exists`() {
        givenTableSubmissionCodeContainsCode("1", "0000000a-0000-0000-0000-000000000000")
        givenTableSubmissionCodeContainsCode("3", "BBBBBBBBBBBB")
        givenTableSubmissionCodeContainsCode("2", "AAAAAA")
        val expiredInstant = Instant.now().minusSeconds(1)
        givenTableSubmissionCodeContainsCode("1", "0000000a-0000-0000-0000-000000000001", expiresOn = expiredInstant)
        givenTableSubmissionCodeContainsCode("3", "BBBBBBBBBBBA", expiresOn = expiredInstant)
        givenTableSubmissionCodeContainsCode("2", "AAAAAB", expiresOn = expiredInstant)
    }

    @ParameterizedTest
    @CsvSource(
        "AAAAAA, AAAAAB, SHORT",
        "BBBBBBBBBBBB, BBBBBBBBBBBA, TEST",
        "0000000a-0000-0000-0000-000000000000, 0000000a-0000-0000-0000-000000000001, LONG"
    )
    fun can_increment_codes_counters(validCode: String, invalidCode: String, codeType: String) {

        When()
            .get("/api/v1/verify?code={code}", validCode)

        When()
            .get("/api/v1/verify?code={code}", invalidCode)

        assertThatCounterIncrement("submission.verify.code", "valid", "true", "code type", codeType)
            .isEqualTo(1.0)
        assertThatCounterIncrement("submission.verify.code", "valid", "false", "code type", codeType)
            .isEqualTo(1.0)
    }

    @Test
    fun can_increment_jwt_counters() {

        val validJwt = givenJwt()
        val invalidJwt1 = givenJwt(jti = null)
        val invalidJwt2 = givenJwt(kid = null)

        When()
            .get("/api/v1/verify?code={jwt}", validJwt)

        When()
            .get("/api/v1/verify?code={jwt}", invalidJwt1)

        When()
            .get("/api/v1/verify?code={jwt}", invalidJwt2)

        assertThatCounterIncrement("submission.verify.code", "valid", "true", "code type", "JWT")
            .isEqualTo(1.0)
        assertThatCounterIncrement("submission.verify.code", "valid", "false", "code type", "JWT")
            .isEqualTo(2.0)
    }
}
