package fr.gouv.stopc.submission.code.server.sftp;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import fr.gouv.stopc.submission.code.server.business.service.SFTPService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import java.io.*;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static fr.gouv.stopc.submission.code.server.sftp.SftpManager.assertThatAllFilesFromSftp;
import static fr.gouv.stopc.submission.code.server.sftp.SftpManager.pushFileToSftp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Slf4j
@ExtendWith(SpringExtension.class)
@IntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ContextConfiguration(classes = { SFTPService.class })
public class IsFilePresentTest {

    private static final String SEPARATOR = "/";

    @Autowired
    private SFTPService sftpService;

    @Value("${stop.covid.qr.code.targetzone}")
    private String targetZoneId;

    @TempDir
    File tmpDirectory;

    @Test
    @Order(1)
    public void given_create_zip_complete_one_day() {
        // TODO : function to be replace by the new service
        List<String> listFilenames = List.of(
                "stopcovid_qrcode_batch.tgz",
                "stopcovid_qrcode_batch.sha256"
        );
        OffsetDateTime date = OffsetDateTime.now(ZoneId.of(targetZoneId));
        String dateFile = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        ClassLoader classLoader = getClass().getClassLoader();
        for (String fileName : listFilenames) {
            File file = new File(classLoader.getResource("sftp".concat(SEPARATOR).concat(fileName)).getFile());
            String fileNameDest = dateFile.concat("000000_").concat(fileName);
            pushFileToSftp(sftpService, file, fileNameDest);
        }
    }

    @Test
    @Order(2)
    void is_file_present_on_sftp() {
        OffsetDateTime date = OffsetDateTime.now(ZoneId.of(targetZoneId));
        String dateFile = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        assertThatAllFilesFromSftp(sftpService)
                .hasSize(2)
                .anyMatch(l -> l.matches(dateFile + "\\d{6}_stopcovid_qrcode_batch.tgz"))
                .anyMatch(l -> l.matches(dateFile + "\\d{6}_stopcovid_qrcode_batch.sha256"));
    }

    @Test
    @Order(3)
    void csv_file_from_sftp_contains_good_number_of_rows() throws CsvValidationException, IOException {
        log.debug("Created directory {}", tmpDirectory.getAbsolutePath());
        List<String> fileList = SftpManager.getAllFilesFromSftp(sftpService, tmpDirectory);
        assertFalse(fileList.isEmpty());
        assertEquals(2, processExtractAndCount(fileList, tmpDirectory));
    }

    @Test
    @Order(4)
    void local_and_dest_files_are_the_same() throws IOException {
        // upload
        String fileName = "stopcovid_qrcode_batch.tgz";
        ClassLoader classLoader = getClass().getClassLoader();
        File uploadedFile = new File(classLoader.getResource("sftp".concat(SEPARATOR).concat(fileName)).getFile());
        OffsetDateTime date = OffsetDateTime.now(ZoneId.of(targetZoneId));
        String dateFile = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileNameDest = dateFile.concat("000001_").concat(fileName);
        pushFileToSftp(sftpService, uploadedFile, fileNameDest);
        // download
        File downloadedFile = SftpManager.getFileFromSftp(sftpService, fileNameDest, tmpDirectory);
        // compare
        assertTrue(areSameFiles(uploadedFile, downloadedFile));
    }

    private boolean areSameFiles(File uploaded, File downloaded) throws IOException {
        return FileUtils.contentEquals(uploaded, downloaded);
    }

    private int processExtractAndCount(List<String> fileList, File tmpDirectory) {
        int nbRows = 0;
        for (String file : fileList) {
            log.debug("File :  {}", file);
            if (file.contains(".tgz")) {
                log.debug("Try to extract file : {}", file);
                try (
                        FileInputStream fis = new FileInputStream(file);
                        GZIPInputStream gzipInputStream = new GZIPInputStream(new BufferedInputStream(fis));
                        TarArchiveInputStream tis = new TarArchiveInputStream(gzipInputStream)) {
                    TarArchiveEntry entry;
                    while ((entry = tis.getNextTarEntry()) != null) {
                        log.debug("Extracted file : {}", entry.getName());
                        final File curfile = new File(tmpDirectory, entry.getName());
                        extractFileFromTar(curfile, tis);
                        nbRows = this.countRowsFromCsvFile(curfile.getAbsolutePath());
                        curfile.delete();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return nbRows;
    }

    private void extractFileFromTar(File file, TarArchiveInputStream tis) throws IOException {
        int count;
        byte data[] = new byte[2048];
        try (BufferedOutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(file)
        )) {

            while ((count = tis.read(data)) != -1) {
                outputStream.write(data, 0, count);
            }

            outputStream.flush();
        }
    }

    private int countRowsFromCsvFile(String filePath) throws IOException, CsvValidationException {
        CSVReader reader = new CSVReader(new FileReader(filePath));
        int count = 0;
        while (reader.readNext() != null) {
            count++;
        }
        log.debug("File : {} contains ({}) rows.", filePath, String.valueOf(count));
        reader.close();
        return count;
    }
}
