package test.fr.gouv.stopc.submission.code.server.database.server;

import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;
import fr.gouv.stopc.submission.code.server.database.repository.SubmissionCodeRepository;
import fr.gouv.stopc.submission.code.server.database.service.impl.SubmissionCodeServiceImpl;

import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.internal.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class SubmissionCodeServiceImplTest {
    private SubmissionCodeRepository submissionCodeRepositoryMock = Mockito.mock(SubmissionCodeRepository.class);

    @Test
    public void getCodeValidity() {
        SubmissionCode submissionCode = new SubmissionCode();
        Mockito.when(submissionCodeRepositoryMock.findByCodeAndType(Mockito.anyString(),Mockito.anyString())).thenReturn(submissionCode);
        SubmissionCodeServiceImpl submissionCodeServiceImplTestt = new SubmissionCodeServiceImpl(submissionCodeRepositoryMock);
        String code = "test";
        Optional<SubmissionCodeDto> result = submissionCodeServiceImplTestt.getCodeValidity(code,"test");
        Assert.notNull(result.get());
    }

    @Test
    public void saveAllCodeGenerateByBatch() {
        List<SubmissionCode> submissionCodes = new ArrayList<>();
        SubmissionCode codepositive = new SubmissionCode();
        submissionCodes.add(codepositive);
        Mockito.when(submissionCodeRepositoryMock.saveAll(submissionCodes)).thenReturn(submissionCodes);
        SubmissionCodeServiceImpl submissionCodeServiceTest = new SubmissionCodeServiceImpl(submissionCodeRepositoryMock);
        List<SubmissionCodeDto> submissionCodeDtos = new ArrayList<>();
        SubmissionCodeDto submissionCodeDto = new SubmissionCodeDto();
        submissionCodeDtos.add(submissionCodeDto);
        Iterable<SubmissionCode> result = submissionCodeServiceTest.saveAllCodes(submissionCodeDtos);
        Assert.isTrue(IterableUtils.size(result) != 0);
    }

    @Test
    public void saveCodeGenerate() {
        SubmissionCode submissionCode = new SubmissionCode();
        Mockito.when(submissionCodeRepositoryMock.save(submissionCode)).thenReturn(submissionCode);
        SubmissionCodeServiceImpl submissionCodeServiceTest = new SubmissionCodeServiceImpl(submissionCodeRepositoryMock);
        SubmissionCodeDto submissionCodeDto = new SubmissionCodeDto();
        SubmissionCode result= submissionCodeServiceTest.saveCode(submissionCodeDto);
        Assert.isTrue(result != null);
    }

    @Test
    public void getValidityEmpty(){
        SubmissionCode submissionCode = new SubmissionCode();
        Mockito.when(submissionCodeRepositoryMock.findByCodeAndType(Mockito.anyString(),Mockito.anyString())).thenReturn(submissionCode);
        SubmissionCodeServiceImpl submissionCodeService = new SubmissionCodeServiceImpl(submissionCodeRepositoryMock);
        String code = "";
        Optional<SubmissionCodeDto> result = submissionCodeService.getCodeValidity(code, "test");
        Assert.isTrue(!result.isPresent());
    }

    @Test
    public void saveAllCodeGenerateByBatchEmpty() {
        SubmissionCodeServiceImpl codePositiveServiceTest = new SubmissionCodeServiceImpl(submissionCodeRepositoryMock);
        Iterable<SubmissionCode> result = codePositiveServiceTest.saveAllCodes(new ArrayList<>());
        Assert.isTrue(result == null);
    }


    @Test
    public void updateCodeUsed(){
        SubmissionCodeDto submissionCodeDto = new SubmissionCodeDto();
        submissionCodeDto.setCode("test");
        submissionCodeDto.setType("test");
        SubmissionCode submissionCode = new SubmissionCode();
        Mockito.when(submissionCodeRepositoryMock.findByCodeAndType(Mockito.anyString(),Mockito.anyString())).thenReturn(submissionCode);
        Mockito.when(submissionCodeRepositoryMock.save(submissionCode)).thenReturn(submissionCode);
        SubmissionCodeServiceImpl submissionCodeService = new SubmissionCodeServiceImpl(submissionCodeRepositoryMock);
        Assert.isTrue(submissionCodeService.updateCodeUsed(submissionCodeDto));
    }
    @Test
    public void updateCodeUsedNotFound(){
        SubmissionCodeDto submissionCodeDto = new SubmissionCodeDto();
        submissionCodeDto.setCode("test");
        submissionCodeDto.setType("test");
        SubmissionCode submissionCode = new SubmissionCode();
        Mockito.when(submissionCodeRepositoryMock.findByCodeAndType(Mockito.anyString(),Mockito.anyString())).thenReturn(null);
        Mockito.when(submissionCodeRepositoryMock.save(submissionCode)).thenReturn(submissionCode);
        SubmissionCodeServiceImpl submissionCodeService = new SubmissionCodeServiceImpl(submissionCodeRepositoryMock);
        Assert.isTrue(!submissionCodeService.updateCodeUsed(submissionCodeDto));
    }

}
