package fr.gouv.stopc.submission.code.server.ws.service.fileservice;

import fr.gouv.stopc.submission.code.server.ws.service.FileExportServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.modelmapper.internal.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@SpringBootTest
class FileExportServiceTest {

    private static final String TEST_FILE_ZIP = "testFile.zip";

    @Autowired
    private FileExportServiceImpl fileExportService;


    @Before
    public void init(){
        log.info("Initialize mokito injection in services...");
        MockitoAnnotations.initMocks(this);
    }

    public static final SecureRandom sr = new SecureRandom();

    @Test
    public void createZipComplete() throws IOException {
        // String numberCodeDay, String lot, String dateFrom, String dateTo
        Optional<ByteArrayOutputStream> result = Optional.empty();

        String nowDay = OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        String endDay = OffsetDateTime.now().plusDays(4L).format(DateTimeFormatter.ISO_DATE_TIME);
        try{
            result = fileExportService.zipExport("10", Long.toString(sr.nextLong()), nowDay, endDay);

        } catch (Exception e)
        {
            Assert.isTrue(false);
        }

        ByteArrayOutputStream byteArray;
        if(result.isPresent()) {
            byteArray = result.get();

            OutputStream outputStream = new FileOutputStream(TEST_FILE_ZIP);
            byteArray.writeTo(outputStream);

            //unzip
            FileInputStream fis = new FileInputStream(TEST_FILE_ZIP);
            ZipInputStream zis = new ZipInputStream(fis);

            ZipEntry ze = zis.getNextEntry();
            int countCsv = 0;
            while (ze != null) {
                countCsv = countCsv + 1;
                ze = zis.getNextEntry();
            }
            Assert.isTrue(countCsv != 0);
            fis.close();
            outputStream.flush();
            outputStream.close();
            File fileToDelete = new File(TEST_FILE_ZIP);
            fileToDelete.deleteOnExit();

        } else{
            Assert.isTrue(false);
        }


    }

    @Test
    public void createZipCompleteOneDay() throws Exception {
        // String numberCodeDay, String lot, String dateFrom, String dateTo
        Optional<ByteArrayOutputStream> result;

        String nowDay = OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        String endDay = nowDay;

        result = fileExportService.zipExport("10", Long.toString(sr.nextLong()), nowDay, endDay);

        Assert.notNull(result.get());

    }

    @Test
    public void checkDatesValidation(){

        Assertions.assertThrows(Exception.class, () -> {
            String startDay = OffsetDateTime.now().minusDays(1l).format(DateTimeFormatter.ISO_DATE_TIME);
            String endDay = OffsetDateTime.now().plusDays(4L).format(DateTimeFormatter.ISO_DATE_TIME);
            Optional<ByteArrayOutputStream> result = fileExportService.zipExport("10", "2", startDay, endDay);
        });

    }
}
