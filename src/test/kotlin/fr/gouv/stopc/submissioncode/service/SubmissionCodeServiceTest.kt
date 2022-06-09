package fr.gouv.stopc.submissioncode.service

import fr.gouv.stopc.submissioncode.repository.JwtRepository
import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.PostgresqlManager.Companion.givenTableJwtUsedContainsJwt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

@IntegrationTest
class SubmissionCodeServiceTest(@Autowired val submissionCodeService: SubmissionCodeService, @Autowired val jwtRepository: JwtRepository) {

    @BeforeEach
    fun givenJwtUsedExists() {
        val now = Instant.now()
        givenTableJwtUsedContainsJwt(jti = "jti1", dateUse = now)
        givenTableJwtUsedContainsJwt(jti = "jti2", dateUse = now.plus(5, DAYS))
        givenTableJwtUsedContainsJwt(jti = "jti3", dateUse = now.plus(15, DAYS))
        givenTableJwtUsedContainsJwt(jti = "jti4", dateUse = now.plus(30, DAYS))
        givenTableJwtUsedContainsJwt(jti = "jti5", dateUse = now.plus(31, DAYS))
        givenTableJwtUsedContainsJwt(jti = "jti6", dateUse = now.plus(40, DAYS))
    }

    @Test
    fun delete_old_jwt() {

        submissionCodeService.purgeOldJwtUsed()

        val listOfJwt = jwtRepository.findAll()

        assertThat(listOfJwt.count()).isEqualTo(4)
        assert(listOfJwt.map { it.jti }.containsAll(listOf("jti1", "jti2", "jti3", "jti4")))
    }
}
