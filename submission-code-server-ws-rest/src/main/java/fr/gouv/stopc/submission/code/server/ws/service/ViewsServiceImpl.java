package fr.gouv.stopc.submission.code.server.ws.service;


import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import fr.gouv.stopc.submission.code.server.ws.dto.ViewLotInformationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Slf4j
@Service
public class ViewsServiceImpl {

    private final ISubmissionCodeService submissionCodeService;

    /**
     * Default constructor
     * @param submissionCodeService Spring-injection of the alphaNumericCodeService giving access to persistence in db.
     */
    @Inject
    public ViewsServiceImpl(ISubmissionCodeService submissionCodeService)
    {
        this.submissionCodeService = submissionCodeService;
    }

    public ViewLotInformationDto getLotInformation(long lotIdentifier) {
        final long numOfCodes = this.submissionCodeService.getNumberOfCodesForLotIdentifier(lotIdentifier);
        return ViewLotInformationDto.builder()
                .lotNumber(lotIdentifier)
                .numberOfCodes(numOfCodes)
                .build();
    }

}
