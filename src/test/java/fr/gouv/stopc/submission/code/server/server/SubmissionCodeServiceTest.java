package fr.gouv.stopc.submission.code.server.server;

import fr.gouv.stopc.submission.code.server.business.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.business.service.SubmissionCodeService;
import fr.gouv.stopc.submission.code.server.data.entity.SubmissionCode;
import fr.gouv.stopc.submission.code.server.data.repository.SubmissionCodeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Transactional
public class SubmissionCodeServiceTest {

    private SubmissionCodeRepository submissionCodeRepositoryMock = Mockito.mock(SubmissionCodeRepository.class);

    @Test
    public void testGetCodeValidity() {
        SubmissionCode submissionCode = new SubmissionCode();
        Mockito.when(submissionCodeRepositoryMock.findByCode(Mockito.anyString()))
                .thenReturn(submissionCode);
        SubmissionCodeService submissionCodeServiceImplTest = new SubmissionCodeService(
                submissionCodeRepositoryMock
        );
        String code = "test";
        Optional<SubmissionCodeDto> result = submissionCodeServiceImplTest.getCodeValidity(code);
        Assertions.assertTrue(result.isPresent());
    }

    @Test
    public void testSaveAllCodeGenerateByBatch() {
        List<SubmissionCode> submissionCodes = new ArrayList<>();
        SubmissionCode codePositive = new SubmissionCode();
        submissionCodes.add(codePositive);
        Mockito.when(submissionCodeRepositoryMock.saveAll(Mockito.anyList())).thenReturn(submissionCodes);
        SubmissionCodeService submissionCodeServiceTest = new SubmissionCodeService(
                submissionCodeRepositoryMock
        );
        List<SubmissionCodeDto> submissionCodeDtos = new ArrayList<>();
        SubmissionCodeDto submissionCodeDto = new SubmissionCodeDto();
        submissionCodeDtos.add(submissionCodeDto);
        Iterable<SubmissionCode> result = submissionCodeServiceTest.saveAllCodes(submissionCodeDtos);
        Assertions.assertTrue(StreamSupport.stream(result.spliterator(), false).count() != 0);
    }

    @Test
    public void testSaveCodeGenerate() {
        SubmissionCode submissionCode = new SubmissionCode();
        Mockito.when(submissionCodeRepositoryMock.save(submissionCode)).thenReturn(submissionCode);
        SubmissionCodeService submissionCodeServiceTest = new SubmissionCodeService(
                submissionCodeRepositoryMock
        );
        SubmissionCodeDto submissionCodeDto = new SubmissionCodeDto();
        Optional<SubmissionCode> result = submissionCodeServiceTest.saveCode(submissionCodeDto);
        Assertions.assertTrue(result.isPresent());

    }

    @Test
    public void testGetValidityEmpty() {
        SubmissionCode submissionCode = new SubmissionCode();
        Mockito.when(submissionCodeRepositoryMock.findByCode(Mockito.anyString()))
                .thenReturn(submissionCode);
        SubmissionCodeService submissionCodeService = new SubmissionCodeService(submissionCodeRepositoryMock);
        String code = "";
        Optional<SubmissionCodeDto> result = submissionCodeService.getCodeValidity(code);
        Assertions.assertTrue(!result.isPresent());
    }

    @Test
    public void testSaveAllCodeGenerateByBatchEmpty() {
        SubmissionCodeService codePositiveServiceTest = new SubmissionCodeService(submissionCodeRepositoryMock);
        Iterable<SubmissionCode> result = codePositiveServiceTest.saveAllCodes(new ArrayList<>());
        Assertions.assertNotNull(result);
    }

    @Test
    public void testUpdateCodeUsed() {
        SubmissionCodeDto submissionCodeDto = new SubmissionCodeDto();
        submissionCodeDto.setCode("test");
        submissionCodeDto.setType("test");
        SubmissionCode submissionCode = new SubmissionCode();
        Mockito.when(submissionCodeRepositoryMock.findByCode(Mockito.anyString()))
                .thenReturn(submissionCode);
        Mockito.when(submissionCodeRepositoryMock.save(submissionCode)).thenReturn(submissionCode);
        SubmissionCodeService submissionCodeService = new SubmissionCodeService(submissionCodeRepositoryMock);
        Assertions.assertTrue(submissionCodeService.updateCodeUsed(submissionCodeDto));
    }

    @Test
    public void testUpdateCodeUsedNotFound() {
        SubmissionCodeDto submissionCodeDto = new SubmissionCodeDto();
        submissionCodeDto.setCode("test");
        submissionCodeDto.setType("test");
        SubmissionCode submissionCode = new SubmissionCode();
        Mockito.when(submissionCodeRepositoryMock.findByCode(Mockito.anyString()))
                .thenReturn(null);
        Mockito.when(submissionCodeRepositoryMock.save(submissionCode)).thenReturn(submissionCode);
        SubmissionCodeService submissionCodeService = new SubmissionCodeService(submissionCodeRepositoryMock);
        Assertions.assertTrue(!submissionCodeService.updateCodeUsed(submissionCodeDto));
    }

}
