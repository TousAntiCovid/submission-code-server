package fr.gouv.stopc.submission.code.server.ws.controller;

import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.ws.dto.ViewDto;
import fr.gouv.stopc.submission.code.server.ws.vo.ViewVo;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.Produces;

@RestController
@RequestMapping(value = "${controller.path.prefix}/views")
@Produces(MediaType.APPLICATION_JSON_VALUE)
public interface IViewController {


    @GetMapping(path="/lots/{lotIdentifier}/information")
    ResponseEntity<ViewDto.LotInformation> getLotInformation(@PathVariable long lotIdentifier) throws SubmissionCodeServerException;

    @GetMapping(path="/lots/{lotIdentifier}/page/{page}/by/{elementByPage}")
    ResponseEntity<ViewDto.CodeValuesForPage> getCodeValuesForPage(
            @PathVariable long lotIdentifier,
            @PathVariable int page,
            @PathVariable int elementByPage
    ) throws SubmissionCodeServerException;

    @PostMapping(path="/codes/generation/request")
    ResponseEntity<ViewDto.CodeGenerationRequest> postCodeGenerationRequest(
            @Valid @RequestBody ViewVo.CodeGenerationRequestBody cgrpr
    ) throws SubmissionCodeServerException;
}
