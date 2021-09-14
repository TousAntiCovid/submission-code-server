package fr.gouv.stopc.submission.code.server.business.service;

import fr.gouv.stopc.submission.code.server.business.controller.exception.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.business.dto.ViewDto;
import fr.gouv.stopc.submission.code.server.business.vo.ViewVo;
import fr.gouv.stopc.submission.code.server.data.entity.Lot;
import fr.gouv.stopc.submission.code.server.data.entity.SubmissionCode;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Service
public class ViewsService {

    private final SubmissionCodeService submissionCodeService;

    private final FileService fileExportService;

    /**
     * Default constructor
     * 
     * @param submissionCodeService Spring-injection of the shortCodeService giving
     *                              access to persistence in db.
     */
    @Inject
    public ViewsService(SubmissionCodeService submissionCodeService,
            FileService fileExportService) {
        this.submissionCodeService = submissionCodeService;
        this.fileExportService = fileExportService;
    }

    /**
     * The method returns the information as number of codes .
     *
     * @param lotIdentifier
     * @return ViewDto.LotInformation
     * @throws SubmissionCodeServerException
     */
    public ViewDto.LotInformation getLotInformation(long lotIdentifier) throws SubmissionCodeServerException {
        final Long numOfCodes = this.submissionCodeService
                .getNumberOfCodesForLotIdentifier(lotIdentifier);

        if (numOfCodes == null) {
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.DB_NO_RECORD_FOR_LOT_IDENTIFIER_ERROR
            );
        }

        return ViewDto.LotInformation.builder()
                .lotIdentifier(lotIdentifier)
                .numberOfCodes(numOfCodes)
                .build();
    }

    /**
     * The method returns the information as number page, as number elements for
     * page and list of codes.
     *
     * @param page
     * @param elementByPage
     * @param lotIdentifier
     * @return
     * @throws SubmissionCodeServerException
     */
    public ViewDto.CodeValuesForPage getViewLotCodeDetailListFor(
            int page,
            int elementByPage,
            long lotIdentifier)
            throws SubmissionCodeServerException {
        final Page<SubmissionCode> submissionCodesPage = this.submissionCodeService.getSubmissionCodesFor(
                lotIdentifier,
                page - 1,
                elementByPage
        );

        if (submissionCodesPage == null ||
                submissionCodesPage.getTotalElements() < 1 ||
                submissionCodesPage.getTotalPages() < 1) {
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.DB_INVALID_PARAMETERS_ERROR
            );
        }

        return ViewDto.CodeValuesForPage.builder()
                .actualPage(submissionCodesPage.getNumber() + 1)
                .lastPage(submissionCodesPage.getTotalPages())
                .maxByPage(submissionCodesPage.getNumberOfElements())
                .lot(lotIdentifier)
                .codes(
                        submissionCodesPage.toList().stream()
                                .map(
                                        sc -> ViewDto.CodeDetail.builder()
                                                .code(sc.getCode())
                                                .build()
                                )
                                .collect(Collectors.toList())
                )
                .build();
    }

    /**
     * The method launch the generation code with the dates from and to and number
     * of codes for day.
     *
     * @return ViewDto.CodeGenerationRequest defining if the request has been
     *         successfully or not successfully submitted.
     */
    public ViewDto.CodeGenerationRequest launchGenerationWith(
            ViewVo.CodeGenerationRequestBody codeGenerationRequestBody) throws SubmissionCodeServerException {

        @NotNull
        final long codePerDay = codeGenerationRequestBody.getDailyAmount();
        @NotNull
        final OffsetDateTime from = codeGenerationRequestBody.getFrom();
        @NotNull
        OffsetDateTime to = codeGenerationRequestBody.getTo();

        Lot newLot = new Lot();
        newLot.setNumberOfCodes(codePerDay);
        newLot.setDateExecution(OffsetDateTime.now());
        this.fileExportService.zipExportAsync(
                codePerDay,
                newLot,
                from.toString(),
                to.toString()
        );

        return ViewDto.CodeGenerationRequest.builder()
                .isSubmitted(true)
                .message("data have been successfully saved !")
                .build();
    }
}
