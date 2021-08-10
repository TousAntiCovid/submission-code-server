package fr.gouv.stopc.submission.code.server.business.controller;

import fr.gouv.stopc.submission.code.server.api.KpiApi;
import fr.gouv.stopc.submission.code.server.business.model.Kpi;
import fr.gouv.stopc.submission.code.server.business.service.KpiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Kpi generation REST controller definition
 * 
 * @author plant-stopcovid
 */
@RestController
@RequestMapping(value = "/internal/api/v1")
public class KpiController implements KpiApi {

    /**
     * The Kpi generation service
     */
    private KpiService kpiService;

    /**
     * Spring Injection constructor
     * 
     * @param kpiService the <code>IKpiService</code> bean instance to inject
     */
    public KpiController(KpiService kpiService) {
        this.kpiService = kpiService;
    }

    /**
     * Computes the Kpi on a period
     *
     * @param fromDate beginning date of the period
     * @param toDate   ending date of the period
     * @return the list of Kpi computed on this period (one per day)
     */
    @Override
    public ResponseEntity<List<Kpi>> kpi(LocalDate fromDate, LocalDate toDate) throws Exception {
        return ResponseEntity.ok(this.kpiService.generateKPI(fromDate, toDate));
    }
}
