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

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.zip.ZipOutputStream;

@Slf4j
@SpringBootTest
class FileExportServiceTest {

    @Autowired
    private FileExportServiceImpl fileExportService;


    @Before
    public void init(){
        log.info("Initialize mokito injection in services...");
        MockitoAnnotations.initMocks(this);
    }

    public static final SecureRandom sr = new SecureRandom();

    @Test
    public void createZipComplete(){
        // String numberCodeDay, String lot, String dateFrom, String dateTo
        Optional<ZipOutputStream> result = Optional.empty();

        String nowDay = OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        String endDay = OffsetDateTime.now().plusDays(4L).format(DateTimeFormatter.ISO_DATE_TIME);
        try{
            result = fileExportService.zipExport("10", Long.toString(sr.nextLong()), nowDay, endDay);

        } catch (Exception e)
        {
            Assert.isTrue(false);
        }
        Assert.notNull(result.get());

    }

    @Test
    public void createZipCompleteOneDay() throws Exception {
        // String numberCodeDay, String lot, String dateFrom, String dateTo
        Optional<ZipOutputStream> result = Optional.empty();

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
            Optional<ZipOutputStream> result = fileExportService.zipExport("10", "2", startDay, endDay);
        });

    }
}
