package fr.gouv.stopc.submission.code.server.ws.service.cvsservice;

import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;
import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import fr.gouv.stopc.submission.code.server.ws.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.ws.service.CsvExportServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.internal.util.Assert;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class CsvServiceTest {

    private ISubmissionCodeService submissionCodeServiceMock = Mockito.mock(ISubmissionCodeService.class);

    @Test
    public void testLotEmpty(){
        String type = CodeTypeEnum.UUIDv4.getTypeCode();
        String lot="1";
        Mockito.when(submissionCodeServiceMock.getCodeUUIDv4CodesForCsv(lot,type)).thenReturn(new ArrayList<>());
        CsvExportServiceImpl csvExportService = new CsvExportServiceImpl(submissionCodeServiceMock);
        File file = csvExportService.csvExport(lot);
        Assert.isNull(file);

    }
    @Test
    public void testLotNotEmpty(){
        String type = CodeTypeEnum.UUIDv4.getTypeCode();
        String lot="1";
        List<SubmissionCodeDto> codesTest = new ArrayList<>();
        codesTest= init(codesTest);
        Mockito.when(submissionCodeServiceMock.getCodeUUIDv4CodesForCsv(lot,type)).thenReturn(codesTest);
        CsvExportServiceImpl csvExportService = new CsvExportServiceImpl(submissionCodeServiceMock);
        File file = csvExportService.csvExport(lot);
        Assert.notNull(file.getName());
        Assert.notNull(file.length());


    }

    private List<SubmissionCodeDto> init(List<SubmissionCodeDto> codesTest) {
        String csvFile = "src/test/java/fr/gouv/stopc/submission/code/server/ws/service/cvsservice/test.csv";
        BufferedReader bufferedReader = null;
        String line = "";
        String csvSplit = ",";
        try {
            bufferedReader = new BufferedReader(new FileReader(csvFile));
            while ((line = bufferedReader.readLine()) != null) {
                String[] submissionCodeSplit = line.split(csvSplit);
                SubmissionCodeDto submissionCode = SubmissionCodeDto.builder().used(false).dateUse(null)
                        .lot(Long.parseLong(submissionCodeSplit[0]))
                        .code(submissionCodeSplit[1])
                        .type(submissionCodeSplit[2])
                        .dateEndValidity(OffsetDateTime.parse(submissionCodeSplit[3]))
                        .dateAvailable(OffsetDateTime.parse(submissionCodeSplit[4])).build();
                codesTest.add(submissionCode);
            }

        } catch (Exception e) {

        }
        return codesTest;
    }

}
