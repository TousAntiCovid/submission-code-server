package fr.gouv.stopc.submission.code.server.business.controller;

import fr.gouv.stopc.submission.code.server.api.GenerateCodeApi;
import fr.gouv.stopc.submission.code.server.business.model.CodeSimpleDto;
import fr.gouv.stopc.submission.code.server.business.service.GenerateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * VPN Control is made to access to this end-point. JWT or ApiKey is checked in
 * API Gateway.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1")
public class GenerateController implements GenerateCodeApi {

    private final GenerateService generateService;

    @Inject
    public GenerateController(GenerateService generateService) {
        this.generateService = generateService;
    }

    /**
     * Generate a new submission code. Codes are one-time use and have a validity
     * date
     *
     * @return ResponseEntity<CodeSimpleDto>
     */
    @Override
    public ResponseEntity<CodeSimpleDto> generate() throws Exception {
        log.info("Trying to generate code with sequential method");

        return ResponseEntity.ok(this.generateService.generateShortCode());
    }

    @Override
    public ResponseEntity<CodeSimpleDto> generateTest() {
        log.info("Try to generate a test code that is 12 characters long.");

        return ResponseEntity.ok(this.generateService.generateTestCode());
    }

}
