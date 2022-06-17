package fr.gouv.stopc.submissioncode.service

import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.JWTManager.Companion.givenJwt
import fr.gouv.stopc.submissioncode.test.MetricsManager.Companion.getCodeVerificationCount
import fr.gouv.stopc.submissioncode.test.PostgresqlManager.Companion.givenTableSubmissionCodeContainsCode
import fr.gouv.stopc.submissioncode.test.When
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Instant

@IntegrationTest
class MetricsServiceTest {

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

        val invalidCodeVerificationCountBefore = getCodeVerificationCount("submission.verify.code", "valid", "true", "code type", codeType)
        val validCodeVerificationCountBefore = getCodeVerificationCount("submission.verify.code", "valid", "false", "code type", codeType)

        When()
            .get("/api/v1/verify?code={code}", validCode)

        When()
            .get("/api/v1/verify?code={code}", invalidCode)

        assertThat(getCodeVerificationCount("submission.verify.code", "valid", "true", "code type", codeType))
            .describedAs("Valid code verification count for type $codeType should have been incremented (was $validCodeVerificationCountBefore) ")
            .isEqualTo(validCodeVerificationCountBefore + 1)
        assertThat(getCodeVerificationCount("submission.verify.code", "valid", "false", "code type", codeType))
            .describedAs("Invalid code verification count for type $codeType should have been incremented (was $invalidCodeVerificationCountBefore) ")
            .isEqualTo(invalidCodeVerificationCountBefore + 1)
    }

    @Test
    fun can_increment_jwt_counters() {

        val validJwt = givenJwt()
        val invalidJwt1 = givenJwt(jti = null)
        val invalidJwt2 = givenJwt(kid = null)

        val validJwtVerificationCountBefore = getCodeVerificationCount("submission.verify.code", "valid", "true", "code type", "JWT")
        val invalidJwtVerificationCountBefore = getCodeVerificationCount("submission.verify.code", "valid", "false", "code type", "JWT")

        When()
            .get("/api/v1/verify?code={jwt}", validJwt)

        When()
            .get("/api/v1/verify?code={jwt}", invalidJwt1)

        When()
            .get("/api/v1/verify?code={jwt}", invalidJwt2)

        assertThat(getCodeVerificationCount("submission.verify.code", "valid", "true", "code type", "JWT"))
            .describedAs("Valid code verification count for type JWT should have been incremented (was $validJwtVerificationCountBefore) ")
            .isEqualTo(validJwtVerificationCountBefore + 1)
        assertThat(getCodeVerificationCount("submission.verify.code", "valid", "false", "code type", "JWT"))
            .describedAs("Invalid code verification count for type JWT should have been incremented (was $invalidJwtVerificationCountBefore) ")
            .isEqualTo(invalidJwtVerificationCountBefore + 2)
    }
}
