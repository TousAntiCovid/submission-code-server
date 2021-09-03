package fr.gouv.stopc.submission.code.server.business.service.schedule;

import fr.gouv.stopc.submission.code.server.business.service.FileService;
import fr.gouv.stopc.submission.code.server.data.entity.Lot;
import fr.gouv.stopc.submission.code.server.data.repository.SubmissionCodeRepository;
import fr.gouv.stopc.submission.code.server.domain.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.domain.utils.FormatDatesKPI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DailyGenerateSchedule {

    private static final int TEN_DAYS = 10;

    private static final int MAX_BATCH_SIZE = 40000;

    private final SubmissionCodeRepository submissionCodeRepository;

    private final FileService fileService;

    // TODO : find a better way, a dataTable by exemple
    private final GenerationConfigProperties generationConfigList;

    private List<GenerationRequest> generationRequestList;

    @Autowired
    public DailyGenerateSchedule(SubmissionCodeRepository submissionCodeRepository,
                                 FileService fileService,
                                 GenerationConfigProperties generationConfigList) {
        this.submissionCodeRepository = submissionCodeRepository;
        this.fileService = fileService;
        this.generationConfigList = generationConfigList;
    }

    @Scheduled(cron = "${stop.covid.qr.code.cron.schedule}")
    public void dailyProductionCodeScheduler() {
        log.info("SCHEDULER : Start dailyProductionCodeScheduler");

        computeAndGenerateRequestList();

        generateCodesAndExportToSftp();

        purgeUnusedCodes();

        log.info("SCHEDULER : End dailyProductionCodeScheduler");
    }

    /**
     * Compute for each ten next days how many tar.gz we have to do
     * after, generate and save the result in a list of objects representing the requests.
     */
    private void computeAndGenerateRequestList() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        generationRequestList = new ArrayList<>();

        Collections.sort(generationConfigList.getScheduling());

        for (int i=0; i < TEN_DAYS; i++) {
            final OffsetDateTime startDateTime = FormatDatesKPI.normaliseDateFrom(today, ZoneOffset.UTC.toString());

            long dailyProductionTarget = getDailyProductionTarget(startDateTime);

            var numberOfAvailableCodes = this.submissionCodeRepository
                    .countAllByTypeAndDateAvailableEquals(CodeTypeEnum.LONG.getTypeCode(), startDateTime);
            var numberOfCodeToGenerate = dailyProductionTarget - numberOfAvailableCodes;
            var fragmentRemainingToGenerate = numberOfCodeToGenerate % MAX_BATCH_SIZE;
            var numberOfFullBatch = Math.toIntExact(numberOfCodeToGenerate / MAX_BATCH_SIZE);

            generateRequestList(numberOfFullBatch, fragmentRemainingToGenerate, startDateTime);

            today = today.plusDays(1);
        }
    }

    /**
     * Return the daily production target of current startDateTime from the properties files
     * @param startDateTime current startDateTime
     * @return long dailyProductionTarget
     */
    private long getDailyProductionTarget(OffsetDateTime startDateTime) {
        long dailyProductionTarget = 0;
        Optional<GenerationConfig> config = generationConfigList.getScheduling().stream().filter(
                c -> c.getStartdate().isBefore(startDateTime) || c.getStartdate().isEqual(startDateTime)
        ).reduce((f, s) -> s);

        if (config.isPresent()) {
            dailyProductionTarget = config.get().getDailyProduction();
        }
        return dailyProductionTarget;
    }

    private void generateRequestList(long numberOfFullBatch, long fragmentRemainingToGenerate, OffsetDateTime startDateTime) {
        for (long i = 0; i < numberOfFullBatch; i++) {
            generationRequestList.add(GenerationRequest.builder()
                    .numberOfCodeToGenerate(MAX_BATCH_SIZE)
                    .startDateTime(startDateTime)
                    .build()
            );
        }

        if (fragmentRemainingToGenerate > 0) {
            generationRequestList.add(GenerationRequest.builder()
                    .numberOfCodeToGenerate(fragmentRemainingToGenerate)
                    .startDateTime(startDateTime)
                    .build()
            );
        }
        log.info("SCHEDULER : Had generated {} tar.gz of 40000 codes and one of {} for the day {}", numberOfFullBatch, fragmentRemainingToGenerate, startDateTime);
    }

    /**
     * Generate codes, tar.gz and push to sftp server
     */
    public void generateCodesAndExportToSftp() {
        for (GenerationRequest generationRequest: generationRequestList) {
            try {
                this.fileService.generateAndPersisit(generationRequest.getNumberOfCodeToGenerate(), new Lot(), generationRequest.getStartDateTime());
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
        Long numberOfDeletedCodes = submissionCodeRepository.deleteAllByUsedFalseAndDateEndValidityBefore(dateEndValidityAfter);
        log.info("SCHEDULER : {} codes with a validity date of more than two months deleted", numberOfDeletedCodes);
    }

}
