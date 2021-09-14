package fr.gouv.stopc.submission.code.server.business.service.schedule;

import fr.gouv.stopc.submission.code.server.business.service.FileService;
import fr.gouv.stopc.submission.code.server.data.entity.Lot;
import fr.gouv.stopc.submission.code.server.data.repository.SubmissionCodeRepository;
import fr.gouv.stopc.submission.code.server.domain.enums.CodeTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DailyGenerateSchedule {

    private static final int TEN_DAYS = 10;

    private final SubmissionCodeRepository submissionCodeRepository;

    private final FileService fileService;

    private final GenerationConfigProperties generationConfig;

    private List<GenerationRequest> generationRequestList;

    @Autowired
    public DailyGenerateSchedule(SubmissionCodeRepository submissionCodeRepository,
            FileService fileService,
            GenerationConfigProperties generationConfig) {
        this.submissionCodeRepository = submissionCodeRepository;
        this.fileService = fileService;
        this.generationConfig = generationConfig;
    }

    @Scheduled(cron = "${stop.covid.qr.code.cron.schedule}")
    public void dailyProductionCodeScheduler() {
        log.info("SCHEDULER : Start dailyProductionCodeScheduler");

        computeAndGenerateRequestList();

        generateCodesAndExportToSftp();

        purgeUnusedCodes();

        log.info("SCHEDULER : End dailyProductionCodeScheduler");
    }

    public List<GenerationConfig> getScheduling() {
        return generationConfig.getScheduling();
    }

    /**
     * Compute for each ten next days how many tar.gz we have to do after, generate
     * and save the result in a list of objects representing the requests.
     */
    private void computeAndGenerateRequestList() {
        OffsetDateTime today = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS);
        generationRequestList = new ArrayList<>();

        Collections.sort(getScheduling());

        for (int i = 0; i <= TEN_DAYS; i++) {
            final OffsetDateTime startDateTime = today;
            long dailyProductionTarget = getDailyProductionTarget(startDateTime);
            var numberOfAvailableCodes = this.submissionCodeRepository
                    .countAllByTypeAndDateAvailableEquals(CodeTypeEnum.LONG.getTypeCode(), startDateTime);
            var numberOfCodeToGenerate = dailyProductionTarget - numberOfAvailableCodes;
            var fragmentRemainingToGenerate = numberOfCodeToGenerate % generationConfig.getMaxbatchsize();
            var numberOfFullBatch = Math.toIntExact(numberOfCodeToGenerate / generationConfig.getMaxbatchsize());

            OffsetDateTime endDateTime = startDateTime.plusDays(1);

            // Batchs with max size
            for (long batchNumber = 0; batchNumber < numberOfFullBatch; batchNumber++) {
                updateOrCreateRequest(batchNumber, generationConfig.getMaxbatchsize(), startDateTime, endDateTime);
            }
            // Batch with less than max size
            if (fragmentRemainingToGenerate > 0) {
                updateOrCreateRequest(null, fragmentRemainingToGenerate, startDateTime, endDateTime);
            }

            today = today.plusDays(1);
        }
    }

    /**
     * Return the daily production target of current startDateTime from the
     * properties files
     * 
     * @param startDateTime current startDateTime
     * @return long dailyProductionTarget
     */
    private long getDailyProductionTarget(OffsetDateTime startDateTime) {
        long dailyProductionTarget = 0;
        Optional<GenerationConfig> config = getScheduling().stream().filter(
                c -> c.getStartDate().isBefore(startDateTime) || c.getStartDate().isEqual(startDateTime)
        ).reduce((f, s) -> s);

        if (config.isPresent()) {
            dailyProductionTarget = config.get().getDailyProduction();
        }
        return dailyProductionTarget;
    }

    private void updateOrCreateRequest(Long fullBatchIterationNumber,
            long dailyProduction,
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime) {
        boolean updated = false;
        // Update
        for (GenerationRequest gr : generationRequestList) {
            if (gr.getNumberOfCodeToGenerate().equals(dailyProduction)
                    && (gr.getIterationBatchNumber() == null
                            || gr.getIterationBatchNumber().equals(fullBatchIterationNumber))) {
                gr.setEndDateTime(endDateTime);
                updated = true;
                break;
            }
        }
        // Create
        if (!updated) {
            generationRequestList.add(
                    GenerationRequest.builder()
                            .numberOfCodeToGenerate(dailyProduction)
                            .startDateTime(startDateTime)
                            .endDateTime(endDateTime)
                            .iterationBatchNumber(fullBatchIterationNumber)
                            .build()
            );
        }
    }

    public void generateCodesAndExportToSftp() {
        for (GenerationRequest generationRequest : generationRequestList) {
            try {
                Lot newLot = new Lot();
                newLot.setNumberOfCodes(generationRequest.getNumberOfCodeToGenerate());
                newLot.setDateExecution(OffsetDateTime.now());
                this.fileService.schedulerZipExport(
                        generationRequest.getNumberOfCodeToGenerate(),
                        newLot,
                        generationRequest.getStartDateTime().toString(),
                        generationRequest.getEndDateTime().toString()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Purge unused codes no more valid since two months
     */
    void purgeUnusedCodes() {
        OffsetDateTime dateEndValidityAfter = OffsetDateTime.now().minusMonths(2);
        Long numberOfDeletedCodes = submissionCodeRepository
                .deleteAllByUsedFalseAndDateEndValidityBefore(dateEndValidityAfter);
        log.info("SCHEDULER : {} codes with a validity date of more than two months deleted", numberOfDeletedCodes);
    }

}
