package fr.gouv.stopc.submissioncode.service

import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.JWTManager.Companion.givenJwt
import fr.gouv.stopc.submissioncode.test.MetricsManager.Companion.assertThatMetricCounterIncrement
import fr.gouv.stopc.submissioncode.test.When
import org.junit.jupiter.api.Test

@IntegrationTest
class MetricsServiceTest {

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

        assertThatMetricCounterIncrement("submission.verify.code", "valid", "true")
            .isEqualTo(1.0)
        assertThatMetricCounterIncrement("submission.verify.code", "valid", "false")
            .isEqualTo(2.0)
    }
}
