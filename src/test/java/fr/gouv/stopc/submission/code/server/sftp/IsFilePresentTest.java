package fr.gouv.stopc.submission.code.server.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import fr.gouv.stopc.submission.code.server.business.controller.exception.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.business.dto.CodeDetailedDto;
import fr.gouv.stopc.submission.code.server.business.service.*;
import fr.gouv.stopc.submission.code.server.data.entity.Lot;
import fr.gouv.stopc.submission.code.server.domain.enums.CodeTypeEnum;
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
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application.properties")
@IntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IsFilePresentTest {

    /**
     * Zone ID to use
     */
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

        // Mock pour ne pas avoir à faire les clé priv/pub
        when(this.sftp.createConnection()).thenReturn(this.createConnection());
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

        final CodeDetailedDto sc = CodeDetailedDto.builder()
                .typeAsString(CodeTypeEnum.LONG.getTypeCode())
                .validUntil(OffsetDateTime.now().toString())
                .validFrom(OffsetDateTime.now().toString())
                .dateGenerate(OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .code("3d27eeb8-956c-4660-bc04-8612a4c0a7f1")
                .build();

        OffsetDateTime nowDay = OffsetDateTime.now();
        String nowDayString = nowDay.format(DateTimeFormatter.ISO_DATE_TIME);
        String endDay = nowDayString;

        Lot lot = new Lot();
        lot.setId(1L);

        when(
                generateService.generateLongCodesWithBulkMethod(
                        any(OffsetDateTime.class), anyLong(), any(Lot.class), any(OffsetDateTime.class)
                )
        )
                .thenReturn(Arrays.asList(sc));
        List<OffsetDateTime> dates = new ArrayList<>();
        dates.add(nowDay);
        when(generateService.getListOfValidDatesFor(1, nowDay)).thenReturn(dates);

        result = fileExportService.zipExport(10L, lot, nowDayString, endDay);

        Assert.notNull(result.get());

    }

    @Test
    @Order(2)
    void is_file_present() throws Exception {

        log.info("SFTP: connection is about to be created");
        ChannelSftp channelSftp = sftp.createConnection();
        log.info("SFTP: connexion created");

        log.info("===> SFTP: ls -lah /home/foo/upload");
        Vector<ChannelSftp.LsEntry> ls = channelSftp.ls("upload");
        for (ChannelSftp.LsEntry string : ls) {
            log.info(">>> {}", String.valueOf(string.getFilename()));
        }
        log.info("<=== SFTP: ls -lah /home/foo/upload");

        log.info("SFTP: connection is about to be closed");
        channelSftp.exit();
        log.info("SFTP: connection closed");

    }

    /**
     * Create connection SFTP to transfer file in server. The connection is created
     * with user and private key of user.
     *
     * @return An object channelSftp.
     */
    public ChannelSftp createConnection() throws SubmissionCodeServerException {
        JSch jSch = new JSch();
        try {
            log.info("SFTP: host : {}", System.getProperty("spring.sftp.host"));
            log.info("SFTP: port : {}", System.getProperty("spring.sftp.port"));
            Session jsSession = jSch.getSession(
                    "user", System.getProperty("spring.sftp.host"),
                    Integer.parseInt(System.getProperty("spring.sftp.port"))
            );
            jsSession.setConfig("StrictHostKeyChecking", "no");
            jsSession.setPassword("password");
            jsSession.connect();
            final ChannelSftp sftp = (ChannelSftp) jsSession.openChannel("sftp");
            sftp.connect();
            return sftp;
        } catch (JSchException e) {
            e.printStackTrace();
            return null;
        }
    }

}
