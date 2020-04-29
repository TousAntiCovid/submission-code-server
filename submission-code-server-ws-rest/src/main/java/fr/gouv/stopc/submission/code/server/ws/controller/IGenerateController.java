package fr.gouv.stopc.submission.code.server.ws.controller;

import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.ws.vo.GenerateRequestVo;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Produces;

@RestController
@RequestMapping(value = "${controller.path.prefix}")
@Produces(MediaType.APPLICATION_JSON_VALUE)
public interface IGenerateController {
	

	@PostMapping(value = "/generate")
	public ResponseEntity<GenerateResponseDto> register(@RequestBody(required = true) GenerateRequestVo generateRequestVo);
}
