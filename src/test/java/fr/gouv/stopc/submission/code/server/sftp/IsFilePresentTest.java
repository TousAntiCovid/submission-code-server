package fr.gouv.stopc.submission.code.server.sftp;

import fr.gouv.stopc.submission.code.server.business.controller.exception.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.business.service.*;
import fr.gouv.stopc.submission.code.server.data.entity.Lot;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.modelmapper.internal.util.Assert;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static fr.gouv.stopc.submission.code.server.sftp.SftpManager.assertThatAllFilesFromSftp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application.properties")
@IntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IsFilePresentTest {

    @Value("${stop.covid.qr.code.targetzone}")
    private String targetZoneId;

    @Mock
    private GenerateService generateService;

    @Mock
    private SubmissionCodeService submissionCodeService;

    @Mock
    private SequenceFichierService sequenceFichierService;

    @Spy
    @InjectMocks
    private FileService fileExportService;

    private SFTPService sftp = mock(SFTPService.class);

    @BeforeEach
    public void init() throws SubmissionCodeServerException {

        // Mock sftp keys
        when(this.sftp.createConnection()).thenReturn(SftpManager.createConnection());
        doCallRealMethod().when(this.sftp).transferFileSFTP(any(ByteArrayOutputStream.class));

        MockitoAnnotations.initMocks(this);

        TimeZone.setDefault(TimeZone.getTimeZone(this.targetZoneId));

        ReflectionTestUtils.setField(this.fileExportService, "qrCodeBaseUrlToBeFormatted", "my%smy%s");
        ReflectionTestUtils.setField(this.fileExportService, "targetZoneId", this.targetZoneId);
        ReflectionTestUtils.setField(this.fileExportService, "csvSeparator", ',');
        ReflectionTestUtils.setField(this.fileExportService, "csvDelimiter", '"');
        ReflectionTestUtils.setField(this.fileExportService, "csvFilenameFormat", "%d%s.csv");
        ReflectionTestUtils.setField(this.fileExportService, "directoryTmpCsv", "tmp");
        ReflectionTestUtils.setField(this.fileExportService, "transferFile", true);
        ReflectionTestUtils.setField(this.generateService, "targetZoneId", this.targetZoneId);
        ReflectionTestUtils.setField(this.generateService, "numberOfTryInCaseOfError", 1);
        ReflectionTestUtils.setField(this.generateService, "timeValidityLongCode", 2);
        ReflectionTestUtils.setField(this.generateService, "timeValidityShortCode", 15);
        ReflectionTestUtils.setField(this.generateService, "submissionCodeService", this.submissionCodeService);
        ReflectionTestUtils.setField(this.generateService, "shortCodeService", new ShortCodeService());
        ReflectionTestUtils.setField(this.generateService, "longCodeService", new LongCodeService());
        ReflectionTestUtils.setField(this.sftp, "targetZoneId", this.targetZoneId);
        ReflectionTestUtils.setField(this.sftp, "zipFilenameFormat", "upload/%s_stopcovid_qrcode_batch.tgz");
        ReflectionTestUtils
                .setField(this.sftp, "digestFileNameFormatSHA256", "upload/%s_stopcovid_qrcode_batch.sha256");
        when(this.sequenceFichierService.getSequence(any())).thenReturn(Optional.empty());
    }

    @Test
    @Order(1)
    public void test_create_zip_complete_one_day() throws Exception {
        Optional<ByteArrayOutputStream> result;

        OffsetDateTime nowDay = OffsetDateTime.now();
        String nowDayString = nowDay.format(DateTimeFormatter.ISO_DATE_TIME);
        String endDay = nowDayString;
        Lot lot = new Lot();
        lot.setId(1L);
        List<OffsetDateTime> dates = new ArrayList<>();
        dates.add(nowDay);
        when(generateService.getListOfValidDatesFor(1, nowDay)).thenReturn(dates);

        result = fileExportService.zipExport(10L, lot, nowDayString, endDay);

        Assert.notNull(result.get());
    }

    @Test
    @Order(2)
    void is_file_present() {
        OffsetDateTime date = OffsetDateTime.now(ZoneId.of(targetZoneId));
        String dateFile = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        assertThatAllFilesFromSftp()
                .hasSize(4)
                .anyMatch(l -> l.matches(dateFile + "\\d{6}_stopcovid_qrcode_batch.tgz"))
                .anyMatch(l -> l.matches(dateFile + "\\d{6}_stopcovid_qrcode_batch.sha256"));
    }
}
