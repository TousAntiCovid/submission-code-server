package fr.gouv.stopc.submissioncode.repository

import fr.gouv.stopc.submissioncode.repository.model.JwtUsed
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface JwtRepository : PagingAndSortingRepository<JwtUsed, Long> {

    fun existsByJti(jti: String): Boolean
}
