package fr.gouv.stopc.submission.code.server.it.utils;

import fr.gouv.stopc.submission.code.server.business.controller.exception.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.business.service.FileService;
import fr.gouv.stopc.submission.code.server.business.service.GenerateService;
import fr.gouv.stopc.submission.code.server.business.service.SFTPService;
import fr.gouv.stopc.submission.code.server.business.service.schedule.DailyGenerateSchedule;
import fr.gouv.stopc.submission.code.server.business.service.schedule.GenerationConfigProperties;
import fr.gouv.stopc.submission.code.server.business.service.schedule.GenerationConfigProperties.GenerationConfig;
import fr.gouv.stopc.submission.code.server.data.entity.Lot;
import fr.gouv.stopc.submission.code.server.data.repository.SubmissionCodeRepository;
import fr.gouv.stopc.submission.code.server.domain.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.it.manager.SftpManager;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;

public class SchedulerTestUtil {

    @Value("${stop.covid.qr.code.targetzone}")
    protected String targetZoneId;

    @Autowired
    protected DailyGenerateSchedule dailyGenerateSchedule;

    @Autowired
    protected SubmissionCodeRepository submissionCodeRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    protected SFTPService sftpService;

    @Autowired
    GenerateService generateService;

    protected OffsetDateTime getMidnight() {
        return Instant.now()
                .atZone(ZoneId.of("Europe/Paris"))
                .truncatedTo(DAYS)
                .toOffsetDateTime();
    }

    protected void assertFromStartDayDuringNumberOfDaysCorrespondingToNumberOfCodes(int startDay, int numberOfDays,
            int numberOfExpectedCodes) {
        OffsetDateTime currentDate = getMidnight();
        OffsetDateTime iterateDate = currentDate.plusDays(startDay);
        for (int days = 0; days < numberOfDays; days++) {

            long availableCodes = submissionCodeRepository.countAllByTypeAndDateAvailableEquals(
                    CodeTypeEnum.LONG.getTypeCode(), iterateDate
            );
            Assertions.assertEquals(numberOfExpectedCodes, availableCodes);
            iterateDate = iterateDate.plusDays(1);
        }
    }

    protected void configureScheduler(Map<Integer, Integer> production) {
        OffsetDateTime todayOff = OffsetDateTime.now(ZoneId.of(targetZoneId)).truncatedTo(DAYS);
        List<GenerationConfig> scheduling = new ArrayList<>();
        production.forEach((day, volume) -> {
            OffsetDateTime currentDate = todayOff.plusDays(day);
            GenerationConfig conf = GenerationConfig.builder().dailyProduction(volume).startDate(currentDate)
                    .build();
            scheduling.add(conf);
        }
        );

        GenerationConfigProperties generationConfig = new GenerationConfigProperties();
        generationConfig.setScheduling(scheduling);
        generationConfig.setMaxbatchsize(40);
        dailyGenerateSchedule = new DailyGenerateSchedule(
                submissionCodeRepository,
                fileService,
                generationConfig
        );
    }

    protected void purgeSftp() {
        SftpManager.purgeSftp();
    }

    protected void purgeSftpAndDB() {
        submissionCodeRepository.deleteAll();
        SftpManager.purgeSftp();
    }

    protected void createFalsesCodesInDB(OffsetDateTime from, long dailyAmount)
            throws SubmissionCodeServerException {
        Lot newLot = new Lot();
        newLot.setNumberOfCodes(1);
        newLot.setDateExecution(OffsetDateTime.now());

        generateService.generateLongCodesWithBulkMethod(
                from,
                dailyAmount,
                newLot,
                OffsetDateTime.now()
        );
    }

    public void makeSftpUnreachable() {
        sftpService.setPort(2222);
    }

    public void makeSftpReachable() {
        sftpService.setPort(SftpManager.getMappedPort());
    }
}
