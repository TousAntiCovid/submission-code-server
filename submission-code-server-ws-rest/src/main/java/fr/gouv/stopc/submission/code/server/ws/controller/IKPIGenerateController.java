package fr.gouv.stopc.submission.code.server.ws.controller;

import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.ws.vo.SubmissionCodeServerKpi;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Produces;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "${controller.path.prefix}")
@Produces(MediaType.APPLICATION_JSON_VALUE)
public interface IKPIGenerateController {

    @GetMapping(value = "/generate/kpi")
    ResponseEntity<List<SubmissionCodeServerKpi>> generateKPI(@RequestParam(name = "fromDate")@DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fromDate, @RequestParam(name = "toDate") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate toDate) throws SubmissionCodeServerException;

}
