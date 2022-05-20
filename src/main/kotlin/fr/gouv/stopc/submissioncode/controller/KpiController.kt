package fr.gouv.stopc.submissioncode.controller

import fr.gouv.stopc.submissioncode.api.KpiApi
import fr.gouv.stopc.submissioncode.api.model.Kpi
import fr.gouv.stopc.submissioncode.service.KpiService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO.DATE
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.LocalDate

@RestController
@RequestMapping("/internal/api/v1")
class KpiController(val kpiService: KpiService) : KpiApi {

    override fun kpi(@DateTimeFormat(iso = DATE) fromDate: LocalDate, @DateTimeFormat(iso = DATE) toDate: LocalDate): ResponseEntity<List<Kpi>> {
        val validation = BindException(object {}, "params")
        val period = Duration.between(fromDate.atStartOfDay(), toDate.atStartOfDay())
        if (period.toDays() < 0) {
            validation.reject("INCONSISTENT_BOUNDS", "The 'fromDate' should be before 'toDate'")
        }
        if (period.toDays() > 100) {
            validation.reject(
                "PERIOD_TOO_LARGE",
                "The period between 'fromDate' and 'toDate' must be less than 100 days"
            )
        }
        if (validation.hasErrors()) {
            throw validation
        }
        val kpis = kpiService.computeKpi(fromDate, toDate)
        return ResponseEntity.ok(kpis)
    }
}
