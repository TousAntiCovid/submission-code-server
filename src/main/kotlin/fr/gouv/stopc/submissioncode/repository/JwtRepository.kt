package fr.gouv.stopc.submissioncode.repository

import fr.gouv.stopc.submissioncode.repository.model.JwtUsed
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface JwtRepository : JpaRepository<JwtUsed, Long> {

    /**
     * @return 1 when the JWT is successfully stored as <em>used</em>
     */
    @Modifying
    @Query(
        nativeQuery = true,
        value = """
            insert into jwt_used(jti, date_use)
            values (:jti, :now)
            on conflict do nothing
        """
    )
    fun saveUsedJti(jti: String, now: Instant): Int

    @Query(
        """
            select count(j.id)
            from JwtUsed j
            where j.dateUse >= :start and j.dateUse < :endExclusive
        """
    )
    fun countByUsedBetween(start: Instant, endExclusive: Instant): Long
}
