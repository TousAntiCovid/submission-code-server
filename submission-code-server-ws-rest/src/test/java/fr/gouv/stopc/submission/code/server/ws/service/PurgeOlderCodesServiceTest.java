package fr.gouv.stopc.submission.code.server.ws.service;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import fr.gouv.stopc.submission.code.server.commun.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import fr.gouv.stopc.submission.code.server.ws.SubmissionCodeServerClientApiWsRestApplication;

@SpringBootTest(classes = {
        SubmissionCodeServerClientApiWsRestApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.profiles.active=test", "async.enabled=false"})
public class PurgeOlderCodesServiceTest {

    @Autowired
    private IPurgeOlderCodesService purgeOlderCodes;

    @Autowired
    private ISubmissionCodeService submissionCodeService;

    @Test
    public void testPurgeShouldNotRemoveUsedShortCode() {

        // Given
        String codeValue = (UUID.randomUUID()).toString();
        OffsetDateTime now = OffsetDateTime.now();
        SubmissionCodeDto submissionCodeDto = SubmissionCodeDto.builder()
                .code(codeValue)
                .type(CodeTypeEnum.SHORT.getTypeCode())
                .dateAvailable(now.minusMinutes(10))
                .dateEndValidity(now.plusMinutes(50))
                .dateGeneration(now.minusMinutes(10))
                .dateUse(now.minusMinutes(5))
                .used(true)
                .build();

        this.submissionCodeService.saveCode(submissionCodeDto);
        Optional<SubmissionCodeDto> expectedSubmissionCode = this.submissionCodeService.getCodeValidity(codeValue, CodeTypeEnum.SHORT);
        assertTrue(expectedSubmissionCode.isPresent());

        // When
        this.purgeOlderCodes.deleteExpiredCodes();

        // Then
        Optional<SubmissionCodeDto> submissionCode = this.submissionCodeService.getCodeValidity(codeValue, CodeTypeEnum.SHORT);
        assertTrue(submissionCode.isPresent());
    }

    @Test
    public void testPurgeShouldNotRemoveExpiredShortCode() {

        // Given
        String codeValue = (UUID.randomUUID()).toString();
        OffsetDateTime now = OffsetDateTime.now();
        SubmissionCodeDto submissionCodeDto = SubmissionCodeDto.builder()
                .code(codeValue)
                .type(CodeTypeEnum.SHORT.getTypeCode())
                .dateAvailable(now.minusHours(3))
                .dateEndValidity(now.minusHours(2))
                .dateGeneration(now.minusHours(3))
                .used(false)
                .build();

        this.submissionCodeService.saveCode(submissionCodeDto);

        // When
        this.purgeOlderCodes.deleteExpiredCodes();

        // Then
        Optional<SubmissionCodeDto> submissionCode = this.submissionCodeService.getCodeValidity(codeValue, CodeTypeEnum.SHORT);
        assertTrue(submissionCode.isPresent());
    }

    @Test
    public void testPurgeShouldNotRemoveValidShortCode() {

        // Given
        String codeValue = (UUID.randomUUID()).toString();
        OffsetDateTime now = OffsetDateTime.now();
        SubmissionCodeDto submissionCodeDto = SubmissionCodeDto.builder()
                .code(codeValue)
                .type(CodeTypeEnum.SHORT.getTypeCode())
                .dateAvailable(now)
                .dateEndValidity(now.plusHours(1))
                .dateGeneration(now)
                .used(false)
                .build();

        this.submissionCodeService.saveCode(submissionCodeDto);

        // When
        this.purgeOlderCodes.deleteExpiredCodes();

        // Then
        Optional<SubmissionCodeDto> submissionCode = this.submissionCodeService.getCodeValidity(codeValue, CodeTypeEnum.SHORT);
        assertTrue(submissionCode.isPresent());
    }

    @Test
    public void testPurgeShouldNotRemoveUsedLongCode() {

        // Given
        String codeValue = (UUID.randomUUID()).toString();
        OffsetDateTime now = OffsetDateTime.now();
        SubmissionCodeDto submissionCodeDto = SubmissionCodeDto.builder()
                .lot(0L)
                .code(codeValue)
                .type(CodeTypeEnum.LONG.getTypeCode())
                .dateAvailable(now.minusDays(6))
                .dateEndValidity(now.plusDays(1))
                .dateGeneration(now.minusDays(6))
                .dateUse(now.minusDays(2))
                .used(true)
                .build();

        this.submissionCodeService.saveCode(submissionCodeDto);
        Optional<SubmissionCodeDto> expectedSubmissionCode = this.submissionCodeService.getCodeValidity(codeValue, CodeTypeEnum.LONG);
        assertTrue(expectedSubmissionCode.isPresent());

        // When
        this.purgeOlderCodes.deleteExpiredCodes();

        // Then
        Optional<SubmissionCodeDto> submissionCode = this.submissionCodeService.getCodeValidity(codeValue, CodeTypeEnum.LONG);
        assertTrue(submissionCode.isPresent());
    }

    @Test
    public void testPurgeShouldNotRemoveValidLongCode() {

        // Given
        String codeValue = (UUID.randomUUID()).toString();
        OffsetDateTime now = OffsetDateTime.now();
        SubmissionCodeDto submissionCodeDto = SubmissionCodeDto.builder()
                .code(codeValue)
                .type(CodeTypeEnum.LONG.getTypeCode())
                .dateAvailable(now.minusDays(6))
                .dateEndValidity(now.plusDays(1))
                .dateGeneration(now.minusDays(6))
                .used(false)
                .build();

        this.submissionCodeService.saveCode(submissionCodeDto);
        Optional<SubmissionCodeDto> expectedSubmissionCode = this.submissionCodeService.getCodeValidity(codeValue, CodeTypeEnum.LONG);
        assertTrue(expectedSubmissionCode.isPresent());

        // When
        this.purgeOlderCodes.deleteExpiredCodes();

        // Then
        Optional<SubmissionCodeDto> submissionCode = this.submissionCodeService.getCodeValidity(codeValue, CodeTypeEnum.LONG);
        assertTrue(submissionCode.isPresent());
    }

    @Test
    public void testPurgeShouldRemoveExpiredLongCode() {

        // Given
        String codeValue = (UUID.randomUUID()).toString();
        OffsetDateTime now = OffsetDateTime.now();
        SubmissionCodeDto submissionCodeDto = SubmissionCodeDto.builder()
                .lot(0L)
                .code(codeValue)
                .type(CodeTypeEnum.LONG.getTypeCode())
                .dateAvailable(now.minusDays(7))
                .dateEndValidity(now)
                .dateGeneration(now.minusDays(7))
                .used(false)
                .build();

        this.submissionCodeService.saveCode(submissionCodeDto);
        Optional<SubmissionCodeDto> expectedSubmissionCode = this.submissionCodeService.getCodeValidity(codeValue, CodeTypeEnum.LONG);
        assertTrue(expectedSubmissionCode.isPresent());

        // When
        this.purgeOlderCodes.deleteExpiredCodes();

        // Then
        Optional<SubmissionCodeDto> submissionCode = this.submissionCodeService.getCodeValidity(codeValue, CodeTypeEnum.LONG);
        assertFalse(submissionCode.isPresent());
    }

    @Test
    public void testPurgeShouldRemoveExpiredLongCodeAndNotRemoveUsedLongCode() {

        // Given
        String firstCodeValue = (UUID.randomUUID()).toString();
        String secondCodeValue = (UUID.randomUUID()).toString();
        OffsetDateTime now = OffsetDateTime.now();
        SubmissionCodeDto expiredCodeDto = SubmissionCodeDto.builder()
                .lot(1L)
                .code(firstCodeValue)
                .type(CodeTypeEnum.LONG.getTypeCode())
                .dateAvailable(now.minusDays(7))
                .dateEndValidity(now)
                .dateGeneration(now.minusDays(7))
                .used(false)
                .build();

        SubmissionCodeDto usedCodeDto = SubmissionCodeDto.builder()
                .lot(1L)
                .code(secondCodeValue)
                .type(CodeTypeEnum.LONG.getTypeCode())
                .dateAvailable(now.minusDays(6))
                .dateEndValidity(now.plusDays(1))
                .dateGeneration(now.minusDays(6))
                .used(true)
                .build();

        List<SubmissionCodeDto> codeDtoList = new ArrayList<>();
        codeDtoList.add(expiredCodeDto);
        codeDtoList.add(usedCodeDto);
        this.submissionCodeService.saveAllCodes(codeDtoList);
        Optional<SubmissionCodeDto> expectedExpiredSubmissionCode = this.submissionCodeService.getCodeValidity(firstCodeValue, CodeTypeEnum.LONG);
        Optional<SubmissionCodeDto> expectedUsedSubmissionCode = this.submissionCodeService.getCodeValidity(secondCodeValue, CodeTypeEnum.LONG);
        assertTrue(expectedExpiredSubmissionCode.isPresent());
        assertTrue(expectedUsedSubmissionCode.isPresent());

        // When
        this.purgeOlderCodes.deleteExpiredCodes();

        // Then
        Optional<SubmissionCodeDto> expiredSubmissionCode = this.submissionCodeService.getCodeValidity(firstCodeValue, CodeTypeEnum.LONG);
        Optional<SubmissionCodeDto> usedSubmissionCode = this.submissionCodeService.getCodeValidity(secondCodeValue, CodeTypeEnum.LONG);
        assertFalse(expiredSubmissionCode.isPresent());
        assertTrue(usedSubmissionCode.isPresent());
    }
}
