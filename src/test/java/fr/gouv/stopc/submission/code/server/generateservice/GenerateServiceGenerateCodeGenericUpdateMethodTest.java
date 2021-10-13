package fr.gouv.stopc.submission.code.server.generateservice;

import fr.gouv.stopc.submission.code.server.business.controller.exception.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.business.dto.CodeDetailedDto;
import fr.gouv.stopc.submission.code.server.business.service.GenerateService;
import fr.gouv.stopc.submission.code.server.business.service.ShortCodeService;
import fr.gouv.stopc.submission.code.server.business.service.SubmissionCodeService;
import fr.gouv.stopc.submission.code.server.data.entity.SubmissionCode;
import fr.gouv.stopc.submission.code.server.data.repository.SubmissionCodeRepository;
import fr.gouv.stopc.submission.code.server.domain.enums.CodeTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenerateServiceGenerateCodeGenericUpdateMethodTest {

    @Mock
    private ShortCodeService shortCodeService;

    @Mock
    private SubmissionCodeRepository submissionCodeRepository;

    @Spy
    @InjectMocks
    private SubmissionCodeService submissionCodeService;

    @Spy
    @InjectMocks
    private GenerateService generateService;

    @BeforeEach
    public void init() {

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(this.generateService, "targetZoneId", "Europe/Paris");
        ReflectionTestUtils.setField(this.generateService, "numberOfTryInCaseOfError", 0);

        // SET 24 hours of lock security
        ReflectionTestUtils.setField(this.submissionCodeService, "securityTimeBetweenTwoUsagesOfShortCode", 24);
        ReflectionTestUtils.setField(this.generateService, "submissionCodeService", this.submissionCodeService);
    }

    /**
     * Simulate a same code insertion when validity date is not compliant with
     */
    @Test
    void testSameShortCodeAndSecurityDelayNotRespected() {
        // asserting gsi is available
        final long size = Long.parseLong("1");
        final CodeTypeEnum cte = CodeTypeEnum.SHORT;
        final OffsetDateTime validFrom = OffsetDateTime.now();

        Mockito.when(shortCodeService.generateCode())
                .thenReturn("5d98e3");

        final SubmissionCode submissionCode = new SubmissionCode();
        submissionCode.setId(1);
        submissionCode.setCode("5d98e3");

        Mockito.when(this.submissionCodeRepository.save(Mockito.any()))
                .thenThrow(DataIntegrityViolationException.class)
                .thenReturn(null);

        Mockito.when(
                this.submissionCodeRepository.findByCodeAndTypeAndAndDateEndValidityLessThan(
                        "5d98e3", cte.getTypeCode(), validFrom.minusHours(24)
                )
        ).thenReturn(null);

        assertThrows(
                SubmissionCodeServerException.class,
                () -> this.generateService.generateCodeGeneric(
                        size, cte, validFrom, null
                ),
                "Expected generateCodeGeneric() to throw, but it didn't"
        );

    }

    /**
     * Number of tries reach
     */
    @Test
    void testSameShortCodeAndSecurityDelayIsRespected() throws SubmissionCodeServerException {
        // asserting gsi is available
        final long size = Long.parseLong("1");
        final CodeTypeEnum cte = CodeTypeEnum.SHORT;
        final OffsetDateTime validFrom = OffsetDateTime.now();

        Mockito.when(shortCodeService.generateCode())
                .thenReturn("5d98e3");

        final SubmissionCode submissionCode = new SubmissionCode();
        submissionCode.setId(1);
        submissionCode.setCode("5d98e3");

        Mockito.when(this.submissionCodeRepository.save(Mockito.any()))
                .thenThrow(DataIntegrityViolationException.class)
                .thenReturn(submissionCode);

        Mockito.when(
                this.submissionCodeRepository.findByCodeAndTypeAndAndDateEndValidityLessThan(
                        "5d98e3", cte.getTypeCode(), validFrom.minusHours(24)
                )
        ).thenReturn(submissionCode);

        // try once
        final List<CodeDetailedDto> codeDetailedResponseDtoListFirst = this.generateService.generateCodeGeneric(
                size, cte, validFrom, null
        );

        assertEquals(
                codeDetailedResponseDtoListFirst.size(),
                size
        );

    }

}
