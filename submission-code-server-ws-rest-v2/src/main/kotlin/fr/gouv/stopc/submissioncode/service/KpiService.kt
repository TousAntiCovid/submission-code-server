package fr.gouv.stopc.submissioncode.service

import fr.gouv.stopc.submissioncode.api.model.Kpi
import fr.gouv.stopc.submissioncode.repository.SubmissionCodeRepository
import fr.gouv.stopc.submissioncode.repository.model.SubmissionCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import kotlin.streams.toList

@Service
@Transactional(readOnly = true)
class KpiService(private val submissionCodeRepository: SubmissionCodeRepository) {

    fun computeKpi(startDate: LocalDate, endDate: LocalDate): List<Kpi> {
        return startDate.datesUntil(endDate.plusDays(1))
            .map {
                val startOfDay = it.atStartOfDay().toInstant(UTC)
                val endOfDay = it.atStartOfDay().plusDays(1).toInstant(UTC)
                Kpi(
                    date = it,
                    nbShortCodesUsed = submissionCodeRepository.countByTypeAndUsedBetween(
                        SubmissionCode.Type.SHORT.dbValue,
                        startOfDay,
                        endOfDay
                    ),
                    nbLongCodesUsed = submissionCodeRepository.countByTypeAndUsedBetween(
                        SubmissionCode.Type.LONG.dbValue,
                        startOfDay,
                        endOfDay
                    ),
                    nbShortExpiredCodes = submissionCodeRepository.countByTypeAndExpired(
                        SubmissionCode.Type.SHORT.dbValue,
                        startOfDay,
                        endOfDay
                    ),
                    nbLongExpiredCodes = submissionCodeRepository.countByTypeAndExpired(
                        SubmissionCode.Type.LONG.dbValue,
                        startOfDay,
                        endOfDay
                    ),
                    nbShortCodesGenerated = submissionCodeRepository.countByTypeNewlyGenerated(
                        SubmissionCode.Type.SHORT.dbValue,
                        startOfDay,
                        endOfDay
                    )
                )
            }
            .toList()
    }

    data class SingleDayKpi(
        val date: LocalDate,
        val nbShortCodesUsed: Long,
        val nbLongCodesUsed: Long,
        val nbShortExpiredCodes: Long,
        val nbLongExpiredCodes: Long,
        val nbShortCodesGenerated: Long
    )
}
