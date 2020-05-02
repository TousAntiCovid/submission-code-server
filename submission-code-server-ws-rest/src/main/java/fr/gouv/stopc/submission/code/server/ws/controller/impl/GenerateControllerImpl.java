package fr.gouv.stopc.submission.code.server.ws.controller.impl;

import fr.gouv.stopc.submission.code.server.ws.controller.IGenerateController;
import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.ws.service.IGenerateService;
import fr.gouv.stopc.submission.code.server.ws.vo.GenerateRequestVo;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.internal.Errors;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import javax.activation.UnsupportedDataTypeException;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
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
		log.info("Receiving and type : {}", generateRequestVo.getType());
		try {
			return ResponseEntity.ok(this.generateService.generateCode(generateRequestVo));
		} catch (UnsupportedDataTypeException e) {
			//TODO: handle error here ! validation entity for example.
			e.printStackTrace();
		}
		return ResponseEntity.badRequest().body(null);
	}

	public ResponseEntity<List<GenerateResponseDto>> generateCodeBulk(GenerateRequestVo generateRequestVo) {
		log.info("Receiving and type : {}", generateRequestVo.getType());
		return ResponseEntity.ok(this.generateService.generateCodeBulk());
	}


	public void requestErrorHandling(Exception e) {
		//TODO: handle error here ! validation entity for example.
		log.error("requestErrorHandling logs : {}", e.toString());
	}



}