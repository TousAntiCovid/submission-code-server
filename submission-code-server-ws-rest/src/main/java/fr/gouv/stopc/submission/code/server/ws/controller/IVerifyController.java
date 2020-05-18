package fr.gouv.stopc.submission.code.server.ws.controller;

import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.ws.vo.VerifyRequestVo;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.Produces;

@RestController
@RequestMapping(value = "${controller.path.prefix}")
@Produces(MediaType.APPLICATION_JSON_VALUE)
public interface IVerifyController {

    /**
     * Check the validity of a submission code (originally provided by the app).
     * This API must be protected and used only by a trusted back-end server over a secure private connection.
     * @param verifyRequestVo The code value to verify & The type of the provided code
     * @return
     */
    @GetMapping(value="/verify")
     ResponseEntity verifySubmissionCode(@ModelAttribute @Valid VerifyRequestVo verifyRequestVo) throws SubmissionCodeServerException;
}
