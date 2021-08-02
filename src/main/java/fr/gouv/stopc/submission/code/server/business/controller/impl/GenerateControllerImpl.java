package fr.gouv.stopc.submission.code.server.business.controller.impl;

import fr.gouv.stopc.submission.code.server.business.controller.IGenerateController;
import fr.gouv.stopc.submission.code.server.business.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.business.dto.CodeSimpleDto;
import fr.gouv.stopc.submission.code.server.business.service.IGenerateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Slf4j
@Service
public class GenerateControllerImpl implements IGenerateController {

    private final IGenerateService generateService;

    @Inject
    public GenerateControllerImpl(IGenerateService generateService) {
        this.generateService = generateService;
    }

    @Override
    public ResponseEntity<CodeSimpleDto> generateShortCode() throws SubmissionCodeServerException {
        log.info("Trying to generate code with sequential method");

        return ResponseEntity.ok(
                this.generateService.generateShortCode()
        );
    }

}
