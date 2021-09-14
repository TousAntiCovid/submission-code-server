package fr.gouv.stopc.submission.code.server.sftp;

import fr.gouv.stopc.submission.code.server.sftp.utils.IntegrationTest;
import fr.gouv.stopc.submission.code.server.sftp.utils.SchedulerTestUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.util.Map;

import static fr.gouv.stopc.submission.code.server.sftp.manager.SftpManager.assertThatAllFilesFromSftp;

@Slf4j
@IntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PartialRecoveryTest extends SchedulerTestUtil {

    // Test resumption of activity following a partial execution yesterday

    @Test
    @Order(0)
    void when_sftp_is_empty() {
        assertThatAllFilesFromSftp(sftpService).hasSize(0);
    }

    @Test
    @Order(1)
    void when_scheduler_generate_300_code_per_days_since_J1_and_J9() {
        Map<Integer, Integer> dayAndVolumeMap = Map.of(0, 300, 8, 40, 9, 0);
        configureScheduler(dayAndVolumeMap);
        Assertions.assertDoesNotThrow(() -> dailyGenerateSchedule.dailyProductionCodeScheduler());
    }

    @Test
    @Order(2)
    void then_sftp_contains_16_files_between_J_and_J10() {
        assertThatAllFilesFromSftp(sftpService).hasSize(16);
    }

    @Test
    @Order(3)
    void then_in_db_there_is_300k_code_each_days_between_J_and_J7() {
        assertFromStartDayDuringNumberOfDaysCorrespondingToNumberOfCodes(0, 7, 300);
    }

    // 40 pour j 8

    @Test
    @Order(3)
    void then_in_db_there_is_0_codes_for_J9_and_J10() {
        assertFromStartDayDuringNumberOfDaysCorrespondingToNumberOfCodes(9, 2, 0);
    }

    @Test
    @Order(4)
    void when_purge_for_next_day() {
        assertPurgeSftp();
    }

    @Test
    @Order(5)
    void when_scheduler_generate_300_code_on_J10() {
        Map<Integer, Integer> dayAndVolumeMap = Map.of(0, 300);
        configureScheduler(dayAndVolumeMap);
        Assertions.assertDoesNotThrow(() -> dailyGenerateSchedule.dailyProductionCodeScheduler());
    }

    @Test
    @Order(6)
    void then_in_db_there_is_300k_code_each_days_between_J_and_J10() {
        assertFromStartDayDuringNumberOfDaysCorrespondingToNumberOfCodes(0, 10, 300);
    }

    @Test
    @Order(7)
    void then_sftp_contains_16_files() {
        assertThatAllFilesFromSftp(sftpService).hasSize(16);
    }

    @Test
    @Order(8)
    void when_purge() {
        assertPurgeSftpAndDB();
    }
}
