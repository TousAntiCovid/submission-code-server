package fr.gouv.stopc.submission.code.server.ws.controller.impl;



import fr.gouv.stopc.submission.code.server.ws.controller.IGenerateController;
import fr.gouv.stopc.submission.code.server.ws.vo.GenerateRequestVo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.inject.Inject;



@Service
public class GenerateControllerImpl implements IGenerateController {

	@Inject
	public GenerateControllerImpl() {

	}


	@Override
	public ResponseEntity register(GenerateRequestVo generateRequestVo) {
		return null;
	}
}