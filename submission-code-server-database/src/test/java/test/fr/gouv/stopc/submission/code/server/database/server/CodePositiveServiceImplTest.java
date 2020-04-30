package test.fr.gouv.stopc.submission.code.server.database.server;

import fr.gouv.stopc.submission.code.server.database.dto.CodePositiveDto;
import fr.gouv.stopc.submission.code.server.database.entity.CodePositive;
import fr.gouv.stopc.submission.code.server.database.repository.CodePositiveRepository;
import fr.gouv.stopc.submission.code.server.database.service.impl.CodePositiveServiceImpl;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.internal.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class CodePositiveServiceImplTest {
    private CodePositiveRepository codePositiveRepositoryMock= Mockito.mock(CodePositiveRepository.class);

    @Test
    public void getCodeValidity() {
        CodePositive codepositive = new CodePositive();
        Mockito.when(codePositiveRepositoryMock.findByCode(Mockito.anyString())).thenReturn(codepositive);
        CodePositiveServiceImpl codePositiveServiceTest = new CodePositiveServiceImpl(codePositiveRepositoryMock);
        String code = "test";
        Optional<CodePositiveDto> result = codePositiveServiceTest.getCodeValidity(code);
        Assert.notNull(result.get());
    }

    @Test
    public void saveAllCodeGenerateByBatch() {
        List<CodePositive> codepositives = new ArrayList<>();
        CodePositive codepositive = new CodePositive();
        codepositives.add(codepositive);
        Mockito.when(codePositiveRepositoryMock.saveAll(codepositives)).thenReturn(codepositives);
        CodePositiveServiceImpl codePositiveServiceTest = new CodePositiveServiceImpl(codePositiveRepositoryMock);
        List<CodePositiveDto> codepositiveDtos = new ArrayList<>();
        CodePositiveDto codepositiveDto = new CodePositiveDto();
        codepositiveDtos.add(codepositiveDto);
        boolean result = codePositiveServiceTest.saveAllCodeGenerateByBatch(codepositiveDtos);
        Assert.isTrue(result);
    }

    @Test
    public void saveCodeGenerate() {
        CodePositive codePositive = new CodePositive();
        Mockito.when(codePositiveRepositoryMock.save(codePositive)).thenReturn(codePositive);
        CodePositiveServiceImpl codePositiveServiceTest = new CodePositiveServiceImpl(codePositiveRepositoryMock);
        CodePositiveDto codePositiveDto = new CodePositiveDto();
        boolean result= codePositiveServiceTest.saveCodeGenerate(codePositiveDto);
        Assert.isTrue(result);
    }

    @Test
    public void getValidityEmpty(){
        CodePositive codepositive = new CodePositive();
        Mockito.when(codePositiveRepositoryMock.findByCode(Mockito.anyString())).thenReturn(codepositive);
        CodePositiveServiceImpl codePositiveServiceTest = new CodePositiveServiceImpl(codePositiveRepositoryMock);
        String code = "";
        Optional<CodePositiveDto> result = codePositiveServiceTest.getCodeValidity(code);
        Assert.isTrue(!result.isPresent());
    }

    @Test
    public void saveAllCodeGenerateByBatchEmpty() {
        List<CodePositive> codepositives = new ArrayList<>();
        CodePositiveServiceImpl codePositiveServiceTest = new CodePositiveServiceImpl(codePositiveRepositoryMock);
        boolean result = codePositiveServiceTest.saveAllCodeGenerateByBatch(new ArrayList<>());
        Assert.isTrue(!result);
    }
}
