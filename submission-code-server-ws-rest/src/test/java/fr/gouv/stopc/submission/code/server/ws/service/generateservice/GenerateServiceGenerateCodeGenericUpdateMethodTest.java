package fr.gouv.stopc.submission.code.server.ws.service.generateservice;

import fr.gouv.stopc.submission.code.server.commun.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.commun.service.IAlphaNumericCodeService;
import fr.gouv.stopc.submission.code.server.database.service.impl.SubmissionCodeServiceImpl;
import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.ws.errors.NumberOfTryGenerateCodeExceededExcetion;
import fr.gouv.stopc.submission.code.server.ws.service.GenerateServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
class GenerateServiceGenerateCodeGenericUpdateMethodTest {


    @Autowired
    private SubmissionCodeServiceImpl submissionCodeService;

    @Mock
    private IAlphaNumericCodeService alphanumCodeService;


    @Spy
    @InjectMocks
    private GenerateServiceImpl gsiMocked;



    @Before
    public void init(){
        log.info("Initialize mokito injection in services...");
        MockitoAnnotations.initMocks(this);
    }


    /**
     * Simulate a same code insertion when validity date is not compliant with
     */
    @Test
    void samealphanumericAndSecurityDelayNotRespected() throws NumberOfTryGenerateCodeExceededExcetion {
        // asserting gsi is available
        final long size = Long.parseLong("1");
        final CodeTypeEnum cte = CodeTypeEnum.ALPHANUM_6;
        final OffsetDateTime validFrom = OffsetDateTime.now();

        Mockito.when(alphanumCodeService.generateCode())
                .thenReturn("5d98e3");

        ReflectionTestUtils.setField(this.gsiMocked, "TARGET_ZONE_ID", "Europe/Paris");
        ReflectionTestUtils.setField(this.gsiMocked, "NUMBER_OF_TRY_IN_CASE_OF_ERROR", 0);

        //SET 24 hours of lock security
        ReflectionTestUtils.setField(this.submissionCodeService, "SECURITY_TIME_BETWEEN_TWO_USAGES_OF_6_ALPHANUM_CODE", 24);

        ReflectionTestUtils.setField(this.gsiMocked, "submissionCodeService", this.submissionCodeService);

        // try once
        this.gsiMocked.generateCodeGeneric(
                size, cte, validFrom, null
        );

        NumberOfTryGenerateCodeExceededExcetion notgcee = null;

        // try twice and do raise error because security time is not over
        try {
            this.gsiMocked.generateCodeGeneric(
                    size, cte, validFrom.plusHours(24).minusMinutes(1), null
            );
        } catch (  NumberOfTryGenerateCodeExceededExcetion e ) {
            log.error("{}", e);
            notgcee = e;
            assertEquals(String.format("Number of tries exceeded. %s were authorized.", 0), e.getMessage());
        }

        assertNotNull(notgcee);
    }


    /**
     * Number of tries reach
     */
    @Test
    void samealphanumericAndSecurityDelayIsRespected() throws NumberOfTryGenerateCodeExceededExcetion {
        // asserting gsi is available
        final long size = Long.parseLong("1");
        final CodeTypeEnum cte = CodeTypeEnum.ALPHANUM_6;
        final OffsetDateTime validFrom = OffsetDateTime.now();

        Mockito.when(alphanumCodeService.generateCode())
                .thenReturn("A1B212");

        ReflectionTestUtils.setField(this.gsiMocked, "TARGET_ZONE_ID", "Europe/Paris");
        ReflectionTestUtils.setField(this.gsiMocked, "NUMBER_OF_TRY_IN_CASE_OF_ERROR", 0);

        //SET 24 hours of lock security
        ReflectionTestUtils.setField(this.submissionCodeService, "SECURITY_TIME_BETWEEN_TWO_USAGES_OF_6_ALPHANUM_CODE", 24);

        ReflectionTestUtils.setField(this.gsiMocked, "submissionCodeService", this.submissionCodeService);

        // try once
        final List<GenerateResponseDto> generateResponseDtoListFirst = this.gsiMocked.generateCodeGeneric(
                size, cte, validFrom, null
        );


        // try twice and do not raise error because security time is over
        final List<GenerateResponseDto> generateResponseDtoListSecond = this.gsiMocked.generateCodeGeneric(
                size, cte, validFrom.plusHours(24).plusMinutes(1), null
        );


        assertEquals(
                generateResponseDtoListSecond.size(),
                generateResponseDtoListFirst.size()
        );
        assertEquals(
                generateResponseDtoListSecond.get(0).getCode(),
                generateResponseDtoListFirst.get(0).getCode()
        );

        assertNotEquals(
                generateResponseDtoListSecond.get(0).getValidFrom(),
                generateResponseDtoListFirst.get(0).getValidFrom()
        );
        assertNotEquals(
                generateResponseDtoListSecond.get(0).getValidUntil(),
                generateResponseDtoListFirst.get(0).getValidUntil()
        );

    }


}