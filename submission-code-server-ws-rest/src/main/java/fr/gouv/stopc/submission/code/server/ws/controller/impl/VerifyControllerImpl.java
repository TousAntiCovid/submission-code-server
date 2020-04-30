package fr.gouv.stopc.submission.code.server.ws.controller.impl;


import fr.gouv.stopc.submission.code.server.ws.controller.IVerifyController;
import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.ws.dto.VerifyResponseDto;
import fr.gouv.stopc.submission.code.server.ws.vo.VerifyRequestVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.inject.Inject;
import java.util.List;

@Service
@Slf4j
public class VerifyControllerImpl implements IVerifyController {

    @Inject
    public VerifyControllerImpl() {
    // inject the different services here.
    }

    @Override
    public ResponseEntity reportContactHistory(VerifyRequestVo verifyRequestVo) {
        log.info("Receiving code : {} and type : {}", verifyRequestVo.getCode(), verifyRequestVo.getType());

        return ResponseEntity.ok(VerifyResponseDto.builder().build());
    }
}
