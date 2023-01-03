package fr.gouv.stopc.submissioncode.service

import fr.gouv.stopc.submissioncode.api.model.Kpi
import fr.gouv.stopc.submissioncode.repository.JwtRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import kotlin.streams.toList

@Service
@Transactional(readOnly = true)
class KpiService(private val jwtRepository: JwtRepository) {

    fun computeKpi(startDate: LocalDate, endDate: LocalDate): List<Kpi> {
        return startDate.datesUntil(endDate.plusDays(1))
            .map {
                val startOfDay = it.atStartOfDay().toInstant(UTC)
                val endOfDay = it.atStartOfDay().plusDays(1).toInstant(UTC)
                Kpi(
                    date = it,
                    nbJwtUsed = jwtRepository.countByUsedBetween(
                        startOfDay,
                        endOfDay
                    )
                )
            }
            .toList()
    }
}
