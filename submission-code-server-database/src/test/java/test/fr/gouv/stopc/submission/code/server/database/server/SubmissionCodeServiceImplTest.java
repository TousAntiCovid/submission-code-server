package test.fr.gouv.stopc.submission.code.server.database.server;

import fr.gouv.stopc.submission.code.server.commun.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;
import fr.gouv.stopc.submission.code.server.database.repository.SubmissionCodeRepository;
import fr.gouv.stopc.submission.code.server.database.service.impl.SubmissionCodeServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class SubmissionCodeServiceImplTest {

    @InjectMocks
    private SubmissionCodeServiceImpl submissionCodeService;

    @Mock
    private SubmissionCodeRepository submissionCodeRepository;

    @Test
    public void testGetCodeValidity() {
        SubmissionCode submissionCode = new SubmissionCode();
        when(submissionCodeRepository.findByCodeAndType(Mockito.anyString(), Mockito.anyString())).thenReturn(submissionCode);
        SubmissionCodeServiceImpl submissionCodeServiceImplTest = new SubmissionCodeServiceImpl(submissionCodeRepository);
        String code = "test";
        Optional<SubmissionCodeDto> result = submissionCodeServiceImplTest.getCodeValidity(code, CodeTypeEnum.LONG);
        Assertions.assertTrue(result.isPresent());
    }

    @Test
    public void testSaveAllCodeGenerateByBatch() {
        List<SubmissionCode> submissionCodes = new ArrayList<>();
        SubmissionCode codePositive = new SubmissionCode();
        submissionCodes.add(codePositive);
        when(submissionCodeRepository.saveAll(Mockito.anyList())).thenReturn(submissionCodes);
        SubmissionCodeServiceImpl submissionCodeServiceTest = new SubmissionCodeServiceImpl(submissionCodeRepository);
        List<SubmissionCodeDto> submissionCodeDtos = new ArrayList<>();
        SubmissionCodeDto submissionCodeDto = new SubmissionCodeDto();
        submissionCodeDtos.add(submissionCodeDto);
        Iterable<SubmissionCode> result = submissionCodeServiceTest.saveAllCodes(submissionCodeDtos);
        Assertions.assertTrue(StreamSupport.stream(result.spliterator(), false).count() != 0);
    }

    @Test
    public void testSaveCodeGenerate() {
        SubmissionCode submissionCode = new SubmissionCode();
        when(submissionCodeRepository.save(submissionCode)).thenReturn(submissionCode);
        SubmissionCodeServiceImpl submissionCodeServiceTest = new SubmissionCodeServiceImpl(submissionCodeRepository);
        SubmissionCodeDto submissionCodeDto = new SubmissionCodeDto();
        Optional<SubmissionCode> result = submissionCodeServiceTest.saveCode(submissionCodeDto);
        Assertions.assertTrue(result.isPresent());

    }

    @Test
    public void testGetValidityEmpty() {
        SubmissionCode submissionCode = new SubmissionCode();
        when(submissionCodeRepository.findByCodeAndType(Mockito.anyString(), Mockito.anyString())).thenReturn(submissionCode);
        SubmissionCodeServiceImpl submissionCodeService = new SubmissionCodeServiceImpl(submissionCodeRepository);
        String code = "";
        Optional<SubmissionCodeDto> result = submissionCodeService.getCodeValidity(code, CodeTypeEnum.LONG);
        Assertions.assertTrue(!result.isPresent());
    }

    @Test
    public void testSaveAllCodeGenerateByBatchEmpty() {
        SubmissionCodeServiceImpl codePositiveServiceTest = new SubmissionCodeServiceImpl(submissionCodeRepository);
        Iterable<SubmissionCode> result = codePositiveServiceTest.saveAllCodes(new ArrayList<>());
        Assertions.assertNotNull(result);
    }


    @Test
    public void testUpdateCodeUsed() {
        SubmissionCodeDto submissionCodeDto = new SubmissionCodeDto();
        submissionCodeDto.setCode("test");
        submissionCodeDto.setType("test");
        SubmissionCode submissionCode = new SubmissionCode();
        when(submissionCodeRepository.findByCodeAndType(Mockito.anyString(), Mockito.anyString())).thenReturn(submissionCode);
        when(submissionCodeRepository.save(submissionCode)).thenReturn(submissionCode);
        SubmissionCodeServiceImpl submissionCodeService = new SubmissionCodeServiceImpl(submissionCodeRepository);
        Assertions.assertTrue(submissionCodeService.updateCodeUsed(submissionCodeDto));
    }

    @Test
    public void testUpdateCodeUsedNotFound() {
        SubmissionCodeDto submissionCodeDto = new SubmissionCodeDto();
        submissionCodeDto.setCode("test");
        submissionCodeDto.setType("test");
        SubmissionCode submissionCode = new SubmissionCode();
        when(submissionCodeRepository.findByCodeAndType(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
        when(submissionCodeRepository.save(submissionCode)).thenReturn(submissionCode);
        SubmissionCodeServiceImpl submissionCodeService = new SubmissionCodeServiceImpl(submissionCodeRepository);
        Assertions.assertTrue(!submissionCodeService.updateCodeUsed(submissionCodeDto));
    }

    @Test
    public void testDeleteOlderCodesWhenTypeIsNull() {

        // Given
        OffsetDateTime dateEndValidity = OffsetDateTime.now();

        // When
        long nbDeleted = this.submissionCodeService.deleteExpiredCodes(null, dateEndValidity);

        // Then
        Assertions.assertEquals(0L, nbDeleted);
        verify(this.submissionCodeRepository, never()).deleteCodeByTypeAndDateEndValidityLessThanAndUsedIs(null, dateEndValidity, false);
    }

    @Test
    public void testDeleteOlderCodesWhenDateEndValidityIsNull() {

        // Given
        String type = CodeTypeEnum.LONG.getTypeCode();

        // When
        long nbDeleted = this.submissionCodeService.deleteExpiredCodes(CodeTypeEnum.LONG, null);

        // Then
        Assertions.assertEquals(0L, nbDeleted);
        verify(this.submissionCodeRepository, never()).deleteCodeByTypeAndDateEndValidityLessThanAndUsedIs(type, null, false);
    }

    @Test
    public void testDeleteOlderCodes() {

        // Given
        OffsetDateTime now = OffsetDateTime.now();
        CodeTypeEnum type = CodeTypeEnum.LONG;
        when(this.submissionCodeRepository.deleteCodeByTypeAndDateEndValidityLessThanAndUsedIs(type.getTypeCode(), now, false)).thenReturn(1L);

        // When
        long nbDeleted = this.submissionCodeService.deleteExpiredCodes(CodeTypeEnum.LONG, now);

        // Then
        Assertions.assertEquals(1L, nbDeleted);
        verify(this.submissionCodeRepository).deleteCodeByTypeAndDateEndValidityLessThanAndUsedIs(type.getTypeCode(), now, false);
    }
}
