package fr.gouv.stopc.submission.code.server.ws.controller;

import fr.gouv.stopc.submission.code.server.ws.vo.RequestCsvVo;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Produces;

@RestController
@RequestMapping(value = "${controller.path.prefix}")
@Produces(MediaType.APPLICATION_JSON_VALUE)
public interface ICsvController {

    @GetMapping(value = "export", produces = "text/csv")
    public ResponseEntity readCsvByLot(@RequestBody RequestCsvVo requestCsvVo);
}
