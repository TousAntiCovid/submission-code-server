package fr.gouv.stopc.submission.code.server.business.controller;

import fr.gouv.stopc.submission.code.server.business.controller.exception.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.business.dto.ViewDto;
import fr.gouv.stopc.submission.code.server.business.service.ViewsService;
import fr.gouv.stopc.submission.code.server.business.vo.ViewVo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;

/**
 * VPN Control is made to access to this end-point. JWT or ApiKey is checked in
 * API Gateway.
 */
@RestController
@RequestMapping(value = "/api/v1/back-office")
public class ViewController {

    private final ViewsService viewsService;

    @Inject
    public ViewController(ViewsService viewsService) {
        this.viewsService = viewsService;
    }

    /**
     * Returns the information as number of codes
     *
     * @param lotIdentifier The lot identifier
     * @return ViewDto.LotInformation
     */
    @GetMapping(path = "/lots/{lotIdentifier}/information")
    public ResponseEntity<ViewDto.LotInformation> getLotInformation(@PathVariable long lotIdentifier)
            throws SubmissionCodeServerException {
        return ResponseEntity.ok(this.viewsService.getLotInformation(lotIdentifier));
    }

    /**
     * The method returns the information as number page, as number elements for
     * page and list of codes.
     *
     * @param lotIdentifier The lot identifier
     * @param page          Number of the researched page
     * @param elementByPage Number of elements by page
     * @return ViewDto.CodeValuesForPage
     */
    @GetMapping(path = "/lots/{lotIdentifier}/page/{page}/by/{elementByPage}")
    public ResponseEntity<ViewDto.CodeValuesForPage> getCodeValuesForPage(
            @PathVariable long lotIdentifier,
            @PathVariable int page,
            @PathVariable int elementByPage) throws SubmissionCodeServerException {
        return ResponseEntity.ok(this.viewsService.getViewLotCodeDetailListFor(page, elementByPage, lotIdentifier));
    }

    /**
     * The method launch the generation code with the dates from and to and number
     * of codes for day.
     *
     * @param codeGenerationRequestBody Object representing the parameters of the
     *                                  research
     * @return ViewDto.CodeGenerationRequest defining if the request has been
     *         successfully or not successfully submitted.
     */
    @PostMapping(path = "/codes/generate/request")
    public ResponseEntity<ViewDto.CodeGenerationRequest> postCodeGenerationRequest(
            @Valid @RequestBody ViewVo.CodeGenerationRequestBody codeGenerationRequestBody)
            throws SubmissionCodeServerException {
        final ViewDto.CodeGenerationRequest codeGenerationRequest = this.viewsService
                .launchGenerationWith(codeGenerationRequestBody);

        return ResponseEntity.ok(codeGenerationRequest);
    }
}
