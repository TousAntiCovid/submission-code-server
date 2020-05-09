package fr.gouv.stopc.submission.code.server.ws.controller.impl;

import fr.gouv.stopc.submission.code.server.ws.controller.IViewController;
import fr.gouv.stopc.submission.code.server.ws.dto.ViewDto;
import fr.gouv.stopc.submission.code.server.ws.service.ViewsServiceImpl;
import fr.gouv.stopc.submission.code.server.ws.vo.ViewVo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.validation.Valid;

@Service
public class ViewControllerImpl implements IViewController {

    private final ViewsServiceImpl vs;

    @Inject
    public ViewControllerImpl(ViewsServiceImpl viewsService) {
        this.vs = viewsService;
    }

    @Override
    public ResponseEntity<ViewDto.LotInformation> getLotInformation(@PathVariable long lotIdentifier)
    {
        return ResponseEntity.ok(
                this.vs.getLotInformation(lotIdentifier)
        );
    }

    @Override
    public ResponseEntity<ViewDto.CodeValuesForPage> getCodeValuesForPage(
            long lotIdentifier, int page, int elementByPage
    )
    {
        return ResponseEntity.ok(
                this.vs.getViewLotCodeDetailListFor(page, elementByPage, lotIdentifier)
        );
    }

    @Override
    public ResponseEntity<ViewDto.CodeGenerationRequest> postCodeGenerationRequest (
            @Valid @RequestBody ViewVo.CodeGenerationRequestBody codeGenerationRequestBody
    )
    {
        final ViewDto.CodeGenerationRequest codeGenerationRequest = this.vs.launchGenerationWith(codeGenerationRequestBody);
        return ResponseEntity.ok(codeGenerationRequest);

    }
}
