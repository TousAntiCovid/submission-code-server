package fr.gouv.stopc.submissioncode.repository

import fr.gouv.stopc.submissioncode.repository.model.JwtUsed
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface JwtRepository : PagingAndSortingRepository<JwtUsed, Long> {

    fun existsByJti(jti: String): Boolean

    @Modifying
    @Query(
        """
        delete from JwtUsed 
        where dateUse > :retentionDate
    """
    )
    fun deleteJwtUsedByDateUse(retentionDate: Instant)
}
