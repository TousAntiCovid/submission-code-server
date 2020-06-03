package fr.gouv.stopc.submission.code.server.ws.controller.impl;

import fr.gouv.stopc.submission.code.server.ws.controller.IKPIGenerateController;
import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.ws.service.IKPIService;
import fr.gouv.stopc.submission.code.server.ws.vo.SubmissionCodeServerKpi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class KPIGenerateController implements IKPIGenerateController {

    private IKPIService ikpiService;

    @Inject
    public KPIGenerateController (IKPIService ikpiService){
        this.ikpiService=ikpiService;

    }
    @Override
    public ResponseEntity generateKPI(LocalDate fromDate, LocalDate toDate) {
        List<SubmissionCodeServerKpi> result;
        try{
            result = ikpiService.generateKPI(fromDate, toDate);
        }catch(SubmissionCodeServerException s){
            return ResponseEntity.badRequest().body(s.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}
