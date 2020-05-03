package fr.gouv.stopc.submission.code.server.ws.controller.impl;

import fr.gouv.stopc.submission.code.server.ws.controller.IGenerateController;
import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.ws.service.IGenerateService;
import fr.gouv.stopc.submission.code.server.ws.vo.GenerateRequestVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.activation.UnsupportedDataTypeException;
import javax.inject.Inject;
import java.util.List;



@Slf4j
@Service
public class GenerateControllerImpl implements IGenerateController {

	private final IGenerateService generateService;

	@Inject
	public GenerateControllerImpl(IGenerateService generateService){
		this.generateService = generateService;
	}

	@Override
	public ResponseEntity<List<GenerateResponseDto>> generateCode(GenerateRequestVo generateRequestVo) {
		try {

			log.info("Trying to generate code with sequential method for {}", generateRequestVo);
			return ResponseEntity.ok(this.generateService.generateCodeFromRequest(generateRequestVo));

		} catch (UnsupportedDataTypeException e) {

			log.error("Unprocessable data trying generating code {} \n {}", generateRequestVo, e);
			//TODO: define strategy in case of the method is failing.
			return ResponseEntity.badRequest().body(null);

		}
	}

	public ResponseEntity<List<GenerateResponseDto>> generateCodeBulk(GenerateRequestVo generateRequestVo) {
		log.info("Trying to generate code with bulk method for {}", generateRequestVo);
		return ResponseEntity.ok(this.generateService.generateUUIDv4CodesBulk());
	}


	public void requestErrorHandling(Exception e) {
		//TODO: handle error here ! validation entity for example.
		log.error("requestErrorHandling logs : {}", e.toString());
	}



}