package fr.gouv.stopc.submissioncode.repository

import fr.gouv.stopc.submissioncode.repository.model.JwtUsed
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface JwtRepository : PagingAndSortingRepository<JwtUsed, Long> {

    fun existsByJti(jti: String): Boolean

    @Query(
        """
            select count(j.id)
            from JwtUsed j
            where j.dateUse >= :start and j.dateUse < :endExclusive
        """
    )
    fun countByUsedBetween(start: Instant, endExclusive: Instant): Long
}
