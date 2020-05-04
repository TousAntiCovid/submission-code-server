package fr.gouv.stopc.submission.code.server.ws.service;


import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;
import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import fr.gouv.stopc.submission.code.server.ws.dto.ViewLotCodeDetailDto;
import fr.gouv.stopc.submission.code.server.ws.dto.ViewLotCodeDetailPageDto;
import fr.gouv.stopc.submission.code.server.ws.dto.ViewLotInformationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.stream.Collectors;

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

    public ViewLotCodeDetailPageDto getViewLotCodeDetailListFor(int page, int elementByPage, long lotIdentifier) {
        final Page<SubmissionCode> submissionCodesPage = this.submissionCodeService.getSubmissionCodesFor(
                lotIdentifier,
                page - 1,
                elementByPage
        );

        return ViewLotCodeDetailPageDto.builder()
                .actualPage(submissionCodesPage.getNumber() + 1)
                    .lastPage(submissionCodesPage.getTotalPages())
                .maxByPage(submissionCodesPage.getNumberOfElements())
                .lot(lotIdentifier)
                .codes(
                        submissionCodesPage.toList().stream()
                                .map(sc -> ViewLotCodeDetailDto.builder()
                                        .code(sc.getCode())
                                        .build()
                                )
                                .collect(Collectors.toList())
                )
                .build();
    }
}
