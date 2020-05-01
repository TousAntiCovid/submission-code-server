package fr.gouv.stopc.submission.code.server.ws.controller;

import fr.gouv.stopc.submission.code.server.ws.vo.VerifyRequestVo;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Produces;

@RestController
@RequestMapping(value = "${controller.path.prefix}")
@Produces(MediaType.APPLICATION_JSON_VALUE)
public interface IVerifyController {

    @GetMapping(value="/verify")
    public ResponseEntity verifySubmissionCode(@RequestBody(required=true) VerifyRequestVo verifyRequestVo);
}
