package fr.gouv.stopc.submission.code.server.ws.controller;

import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.ws.vo.GenerateRequestVo;
import org.modelmapper.internal.Errors;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.Produces;
import java.util.List;

@RestController
@RequestMapping(value = "${controller.path.prefix}")
@Produces(MediaType.APPLICATION_JSON_VALUE)
public interface IGenerateController {

    @GetMapping(value = "/generate")
    ResponseEntity<List<GenerateResponseDto>> generateCode(@Valid @RequestBody GenerateRequestVo generateRequestVo);

    @GetMapping(value = "/generate-bulk")
    ResponseEntity<List<GenerateResponseDto>> generateCodeBulk(GenerateRequestVo generateRequestVo);


    /**
     * Exception should be handle here
     */
    @ExceptionHandler(Exception.class)
    void requestErrorHandling(Exception e);
}
