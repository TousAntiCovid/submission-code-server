package fr.gouv.stopc.submission.code.server.ws.controller.impl;

import fr.gouv.stopc.submission.code.server.ws.controller.IViewController;
import fr.gouv.stopc.submission.code.server.ws.dto.ViewLotInformationDto;
import fr.gouv.stopc.submission.code.server.ws.service.ViewsServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class ViewControllerImpl implements IViewController {

    private final ViewsServiceImpl vs;

    @Inject
    public ViewControllerImpl(ViewsServiceImpl viewsService) {
        this.vs = viewsService;
    }

    @Override
    public ResponseEntity<ViewLotInformationDto> getLotInformation(long lotIdentifier) {
        return ResponseEntity.ok(
                this.vs.getLotInformation(lotIdentifier)
        );
    }
}
