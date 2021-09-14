package fr.gouv.stopc.submission.code.server.sftp;

import fr.gouv.stopc.submission.code.server.business.controller.exception.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.business.service.GenerateService;
import fr.gouv.stopc.submission.code.server.data.entity.Lot;
import fr.gouv.stopc.submission.code.server.domain.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.domain.utils.FormatDatesKPI;
import fr.gouv.stopc.submission.code.server.sftp.utils.IntegrationTest;
import fr.gouv.stopc.submission.code.server.sftp.utils.SchedulerTestUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static fr.gouv.stopc.submission.code.server.sftp.manager.SftpManager.assertThatAllFilesFromSftp;

@Slf4j
@IntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NominalAndIncreasingTest extends SchedulerTestUtil {

    @Value("${stop.covid.qr.code.cron.schedule}")
    private String scheduleTime;

    @Autowired
    GenerateService generateService;

    @Test
    @Order(1)
    void when_scheduler_generate_300k_code_per_days_during_10_days()
            throws NoSuchFieldException, IllegalAccessException {
        Map<Integer, Integer> dayAndVolumeMap = Map.of(0, 300);
        configureScheduler(dayAndVolumeMap);
        dailyGenerateSchedule.dailyProductionCodeScheduler();
    }

    @Test
    @Order(2)
    void then_date_execution_equals_midnight() {
        Assertions.assertEquals("0 0 * * * ?", scheduleTime);
    }

    @Test
    @Order(3)
    void then_sftp_contains_16_files_between_J_and_J10() {
        assertThatAllFilesFromSftp(sftpService).hasSize(16);
    }

    @Test
    @Order(4)
    void then_in_db_there_is_300k_code_each_days_between_J_and_J10() {
        assertFromStartDayDuringNumberOfDaysCorrespondingToNumberOfCodes(0, 10, 300);
    }

    @Test
    @Order(5)
    void then_there_is_no_more_long_code_older_thant_two_months() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        final OffsetDateTime endDateTime = FormatDatesKPI.normaliseDateFrom(today, ZoneOffset.UTC.toString())
                .minusMonths(2);
        long availableCodes = submissionCodeRepository.countAllByTypeAndDateEndValidityBefore(
                CodeTypeEnum.LONG.getTypeCode(), endDateTime
        );
        Assertions.assertEquals(0, availableCodes);
    }

    @Test
    @Order(6)
    void when_purge_sftp() {
        assertPurgeSftp();
    }

    @Test
    @Order(7)
    void when_generate_long_code_older_thant_two_months() throws SubmissionCodeServerException {
        createFalsesCodesInDB(OffsetDateTime.now().minusMonths(3), 100);
    }

    @Test
    @Order(8)
    void when_we_change_j5_daily_production_to_400k() throws NoSuchFieldException, IllegalAccessException {
        Map<Integer, Integer> dayAndVolumeMap = Map.of(0, 300, 5, 400);
        configureScheduler(dayAndVolumeMap);
        dailyGenerateSchedule.dailyProductionCodeScheduler();
    }

    @Test
    @Order(9)
    void then_sftp_contains_6_files_between_J_and_J10() {
        assertThatAllFilesFromSftp(sftpService).hasSize(6);
    }

    @Test
    @Order(10)
    void then_in_db_there_is_300k_code_each_days_between_J_and_J4() {
        assertFromStartDayDuringNumberOfDaysCorrespondingToNumberOfCodes(0, 4, 300);
    }

    @Test
    @Order(11)
    void then_in_db_there_is_400k_code_each_days_between_J6_and_J10() {
        assertFromStartDayDuringNumberOfDaysCorrespondingToNumberOfCodes(5, 5, 400);
    }

    @Test
    @Order(12)
    void then_there_is_still_no_more_long_code_older_thant_two_months() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        final OffsetDateTime endDateTime = FormatDatesKPI.normaliseDateFrom(today, ZoneOffset.UTC.toString())
                .minusMonths(2);
        long availableCodes = submissionCodeRepository.countAllByTypeAndDateEndValidityBefore(
                CodeTypeEnum.LONG.getTypeCode(), endDateTime
        );
        Assertions.assertEquals(0, availableCodes);
    }

    @Test
    @Order(13)
    void when_purge() {
        assertPurgeSftpAndDB();
    }

    private void createFalsesCodesInDB(OffsetDateTime from, long dailyAmount) throws SubmissionCodeServerException {
        Lot newLot = new Lot();
        newLot.setNumberOfCodes((long) 1);
        newLot.setDateExecution(OffsetDateTime.now());

        generateService.generateLongCodesWithBulkMethod(
                from,
                dailyAmount,
                newLot,
                OffsetDateTime.now()
        );
    }
}
