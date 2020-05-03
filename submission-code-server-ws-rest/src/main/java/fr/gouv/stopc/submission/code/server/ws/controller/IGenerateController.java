package fr.gouv.stopc.submission.code.server.ws.controller;

import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.ws.vo.GenerateRequestVo;
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

    /**
     * Generate a new submission code. Codes are one-time use and have a validity date
     * @param generateRequestVo with type Available values : UUIDv4, 6-alphanum
     * @return
     */
    @GetMapping(value = "/generate")
    ResponseEntity<List<GenerateResponseDto>> generateCode(@Valid @RequestBody GenerateRequestVo generateRequestVo);

    /**
     * TODO: Remove endpoint if bulk error handling is not made.
     * @param generateRequestVo
     * @return
     */
    @GetMapping(value = "/generate-bulk")
    ResponseEntity<List<GenerateResponseDto>> generateCodeBulk(GenerateRequestVo generateRequestVo);


    /**
     * Exception should be handle here
     */
    @ExceptionHandler(Exception.class)
    void requestErrorHandling(Exception e);
}
