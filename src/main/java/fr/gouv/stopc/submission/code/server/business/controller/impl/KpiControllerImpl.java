package fr.gouv.stopc.submission.code.server.business.controller.impl;

import fr.gouv.stopc.submission.code.server.business.controller.IKpiController;
import fr.gouv.stopc.submission.code.server.business.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.business.service.IKpiService;
import fr.gouv.stopc.submission.code.server.business.vo.SubmissionCodeServerKpi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Default implementation of the Kpi generation REST Controller
 * 
 * @author plant-stopcovid
 */
@Service
public class KpiControllerImpl implements IKpiController {

    /**
     * The Kpi generation service
     */
    private IKpiService ikpiService;

    /**
     * Spring Injection constructor
     * 
     * @param ikpiService the <code>IKpiService</code> bean instance to inject
     */
    public KpiControllerImpl(IKpiService ikpiService) {
        this.ikpiService = ikpiService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity generateKpi(LocalDate fromDate, LocalDate toDate) {
        List<SubmissionCodeServerKpi> result;
        try {
            result = this.ikpiService.generateKPI(fromDate, toDate);
        } catch (SubmissionCodeServerException s) {
            return ResponseEntity.badRequest().body(s.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}
