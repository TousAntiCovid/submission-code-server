package fr.gouv.stopc.submission.code.server.ws.service.generateservice;

import fr.gouv.stopc.submission.code.server.commun.service.impl.UUIDv4CodeServiceImpl;
import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.ws.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.ws.errors.NumberOfTryGenerateCodeExceededExcetion;
import fr.gouv.stopc.submission.code.server.ws.service.GenerateServiceImpl;
import fr.gouv.stopc.submission.code.server.ws.vo.GenerateRequestVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import javax.activation.UnsupportedDataTypeException;
import java.util.List;

import static fr.gouv.stopc.submission.code.server.ws.service.generateservice.GenerateServiceTestHelper.assertingALPHANUM6Code;
import static fr.gouv.stopc.submission.code.server.ws.service.generateservice.GenerateServiceTestHelper.assertingUUIDv4Code;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
class GenerateServiceGenerateCodeFromRequestMethodTest {

    @Mock
    private ISubmissionCodeService submissionCodeServiceMock;

    @Mock
    private UUIDv4CodeServiceImpl uuiDv4CodeService;

    @Spy
    @InjectMocks
    private GenerateServiceImpl gsiMocked;

    @Autowired
    private GenerateServiceImpl gsi;


    @Before
    public void init(){
        log.info("Initialize mokito injection in services...");
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Calling generateCodeFromRequest with generateRequestVo == null
     */
    @Test
    void withNullTest()
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        UnsupportedDataTypeException udte = null;

        try {
            this.gsi.generateCodeFromRequest(null);
        } catch (UnsupportedDataTypeException e ) {
            udte = e;
        }

        assertNotNull(udte);
    }

    /**
     * Calling generateCodeFromRequest with generateRequestVo.type == null
     */
    @Test
    void withTypeNullTest()
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        UnsupportedDataTypeException udte = null;

        try {
            this.gsi.generateCodeFromRequest(GenerateRequestVo.builder().type(null).build());
        } catch (UnsupportedDataTypeException e ) {
            udte = e;
        }

        assertNotNull(udte);
    }


    /**
     * Calling generateCodeFromRequest with generateRequestVo.type == " "
     */
    @Test
    void withTypeBlankTest()
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        UnsupportedDataTypeException udte = null;

        try {
            this.gsi.generateCodeFromRequest(GenerateRequestVo.builder().type(" ").build());
        } catch (UnsupportedDataTypeException e ) {
            udte = e;
        }

        assertNotNull(udte);
    }

    /**
     * Calling generateCodeFromRequest with generateRequestVo.type == "NOT_AN_CODE_TYPE_ENUM"
     */
    @Test
    void withTypeNotCodeTypeEnumTest()
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        UnsupportedDataTypeException udte = null;

        try {
            this.gsi.generateCodeFromRequest(GenerateRequestVo.builder().type("NOT_AN_CODE_TYPE_ENUM").build());
        } catch (UnsupportedDataTypeException e ) {
            udte = e;
        }

        assertNotNull(udte);
    }

    /**
     * Calling generateCodeFromRequest with generateRequestVo.type == CodeTypeEnum.ALPHANUM_6.getTypeCode()
     */
    @Test
    void withType6ALPHANUMTest()
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        UnsupportedDataTypeException udte = null;

        try {
            final List<GenerateResponseDto> generateResponseDtoList = this.gsi.generateCodeFromRequest(
                    GenerateRequestVo.builder()
                            .type(CodeTypeEnum.ALPHANUM_6.getTypeCode())
                            .build()
            );

            assertingALPHANUM6Code(generateResponseDtoList.get(0));

        } catch (UnsupportedDataTypeException e ) {
            udte = e;
        }

        assertNull(udte);
    }

    /**
     * Calling generateCodeFromRequest with generateRequestVo.type == CodeTypeEnum.UUIDv4.getTypeCode()
     */
    @Test
    void withTypeUUIDv4Test()
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        UnsupportedDataTypeException udte = null;

        try {
            final List<GenerateResponseDto> generateResponseDtoList = this.gsi.generateCodeFromRequest(
                    GenerateRequestVo.builder()
                            .type(CodeTypeEnum.UUIDv4.getTypeCode())
                            .build()
            );

            assertingUUIDv4Code(generateResponseDtoList.get(0));

        } catch (UnsupportedDataTypeException e ) {
            udte = e;
        }

        assertNull(udte);
    }


    /**
     * generateCodeFromRequest with Number of tries reach UUIDv4
     */
    @Test
    void generateCodeFromRequestReachTriesUUIDv4Test()
            throws UnsupportedDataTypeException
    {
        Mockito.when(uuiDv4CodeService.generateCode())
                .thenReturn("1234-123-123-123-123-1234");

        Mockito.when(submissionCodeServiceMock.saveCode(Mockito.any(SubmissionCodeDto.class)))
                .thenThrow(DataIntegrityViolationException.class);

        ReflectionTestUtils.setField(gsiMocked, "NUMBER_OF_TRY_IN_CASE_OF_ERROR", 0);
        ReflectionTestUtils.setField(gsiMocked, "NUMBER_OF_UUIDv4_PER_CALL", 2);
        ReflectionTestUtils.setField(gsiMocked, "TARGET_ZONE_ID", "Europe/Paris");

        NumberOfTryGenerateCodeExceededExcetion notgcee = null;

        try {
            this.gsiMocked.generateCodeFromRequest(
                    GenerateRequestVo.builder()
                            .type(CodeTypeEnum.UUIDv4.getTypeCode())
                            .build()
            );

        } catch (  NumberOfTryGenerateCodeExceededExcetion e ) {
            notgcee = e;
            assertEquals(String.format("Number of tries exceeded. %s were authorized.", 0), e.getMessage());
        }

        assertNotNull(notgcee);

    }

    /**
     * generateCodeFromRequest with Number of tries reach ALPHANUM
     */
    @Test
    void generateCodeFromRequestReachTries6ALPHANUMTest()
            throws UnsupportedDataTypeException
    {
        Mockito.when(submissionCodeServiceMock.saveCode(Mockito.any(SubmissionCodeDto.class)))
                .thenThrow(DataIntegrityViolationException.class);

        Mockito.when(uuiDv4CodeService.generateCode())
                .thenReturn("1234-123-123-123-123-1234");

        ReflectionTestUtils.setField(gsiMocked, "NUMBER_OF_TRY_IN_CASE_OF_ERROR", 0);
        ReflectionTestUtils.setField(gsiMocked, "NUMBER_OF_UUIDv4_PER_CALL", 2);
        ReflectionTestUtils.setField(this.gsiMocked, "TARGET_ZONE_ID", "Europe/Paris");

        NumberOfTryGenerateCodeExceededExcetion notgcee = null;
        try {
            this.gsiMocked.generateCodeFromRequest(
                    GenerateRequestVo.builder()
                            .type(CodeTypeEnum.UUIDv4.getTypeCode())
                            .build()
            );

        } catch (NumberOfTryGenerateCodeExceededExcetion e ) {
            notgcee = e;
            assertEquals(String.format("Number of tries exceeded. %s were authorized.", 0), e.getMessage());
        }

        assertNotNull(notgcee);

    }

}