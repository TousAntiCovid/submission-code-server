package fr.gouv.stopc.submissioncode.repository

import fr.gouv.stopc.submissioncode.repository.model.SubmissionCode
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface SubmissionCodeRepository : PagingAndSortingRepository<SubmissionCode, Long> {

    fun findByCode(code: String): SubmissionCode?

    @Modifying
    @Query(
        """
            update SubmissionCode
            set used = true
              , dateUse = :now
            where code = :code
              and used = false
              and dateUse is null
              and dateAvailable <= :now and :now <= dateEndValidity
        """
    )
    fun verifyAndUse(code: String, now: Instant): Int

    @Query(
        """
            select count(s.id)
            from SubmissionCode s
            where s.type = :type
              and :start <= s.dateUse and s.dateUse < :endExclusive
        """
    )
    fun countByTypeAndUsedBetween(type: String, start: Instant, endExclusive: Instant): Long

    @Query(
        """
            select count(s.id)
            from SubmissionCode s
            where s.dateUse is null
              and s.type = :type
              and :start <= s.dateEndValidity and s.dateEndValidity < :endExclusive
        """
    )
    fun countByTypeAndExpired(type: String, start: Instant, endExclusive: Instant): Long

    @Query(
        """
            select count(s.id)
            from SubmissionCode s
            where s.type = :type
              and :start <= s.dateGeneration and s.dateGeneration < :endExclusive
        """
    )
    fun countByTypeNewlyGenerated(type: String, start: Instant, endExclusive: Instant): Long
}
