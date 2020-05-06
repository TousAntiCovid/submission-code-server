package fr.gouv.stopc.submission.code.server.ws.service.fileservice;

import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class CsvServiceTest {
/*
    private ISubmissionCodeService submissionCodeServiceMock = Mockito.mock(ISubmissionCodeService.class);

    @Test
    public void testLotEmpty(){
        String type = CodeTypeEnum.UUIDv4.getTypeCode();
        String lot="1";
        Mockito.when(submissionCodeServiceMock.getCodeUUIDv4CodesForCsv(lot,type)).thenReturn(new ArrayList<>());
        FileExportServiceImpl csvExportService = new FileExportServiceImpl(submissionCodeServiceMock);
        Optional<StringWriter> result = csvExportService.csvExport(lot);
        Assert.isTrue(!result.isPresent());

    }
    @Test
    public void testLotNotEmpty(){
        String type = CodeTypeEnum.UUIDv4.getTypeCode();
        String lot="1";
        List<SubmissionCodeDto> codesTest = new ArrayList<>();
        codesTest= init(codesTest);
        Mockito.when(submissionCodeServiceMock.getCodeUUIDv4CodesForCsv(lot,type)).thenReturn(codesTest);
        FileExportServiceImpl csvExportService = new FileExportServiceImpl(submissionCodeServiceMock);
        Optional<StringWriter> result = csvExportService.csvExport(lot);
        Assert.isTrue(result.isPresent());


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
*/
}
