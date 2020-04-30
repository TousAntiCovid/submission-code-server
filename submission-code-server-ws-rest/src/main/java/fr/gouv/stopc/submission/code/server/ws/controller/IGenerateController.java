package fr.gouv.stopc.submission.code.server.ws.controller;

import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.ws.vo.GenerateRequestVo;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Produces;

@RestController
@RequestMapping(value = "${controller.path.prefix}")
@Produces(MediaType.APPLICATION_JSON_VALUE)
public interface IGenerateController {
	

	@GetMapping(value = "/generate")
	public ResponseEntity<GenerateResponseDto> createSubmissionCode(@RequestBody(required = true) GenerateRequestVo generateRequestVo);
}
