package fr.gouv.stopc.submission.code.server.ws.controller.impl;



import fr.gouv.stopc.submission.code.server.ws.controller.IGenerateController;
import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.ws.vo.GenerateRequestVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.inject.Inject;


@Slf4j
@Service
public class GenerateControllerImpl implements IGenerateController {

	@Inject
	public GenerateControllerImpl() {

	}


	@Override
	public ResponseEntity<GenerateResponseDto> createSubmissionCode(GenerateRequestVo generateRequestVo) {
		log.info("Receiving code : {} and type : {}", generateRequestVo.getCode(), generateRequestVo.getType().toString());
		return ResponseEntity.ok(GenerateResponseDto.builder().build());
	}
}