package fr.gouv.stopc.submission.code.server.sftp;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import fr.gouv.stopc.submission.code.server.sftp.dto.CsvRowDto;
import fr.gouv.stopc.submission.code.server.sftp.manager.SftpManager;
import fr.gouv.stopc.submission.code.server.sftp.utils.IntegrationTest;
import fr.gouv.stopc.submission.code.server.sftp.utils.SchedulerTestUtil;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvParser;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvParserSettings;

import java.io.*;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static fr.gouv.stopc.submission.code.server.sftp.manager.SftpManager.assertThatAllFilesFromSftp;

@Slf4j
@IntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GeneratedFilesContentTest extends SchedulerTestUtil {

    @TempDir
    File tmpDirectory;

    @Test
    @Order(0)
    void when_sftp_contains_0_files_between_J_and_J10() {
        assertThatAllFilesFromSftp(sftpService).hasSize(0);
    }

    @Test
    @Order(1)
    void when_scheduler_generate_10_code_per_days_during_1day() {
        Map<Integer, Integer> dayAndVolumeMap = Map.of(0, 10, 1, 0);
        configureScheduler(dayAndVolumeMap);
        LockAssert.TestHelper.makeAllAssertsPass(true);
        Assertions.assertDoesNotThrow(() -> dailyGenerateSchedule.dailyProductionCodeScheduler());
    }

    @Test
    @Order(2)
    void then_sftp_contains_2_files() {
        OffsetDateTime date = OffsetDateTime.now(ZoneId.of(targetZoneId));
        String dateFile = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        assertThatAllFilesFromSftp(sftpService).hasSize(2)
                .anyMatch(l -> l.matches(dateFile + "\\d{6}_stopcovid_qrcode_batch.tgz"))
                .anyMatch(l -> l.matches(dateFile + "\\d{6}_stopcovid_qrcode_batch.sha256"));
    }

    @Test
    @Order(3)
    void then_archive_and_csv_had_right_filename() {
        File archiveFile = getTgzFile();
        OffsetDateTime date = OffsetDateTime.now(ZoneId.of(targetZoneId));
        List<File> listCsvFiles = extractFile(archiveFile.getPath());
        String csvDateFile = date.format(DateTimeFormatter.ofPattern("yyMMdd"));
        Assertions
                .assertTrue(listCsvFiles.stream().allMatch(l -> l.getName().matches("\\d{2}" + csvDateFile + ".csv")));
    }

    @Test
    @Order(4)
    void then_csv_contains_11_lines() throws CsvValidationException, IOException {
        File archiveFile = getTgzFile();
        List<File> listCsvFiles = extractFile(archiveFile.getPath());
        HashedMap filesRowsCount = countRowsFromCsvFiles(listCsvFiles);
        Assertions.assertTrue(filesRowsCount.values().stream().findFirst().isPresent());
        Assertions.assertEquals(11, filesRowsCount.values().stream().findFirst().get());
    }

    @Test
    @Order(5)
    void then_first_csv_content_is_correct() throws IOException {
        File archiveFile = getTgzFile();
        List<File> listCsvFiles = extractFile(archiveFile.getPath());
        verifyRowsFromCsvFiles(listCsvFiles);
    }

    @Test
    @Order(6)
    void when_purge() {
        assertPurgeSftpAndDB();
    }

    private File getTgzFile() {
        OffsetDateTime date = OffsetDateTime.now(ZoneId.of(targetZoneId));
        String dateFile = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<File> fileList = SftpManager.getAllFilesFromSftp(sftpService, tmpDirectory);
        File archiveFile = fileList.stream()
                .filter(l -> l.getName().matches(dateFile + "\\d{6}_stopcovid_qrcode_batch.tgz")).findFirst()
                .orElse(null);
        Assertions.assertNotNull(archiveFile);
        return archiveFile;
    }

    private List<File> extractFile(String archiveFilePath) {
        List<File> csvFiles = new ArrayList<>();
        log.debug("File :  {}", archiveFilePath);
        if (archiveFilePath.contains(".tgz")) {
            log.debug("Try to extract file : {}", archiveFilePath);
            try (
                    FileInputStream fis = new FileInputStream(archiveFilePath);
                    GZIPInputStream gzipInputStream = new GZIPInputStream(new BufferedInputStream(fis));
                    TarArchiveInputStream tis = new TarArchiveInputStream(gzipInputStream)) {
                TarArchiveEntry entry;
                while ((entry = tis.getNextTarEntry()) != null) {
                    log.debug("Extracted file : {}", entry.getName());
                    final File currentFile = new File(tmpDirectory, entry.getName());
                    extractFileFromTar(currentFile, tis);
                    csvFiles.add(currentFile);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return csvFiles;
    }

    private void verifyRowsFromCsvFiles(List<File> fileList) throws IOException {
        for (File file : fileList) {
            CsvParserSettings settings = new CsvParserSettings();
            settings.getFormat().setLineSeparator("\n");
            settings.getFormat().setDelimiter(',');
            CsvParser parser = new CsvParser(settings);
            List<String[]> allRows = parser.parseAll(new FileReader(file));

            String[] expected = { "code_pour_qr", "code_brut", "validite_debut", "validite_fin" };
            Assertions.assertTrue(allRows.stream().findFirst().isPresent());
            String[] opt = allRows.stream().findFirst().get();
            Assertions.assertArrayEquals(expected, opt);

            List<CsvRowDto> csvRowDtoList = allRows.stream().skip(1).map(p -> new CsvRowDto(p[0], p[1], p[2], p[3]))
                    .collect(Collectors.toList());
            for (CsvRowDto row : csvRowDtoList) {
                Assertions.assertTrue(row.getQrcode().matches("https://app.stopcovid.gouv.fr\\?code=(.{36})&type=1"));
                Assertions.assertTrue(row.getCode().matches("(.{36})"));
                OffsetDateTime date = OffsetDateTime.now(ZoneId.of(targetZoneId)).truncatedTo(ChronoUnit.DAYS);
                Assertions.assertEquals(date, row.getDateAvailable());
                date = OffsetDateTime.now(ZoneId.of(targetZoneId)).truncatedTo(ChronoUnit.DAYS).plusDays(3)
                        .minusHours(2).minusMinutes(1);
                Assertions.assertEquals(date, row.getDateEndValidity());
            }
        }
    }

    private HashedMap countRowsFromCsvFiles(List<File> fileList) throws IOException, CsvValidationException {
        HashedMap map = new HashedMap();
        for (File file : fileList) {
            CSVReader reader = new CSVReader(new FileReader(file.getPath()));
            int count = 0;
            while (reader.readNext() != null) {
                count++;
            }
            map.put(file.getName(), count);
            reader.close();
        }
        return map;
    }

    private void extractFileFromTar(File file, TarArchiveInputStream tis) throws IOException {
        int count;
        byte[] data = new byte[2048];
        try (BufferedOutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(file)
        )) {

            while ((count = tis.read(data)) != -1) {
                outputStream.write(data, 0, count);
            }

            outputStream.flush();
        }
    }

}
