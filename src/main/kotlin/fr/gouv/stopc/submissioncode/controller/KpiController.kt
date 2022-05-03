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
import java.time.LocalDate
import java.time.Period

@RestController
@RequestMapping("/internal/api/v1")
class KpiController(val kpiService: KpiService) : KpiApi {

    override fun kpi(@DateTimeFormat(iso = DATE) fromDate: LocalDate, @DateTimeFormat(iso = DATE) toDate: LocalDate): ResponseEntity<List<Kpi>> {
        val validation = BindException(object {}, "params")
        if (Period.between(fromDate, toDate).days > 10) {
            validation.reject(
                "PERIOD_TOO_LARGE",
                "The period between 'fromDate' and 'toDate' must be less than 10 days"
            )
        }
        if (Period.between(fromDate, toDate).days < 0) {
            validation.reject("INCONSISTENT_BOUNDS", "The 'fromDate' should be before 'toDate'")
        }
        if (validation.hasErrors()) {
            throw validation
        }
        val kpis = kpiService.computeKpi(fromDate, toDate)
        return ResponseEntity.ok(kpis)
    }
}
