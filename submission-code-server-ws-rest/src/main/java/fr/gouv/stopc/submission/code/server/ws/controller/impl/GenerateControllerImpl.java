package fr.gouv.stopc.submission.code.server.ws.controller.impl;



import fr.gouv.stopc.submission.code.server.ws.controller.IGenerateController;
import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.ws.service.IGenerateService;
import fr.gouv.stopc.submission.code.server.ws.vo.GenerateRequestVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.activation.UnsupportedDataTypeException;
import javax.inject.Inject;
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
			e.printStackTrace();
		}
		return ResponseEntity.badRequest().body(null);
	}

	/**
	 * Exception should be handle here
	 */
	@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Something went wrong", code = HttpStatus.CONFLICT)
	@ExceptionHandler(Exception.class)
	public void conflict() {
		//TODO: handle error here ! validation entity for example.
		log.error("20200501 -- Request raised a DataIntegrityViolationException");
	}



}