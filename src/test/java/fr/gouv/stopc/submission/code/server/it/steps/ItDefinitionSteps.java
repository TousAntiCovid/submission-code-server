package fr.gouv.stopc.submission.code.server.it.steps;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import fr.gouv.stopc.submission.code.server.business.controller.exception.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.domain.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.domain.utils.FormatDatesKPI;
import fr.gouv.stopc.submission.code.server.it.dto.CsvRowDto;
import fr.gouv.stopc.submission.code.server.it.manager.SftpManager;
import fr.gouv.stopc.submission.code.server.it.utils.SchedulerTestUtil;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvParser;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvParserSettings;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@Slf4j
public class ItDefinitionSteps extends SchedulerTestUtil {

    private File tmpDirectory;

    @Before
    public void initialization() {
        makeSftpReachable();
        purgeSftpAndDB();
    }

    @Given("generate long code older than two months")
    public void generate_long_code_older_than_two_months() throws SubmissionCodeServerException {
        createFalsesCodesInDB(OffsetDateTime.now().minusMonths(3), 100);
    }

    @Given("purge sftp")
    public void purge_sftp() {
        purgeSftp();
    }

    @Given("sftp server is unreachable")
    public void stop_sftp() {
        makeSftpUnreachable();
    }

    @Given("scheduler generate codes and stop after the first batch of j8")
    public void scheduler_generate_codes_and_stop_after_the_first_batch_of_j8() {
        Map<Integer, Integer> dayAndVolumeMap = Map.of(0, 300, 8, 40, 9, 0);
        configureScheduler(dayAndVolumeMap);
        LockAssert.TestHelper.makeAllAssertsPass(true);
        Assertions.assertDoesNotThrow(() -> dailyGenerateSchedule.dailyProductionCodeScheduler());
    }

    @Given("scheduler generate {int} code per days since J {int} and J {int}")
    public void scheduler_generate_code_per_days_since_j_and_j(int numberOfCodes, int startDayNumber,
            int endDayNumber) {
        endDayNumber++;
        Map<Integer, Integer> dayAndVolumeMap = Map.of(startDayNumber, numberOfCodes, endDayNumber, 0);
        configureScheduler(dayAndVolumeMap);
        LockAssert.TestHelper.makeAllAssertsPass(true);
        Assertions.assertDoesNotThrow(() -> dailyGenerateSchedule.dailyProductionCodeScheduler());
    }

    @Then("sftp contains {int} files")
    public void then_sftp_contains_files(int numberOfFiles) {
        SftpManager.assertThatAllFilesFromSftp().hasSize(numberOfFiles);
    }

    @Then("then in db there is {int} codes each days between j {int} and j {int}")
    public void then_in_db_there_is_code_each_days_between_J_and_J(int numberOfCodes,
            int startDayNumber,
            int endDayNumber) {
        int numberOfDays = endDayNumber + 1 - startDayNumber;
        assertFromStartDayDuringNumberOfDaysCorrespondingToNumberOfCodes(startDayNumber, numberOfDays, numberOfCodes);
    }

    @Then("then there is no more codes older than two months")
    public void then_there_is_no_more_long_codes_older_thant_two_months() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        final OffsetDateTime endDateTime = FormatDatesKPI.normaliseDateFrom(today, ZoneOffset.UTC.toString())
                .minusMonths(2);
        long availableCodes = submissionCodeRepository.countAllByTypeAndDateEndValidityBefore(
                CodeTypeEnum.LONG.getTypeCode(), endDateTime
        );
        Assertions.assertEquals(0, availableCodes);
    }

    @Then("sftp contains {int} files and names are well formatted")
    public void then_sftp_contains_files_and_names_are_well_formatted(int numberOfFiles) {
        OffsetDateTime date = OffsetDateTime.now(ZoneId.of(targetZoneId));
        String dateFile = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        SftpManager.assertThatAllFilesFromSftp().hasSize(numberOfFiles)
                .anyMatch(l -> l.matches(dateFile + "\\d{6}_stopcovid_qrcode_batch.tgz"))
                .anyMatch(l -> l.matches(dateFile + "\\d{6}_stopcovid_qrcode_batch.sha256"));
    }

    @Then("archive and csv has right filename")
    public void archive_and_csv_has_right_filename() {
        tmpDirectory = Files.createTempDir();
        File archiveFile = getTgzFile();
        OffsetDateTime date = OffsetDateTime.now(ZoneId.of(targetZoneId));
        List<File> listCsvFiles = extractFile(archiveFile.getPath());
        String csvDateFile = date.format(DateTimeFormatter.ofPattern("yyMMdd"));
        Assertions
                .assertTrue(listCsvFiles.stream().allMatch(l -> l.getName().matches("\\d{2}" + csvDateFile + ".csv")));
    }

    @Then("csv contains {int} lines")
    public void csv_contains_x_lines(int numberOfLines) throws CsvValidationException, IOException {
        File archiveFile = getTgzFile();
        List<File> listCsvFiles = extractFile(archiveFile.getPath());
        HashedMap filesRowsCount = countRowsFromCsvFiles(listCsvFiles);
        Assertions.assertTrue(filesRowsCount.values().stream().findFirst().isPresent());
        Assertions.assertEquals(numberOfLines, filesRowsCount.values().stream().findFirst().get());
    }

    @Then("first csv content is correct")
    public void first_csv_content_is_correct() throws IOException {
        File archiveFile = getTgzFile();
        List<File> listCsvFiles = extractFile(archiveFile.getPath());
        verifyRowsFromCsvFiles(listCsvFiles);
    }

    private File getTgzFile() {
        OffsetDateTime date = OffsetDateTime.now(ZoneId.of(targetZoneId));
        String dateFile = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<File> fileList = SftpManager.getAllFilesFromSftp(tmpDirectory);
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

    private HashedMap countRowsFromCsvFiles(List<File> fileList) throws IOException, CsvValidationException {
        HashedMap map = new HashedMap();
        for (File file : fileList) {
            try (CSVReader reader = new CSVReader(new FileReader(file.getPath()))) {
                int count = 0;
                while (reader.readNext() != null) {
                    count++;
                }
                map.put(file.getName(), count);
            }
        }
        return map;
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
                Instant dateAvailable = Instant.now().truncatedTo(ChronoUnit.DAYS);
                Assertions.assertEquals(dateAvailable, row.getDateAvailable());
                Instant expectedEndValidityDate = OffsetDateTime.now(ZoneId.of(targetZoneId))
                        .truncatedTo(ChronoUnit.DAYS).plus(8, ChronoUnit.DAYS)
                        .minus(1, ChronoUnit.MINUTES).toInstant();
                Assertions.assertEquals(expectedEndValidityDate, row.getDateEndValidity());
            }
        }
    }

}
