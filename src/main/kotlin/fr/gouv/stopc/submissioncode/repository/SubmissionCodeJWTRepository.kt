package fr.gouv.stopc.submissioncode.repository

import fr.gouv.stopc.submissioncode.repository.model.JWT
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface SubmissionCodeJWTRepository : PagingAndSortingRepository<JWT, Long> {

    fun findByJti(jti: String): JWT?
}
