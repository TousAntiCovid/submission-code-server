package fr.gouv.stopc.submission.code.server.business.controller;

import fr.gouv.stopc.submission.code.server.api.VerifyCodeApi;
import fr.gouv.stopc.submission.code.server.business.model.VerifyDto;
import fr.gouv.stopc.submission.code.server.business.service.VerifyService;
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
public class VerifyController implements VerifyCodeApi {

    private VerifyService verifyServiceImpl;

    @Inject
    public VerifyController(VerifyService verifyServiceImpl) {
        this.verifyServiceImpl = verifyServiceImpl;
    }

    /**
     * Check the validity of a submission code (originally provided by the app).
     * This API must be protected and used only by a trusted back-end server over a
     * secure private connection.
     *
     * @param code should respect regexp ([a-zA-Z0-9]{6}) |
     *             ([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8}) | ([a-zA-Z0-9]{12})
     *             (required) The code value to verify
     * @return VerifyDto A boolean representing the result status
     */
    @Override
    public ResponseEntity<VerifyDto> verify(String code) {
        log.info("Receiving code : {}", code);
        boolean result = verifyServiceImpl.verifyCode(code);
        return ResponseEntity.ok(VerifyDto.builder().valid(result).build());
    }

}
