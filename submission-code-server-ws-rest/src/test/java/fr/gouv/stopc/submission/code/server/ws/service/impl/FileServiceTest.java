package fr.gouv.stopc.submission.code.server.ws.service.impl;

import fr.gouv.stopc.submission.code.server.commun.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.commun.service.impl.AlphaNumericCodeServiceImpl;
import fr.gouv.stopc.submission.code.server.commun.service.impl.UUIDv4CodeServiceImpl;
import fr.gouv.stopc.submission.code.server.database.entity.Lot;
import fr.gouv.stopc.submission.code.server.database.service.impl.SubmissionCodeServiceImpl;
import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.ws.dto.CodeDetailedDto;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.internal.util.Assert;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPInputStream;



@TestPropertySource("classpath:application.properties")
class FileServiceTest {

    private static final String TEST_FILE_ZIP = "testFile.tgz";

    @Mock
    private GenerateServiceImpl generateService;

    @Mock
    private SFTPServiceImpl sftpService;

    @Mock
    private SubmissionCodeServiceImpl submissionCodeService;

    @Spy
    @InjectMocks
    private FileServiceImpl fileExportService;


    @BeforeEach
    public void init(){

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(this.fileExportService, "qrCodeBaseUrlToBeFormatted", "my%smy%s");
        ReflectionTestUtils.setField(this.fileExportService, "targetZoneId", "Europe/Paris");
        ReflectionTestUtils.setField(this.fileExportService, "csvSeparator", ',');
        ReflectionTestUtils.setField(this.fileExportService, "csvDelimiter", '"');
        ReflectionTestUtils.setField(this.fileExportService, "csvFilenameFormat", "%s.csv");
        ReflectionTestUtils.setField(this.fileExportService, "transferFile", true);
        ReflectionTestUtils.setField(this.generateService, "targetZoneId","Europe/Paris");
        ReflectionTestUtils.setField(this.generateService, "numberOfTryInCaseOfError",1);
        ReflectionTestUtils.setField(this.generateService, "timeValidityUuid",2);
        ReflectionTestUtils.setField(this.generateService, "timeValidityAlphanum",15);
        ReflectionTestUtils.setField(this.generateService, "submissionCodeService", this.submissionCodeService);
        ReflectionTestUtils.setField(this.generateService, "alphaNumericCodeService", new AlphaNumericCodeServiceImpl());
        ReflectionTestUtils.setField(this.generateService, "uuiDv4CodeService", new UUIDv4CodeServiceImpl());
    }

    @Test
    public void testCreateZipComplete() throws IOException, SubmissionCodeServerException {
        // String numberCodeDay, String lot, String dateFrom, String dateTo
        final CodeDetailedDto sc = CodeDetailedDto.builder()
                .typeAsString(CodeTypeEnum.UUIDv4.getTypeCode())
                .validUntil(OffsetDateTime.now().toString())
                .validFrom(OffsetDateTime.now().toString())
                .code("3d27eeb8-956c-4660-bc04-8612a4c0a7f1")
                .build();

        OffsetDateTime startDate = OffsetDateTime.now();
        String nowDay = startDate.format(DateTimeFormatter.ISO_DATE_TIME);
        String endDay = OffsetDateTime.now().plusDays(4L).format(DateTimeFormatter.ISO_DATE_TIME);

        Lot lot= new Lot();
        lot.setId(1L);

        OffsetDateTime date= OffsetDateTime.now().plusDays(1L);
        List<OffsetDateTime> dates = new ArrayList<>();
        dates.add(date);

        Mockito.when(generateService.generateCodeGeneric(10, CodeTypeEnum.UUIDv4,date, lot)).thenReturn(Arrays.asList(sc));
        Mockito.when(generateService.getListOfValidDatesFor(5,startDate)).thenReturn(dates);
        Optional<ByteArrayOutputStream> result = Optional.empty();


        result = fileExportService.zipExport(10L, lot, nowDay, endDay);

        ByteArrayOutputStream byteArray;
        if(result.isPresent()) {
            byteArray = result.get();

            OutputStream outputStream = new FileOutputStream(TEST_FILE_ZIP);
            byteArray.writeTo(outputStream);

            //unzip
            FileInputStream fis = new FileInputStream(TEST_FILE_ZIP);
            TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new GZIPInputStream(fis));
            TarArchiveEntry tarEntry = tarArchiveInputStream.getNextTarEntry();

            int countCsv = 0;
            while (tarEntry != null) {
                countCsv = countCsv + 1;
                tarEntry =tarArchiveInputStream.getNextTarEntry();
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
    public void testCreateZipCompleteOneDay() throws Exception {
        // String numberCodeDay, String lot, String dateFrom, String dateTo
        Optional<ByteArrayOutputStream> result;

        final CodeDetailedDto sc = CodeDetailedDto.builder()
                .typeAsString(CodeTypeEnum.UUIDv4.getTypeCode())
                .validUntil(OffsetDateTime.now().toString())
                .validFrom(OffsetDateTime.now().toString())
                .code("3d27eeb8-956c-4660-bc04-8612a4c0a7f1")
                .build();

        OffsetDateTime nowDay = OffsetDateTime.now();
        String nowDayString = nowDay.format(DateTimeFormatter.ISO_DATE_TIME);
        String endDay = nowDayString;

        Lot lot= new Lot();
        lot.setId(1L);

        Mockito.when(generateService.generateCodeGeneric(10, CodeTypeEnum.UUIDv4,nowDay, lot)).thenReturn(Arrays.asList(sc));
        List<OffsetDateTime> dates = new ArrayList<>();
        dates.add(nowDay);
        Mockito.when(generateService.getListOfValidDatesFor(1,nowDay)).thenReturn(dates);

        result = fileExportService.zipExport(10L, lot, nowDayString, endDay);

        Assert.notNull(result.get());

    }

    @Test
    public void testCheckDatesValidation(){

        OffsetDateTime startDay = OffsetDateTime.now().minusDays(1l);
        OffsetDateTime endDay = OffsetDateTime.now().plusDays(4L);
        Assert.isTrue(!fileExportService.isDateValid(startDay, endDay));

    }
}
