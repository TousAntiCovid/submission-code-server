package fr.gouv.stopc.submission.code.server.ws.service;


import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;
import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import fr.gouv.stopc.submission.code.server.ws.dto.ViewDto;
import fr.gouv.stopc.submission.code.server.ws.vo.ViewVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class ViewsServiceImpl {

    private final ISubmissionCodeService submissionCodeService;
    private final IFileService fileExportService;

    private static final SecureRandom sr = new SecureRandom();

    @Value("${stop.covid.qr.code.target.zone}")
    private String TARGET_ZONE_ID;

    /**
     * Default constructor
     * @param submissionCodeService Spring-injection of the alphaNumericCodeService giving access to persistence in db.
     */
    @Inject
    public ViewsServiceImpl(ISubmissionCodeService submissionCodeService,
                            IFileService fileExportService)
    {
        this.submissionCodeService = submissionCodeService;
        this.fileExportService = fileExportService;
    }

    public ViewDto.LotInformation getLotInformation(long lotIdentifier) {
        final long numOfCodes = this.submissionCodeService
                .getNumberOfCodesForLotIdentifier(lotIdentifier);
        return ViewDto.LotInformation.builder()
                .lotNumber(lotIdentifier)
                .numberOfCodes(numOfCodes)
                .build();
    }

    public ViewDto.CodeValuesForPage getViewLotCodeDetailListFor(
            int page,
            int elementByPage,
            long lotIdentifier)
    {
        final Page<SubmissionCode> submissionCodesPage = this.submissionCodeService.getSubmissionCodesFor(
                lotIdentifier,
                page - 1,
                elementByPage
        );

        return ViewDto.CodeValuesForPage.builder()
                .actualPage(submissionCodesPage.getNumber() + 1)
                .lastPage(submissionCodesPage.getTotalPages())
                .maxByPage(submissionCodesPage.getNumberOfElements())
                .lot(lotIdentifier)
                .codes(
                        submissionCodesPage.toList().stream()
                                .map(sc -> ViewDto.CodeDetail.builder()
                                        .code(sc.getCode())
                                        .build()
                                )
                                .collect(Collectors.toList())
                )
                .build();
    }

    public ViewDto.CodeGenerationRequest launchGenerationWith(
            ViewVo.CodeGenerationRequestBody codeGenerationRequestBody
    )
    {
        @NotNull final long codePerDay = codeGenerationRequestBody.getCodePerDay();
        @NotNull final OffsetDateTime from = codeGenerationRequestBody.getFrom();
        @NotNull OffsetDateTime to = codeGenerationRequestBody.getTo();

        try {
            final Optional<ByteArrayOutputStream> baos = this.fileExportService.zipExport(
                    Long.toString(codePerDay),
                    Long.toString(sr.nextLong()),
                    from.toString(),
                    to.toString()
            );
            return ViewDto.CodeGenerationRequest.builder()
                    .isSubmitted(true)
                    .message("data have been successfully saved !")
                    .baos(baos.get().toByteArray())
                    .build();
        } catch (Exception e) {
            log.error("{}", e);
            return ViewDto.CodeGenerationRequest.builder()
                    .isSubmitted(false)
                    .message("Something went wrong when generating data")
                    .build();
        }

    }
}
