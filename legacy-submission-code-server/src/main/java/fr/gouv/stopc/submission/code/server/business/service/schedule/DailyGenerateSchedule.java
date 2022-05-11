package fr.gouv.stopc.submission.code.server.business.service.schedule;

import fr.gouv.stopc.submission.code.server.business.controller.exception.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.business.service.FileService;
import fr.gouv.stopc.submission.code.server.data.entity.Lot;
import fr.gouv.stopc.submission.code.server.data.repository.SubmissionCodeRepository;
import fr.gouv.stopc.submission.code.server.domain.enums.CodeTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyGenerateSchedule {

    private static final int TEN_DAYS = 10;

    private final SubmissionCodeRepository submissionCodeRepository;

    private final FileService fileService;

    private final GenerationConfigProperties generationConfig;

    private List<GenerationRequest> generationRequestList;

    public void dailyProductionCodeScheduler() {
        log.info("Start dailyProductionCodeScheduler");

        computeAndGenerateRequestList();

        try {
            generateCodesAndExportToSftp();
        } catch (SubmissionCodeServerException e) {
            log.error("Unexpected error occurred", e);
        }

        purgeUnusedCodes();

        log.info("End dailyProductionCodeScheduler");
    }

    protected OffsetDateTime getMidnight() {
        return Instant.now()
                .atZone(ZoneId.of("Europe/Paris"))
                .truncatedTo(DAYS)
                .toOffsetDateTime();
    }

    /**
     * Compute for each ten next days how many tar.gz we have to do after, generate
     * and save the result in a list of objects representing the requests.
     */
    private void computeAndGenerateRequestList() {
        log.info("SCHEDULER : Start computeAndGenerateRequestList");
        OffsetDateTime currentDate = getMidnight();
        generationRequestList = new ArrayList<>();

        for (int i = 0; i <= TEN_DAYS; i++) {
            final OffsetDateTime startDateTime = currentDate;

            Integer dailyProductionTarget = generationConfig.getDailyProductionTarget(startDateTime);
            var numberOfAvailableCodes = this.submissionCodeRepository
                    .countAllByTypeAndDateAvailableEquals(CodeTypeEnum.LONG.getTypeCode(), startDateTime);
            log.info(
                    "SCHEDULER : We have to produce {} codes for day {}", dailyProductionTarget,
                    startDateTime.toString()
            );
            var numberOfCodeToGenerate = dailyProductionTarget - numberOfAvailableCodes;
            Long fragmentRemainingToGenerate = numberOfCodeToGenerate % generationConfig.getMaxbatchsize();
            var numberOfFullBatch = Math.toIntExact(numberOfCodeToGenerate / generationConfig.getMaxbatchsize());

            OffsetDateTime endDateTime = startDateTime.plusDays(1);

            // Batchs with max size
            for (long batchNumber = 0; batchNumber < numberOfFullBatch; batchNumber++) {
                updateOrCreateRequest(batchNumber, generationConfig.getMaxbatchsize(), startDateTime, endDateTime);
            }
            // Batch with less than max size
            if (fragmentRemainingToGenerate > 0) {
                updateOrCreateRequest(null, fragmentRemainingToGenerate.intValue(), startDateTime, endDateTime);
            }

            currentDate = currentDate.plusDays(1);
        }
    }

    private void updateOrCreateRequest(Long iterationBatchNumber,
            Integer numberOfCodeToGenerate,
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime) {
        boolean updated = false;
        // Update
        for (GenerationRequest gr : generationRequestList) {
            if (gr.getNumberOfCodeToGenerate().equals(numberOfCodeToGenerate)
                    && (null == gr.getIterationBatchNumber()
                            || gr.getIterationBatchNumber().equals(iterationBatchNumber))) {
                gr.setEndDateTime(endDateTime);
                updated = true;
                break;
            }
        }
        // Create
        if (!updated) {
            generationRequestList.add(
                    GenerationRequest.builder()
                            .numberOfCodeToGenerate(numberOfCodeToGenerate)
                            .startDateTime(startDateTime)
                            .endDateTime(endDateTime)
                            .iterationBatchNumber(iterationBatchNumber)
                            .build()
            );
        }
    }

    private void generateCodesAndExportToSftp() throws SubmissionCodeServerException {
        for (GenerationRequest generationRequest : generationRequestList) {
            Lot newLot = new Lot();
            newLot.setNumberOfCodes(generationRequest.getNumberOfCodeToGenerate());
            newLot.setDateExecution(OffsetDateTime.now());
            this.fileService.schedulerZipExport(
                    Long.valueOf(generationRequest.getNumberOfCodeToGenerate()),
                    newLot,
                    generationRequest.getStartDateTime(),
                    generationRequest.getEndDateTime()
            );
        }
    }

    /**
     * Purge unused codes no more valid since two months
     */
    public void purgeUnusedCodes() {
        log.info("SCHEDULER : Start purge unused codes");
        OffsetDateTime dateEndValidityAfter = OffsetDateTime.now().minusDays(2);
        submissionCodeRepository
                .deleteAllByUsedFalseAndDateEndValidityBefore(
                        dateEndValidityAfter
                );
        log.info("SCHEDULER : End deletion of codes with a validity date of more than two months deleted");
    }

}
