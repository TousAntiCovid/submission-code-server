package fr.gouv.stopc.submission.code.server.ws.service.impl;

import fr.gouv.stopc.submission.code.server.commun.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.entity.Lot;
import fr.gouv.stopc.submission.code.server.database.service.impl.SubmissionCodeServiceImpl;
import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
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
import java.util.Arrays;
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

    }

    @Test
    public void testCreateZipComplete() throws IOException, SubmissionCodeServerException {
        // String numberCodeDay, String lot, String dateFrom, String dateTo
        final SubmissionCodeDto sc = SubmissionCodeDto.builder()
                .type(CodeTypeEnum.UUIDv4.getTypeCode())
                .dateEndValidity(OffsetDateTime.now())
                .dateAvailable(OffsetDateTime.now())
                .code("3d27eeb8-956c-4660-bc04-8612a4c0a7f1")
                .lot(1)
                .build();

        Mockito.when(this.submissionCodeService
                .getCodeUUIDv4CodesForCsv(Mockito.any(), Mockito.any()))
                .thenReturn(Arrays.asList(sc));

        Optional<ByteArrayOutputStream> result = Optional.empty();

        String nowDay = OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        String endDay = OffsetDateTime.now().plusDays(4L).format(DateTimeFormatter.ISO_DATE_TIME);

        result = fileExportService.zipExport("10", new Lot(), nowDay, endDay);

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

        final SubmissionCodeDto sc = SubmissionCodeDto.builder()
                .type(CodeTypeEnum.UUIDv4.getTypeCode())
                .dateEndValidity(OffsetDateTime.now())
                .dateAvailable(OffsetDateTime.now())
                .code("3d27eeb8-956c-4660-bc04-8612a4c0a7f1")
                .lot(1)
                .build();

        Mockito.when(this.submissionCodeService
                .getCodeUUIDv4CodesForCsv(Mockito.any(), Mockito.any()))
                .thenReturn(Arrays.asList(sc));


        String nowDay = OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        String endDay = nowDay;

        result = fileExportService.zipExport("10", new Lot(), nowDay, endDay);

        Assert.notNull(result.get());

    }

    @Test
    public void testCheckDatesValidation(){

        OffsetDateTime startDay = OffsetDateTime.now().minusDays(1l);
        OffsetDateTime endDay = OffsetDateTime.now().plusDays(4L);
        Assert.isTrue(fileExportService.isDateValid(startDay, endDay));

    }
}
