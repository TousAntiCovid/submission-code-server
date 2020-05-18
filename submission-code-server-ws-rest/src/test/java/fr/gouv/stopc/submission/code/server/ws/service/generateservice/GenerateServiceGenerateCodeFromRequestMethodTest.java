package fr.gouv.stopc.submission.code.server.ws.service.generateservice;

import fr.gouv.stopc.submission.code.server.commun.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.commun.service.impl.AlphaNumericCodeServiceImpl;
import fr.gouv.stopc.submission.code.server.commun.service.impl.UUIDv4CodeServiceImpl;
import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.entity.Lot;
import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;
import fr.gouv.stopc.submission.code.server.database.service.impl.SubmissionCodeServiceImpl;
import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.ws.dto.CodeDetailedDto;
import fr.gouv.stopc.submission.code.server.ws.service.impl.GenerateServiceImpl;
import fr.gouv.stopc.submission.code.server.ws.vo.GenerateRequestVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerateServiceGenerateCodeFromRequestMethodTest {

    @Mock
    private SubmissionCodeServiceImpl submissionCodeService;

    @Spy
    @InjectMocks
    private GenerateServiceImpl generateService;


    @BeforeEach
    public void init(){

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(this.generateService, "targetZoneId", "Europe/Paris");
        ReflectionTestUtils.setField(this.generateService, "numberOfTryInCaseOfError", 0);

        //SET 24 hours of lock security
        ReflectionTestUtils.setField(this.submissionCodeService, "securityTimeBetweenTwoUsagesOf6AlphanumCode", 24);
        ReflectionTestUtils.setField(this.generateService, "uuiDv4CodeService", new UUIDv4CodeServiceImpl());
        ReflectionTestUtils.setField(this.generateService, "alphaNumericCodeService", new AlphaNumericCodeServiceImpl());
    }

    /**
     * Calling generateCodeFromRequest with generateRequestVo == null
     */
    @Test
    void testWithNull()
    {
        assertThrows(
                SubmissionCodeServerException.class,
                () -> this.generateService.generateCodeFromRequest(
                        null),
                "Expected doThing() to throw, but it didn't"
        );
    }

    /**
     * Calling generateCodeFromRequest with generateRequestVo.type == null
     */
    @Test
    void testWithTypeNull()
    {
        assertThrows(
                SubmissionCodeServerException.class,
                () -> this.generateService.generateCodeFromRequest(
                        GenerateRequestVo.builder().type(null).build()),
                "Expected doThing() to throw, but it didn't"
        );
    }


    /**
     * Calling generateCodeFromRequest with generateRequestVo.type == " "
     */
    @Test
    void testWithTypeBlank()
    {
        assertThrows(
                SubmissionCodeServerException.class,
                () -> this.generateService.generateCodeFromRequest(
                        GenerateRequestVo.builder().type(" ").build()),
                "Expected doThing() to throw, but it didn't"
        );
    }

    /**
     * Calling generateCodeFromRequest with generateRequestVo.type == "NOT_AN_CODE_TYPE_ENUM"
     */
    @Test
    void testWithTypeNotCodeTypeEnum()
    {
        assertThrows(
                SubmissionCodeServerException.class,
                () -> this.generateService.generateCodeFromRequest(
                        GenerateRequestVo.builder().type("NOT_AN_CODE_TYPE_ENUM").build()),
                "Expected doThing() to throw, but it didn't"
        );
    }

    /**
     * Calling generateCodeFromRequest with generateRequestVo.type == CodeTypeEnum.ALPHANUM_6.getTypeCode()
     */
    @Test
    void testWithType6ALPHANUM() throws SubmissionCodeServerException {

        SubmissionCodeServerException udte = null;
        Mockito.when(this.submissionCodeService.saveCode(Mockito.any(), Mockito.any()))
                .thenReturn(Optional.of(new SubmissionCode()));

        final List<CodeDetailedDto> codeDetailedResponseDtoList = this.generateService.generateCodeFromRequest(
                GenerateRequestVo.builder()
                        .type(CodeTypeEnum.ALPHANUM_6.getTypeCode())
                        .build()
        );

        assertTrue(codeDetailedResponseDtoList.size()==1);
    }

    /**
     * Calling generateCodeFromRequest with generateRequestVo.type == CodeTypeEnum.UUIDv4.getTypeCode()
     */
    @Test
    void testWithTypeUUIDv4() throws SubmissionCodeServerException {
        SubmissionCodeServerException udte = null;

        ReflectionTestUtils.setField(this.generateService, "numberOfUuidv4PerCall", 2);

        Mockito.when(this.submissionCodeService.saveCode(Mockito.any(), Mockito.any()))
                .thenReturn(Optional.of(new SubmissionCode()));

        final List<CodeDetailedDto> codeDetailedResponseDtoList = this.generateService.generateCodeFromRequest(
                GenerateRequestVo.builder()
                        .type(CodeTypeEnum.UUIDv4.getTypeCode())
                        .build()
        );

        assertTrue(codeDetailedResponseDtoList.size() == 2);
    }


    /**
     * generateCodeFromRequest with Number of tries reach UUIDv4
     */
    @Test
    void testGenerateCodeFromRequestReachTriesUUIDv4()
    {


        Mockito.when(submissionCodeService.saveCode(Mockito.any(), Mockito.any()))
                .thenThrow(DataIntegrityViolationException.class);

        ReflectionTestUtils.setField(generateService, "numberOfTryInCaseOfError", 0);
        ReflectionTestUtils.setField(generateService, "numberOfUuidv4PerCall", 2);

        assertThrows(
                SubmissionCodeServerException.class,
                () -> this.generateService.generateCodeFromRequest(
                        GenerateRequestVo.builder()
                                .type(CodeTypeEnum.UUIDv4.getTypeCode())
                                .build()),
                "Expected doThing() to throw, but it didn't"
        );
    }

    /**
     * generateCodeFromRequest with Number of tries reach ALPHANUM
     */
    @Test
    void testGenerateCodeFromRequestReachTries6ALPHANUM()
    {
        Mockito.when(submissionCodeService.saveCode(Mockito.any(SubmissionCodeDto.class), Mockito.any(Lot.class)))
                .thenThrow(DataIntegrityViolationException.class);


        ReflectionTestUtils.setField(this.generateService, "numberOfTryInCaseOfError", 0);
        ReflectionTestUtils.setField(this.generateService, "numberOfUuidv4PerCall", 2);

        assertThrows(
                SubmissionCodeServerException.class,
                () -> this.generateService.generateCodeFromRequest(
                        GenerateRequestVo.builder()
                                .type(CodeTypeEnum.UUIDv4.getTypeCode())
                                .build()),
                "Expected doThing() to throw, but it didn't"
        );
    }

}