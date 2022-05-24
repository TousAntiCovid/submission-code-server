package fr.gouv.stopc.submissioncode.repository

import fr.gouv.stopc.submissioncode.repository.model.JtiUsed
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface SubmissionCodeJWTRepository : PagingAndSortingRepository<JtiUsed, Long> {

    fun existsByJti(jti: String): Boolean
}
