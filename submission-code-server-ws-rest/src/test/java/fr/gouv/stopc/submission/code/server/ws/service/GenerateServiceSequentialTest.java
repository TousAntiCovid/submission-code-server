package fr.gouv.stopc.submission.code.server.ws.service;

import fr.gouv.stopc.submission.code.server.commun.service.impl.UUIDv4CodeServiceImpl;
import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.ws.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.ws.errors.NumberOfTryGenerateCodeExceededExcetion;
import fr.gouv.stopc.submission.code.server.ws.vo.GenerateRequestVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
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
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
class GenerateServiceSequentialTest {

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
     * List is returning desired number of codes
     */
    @Test
    void generateCodeGenericTest0001()
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        // asserting gsi is available
        final long size = Long.parseLong("10");
        final CodeTypeEnum cte = CodeTypeEnum.UUIDv4;
        final OffsetDateTime validFrom = OffsetDateTime.now();
        final long lot = Long.parseLong("123456");

        final List<GenerateResponseDto> generateResponseDtoList = this.gsi.generateCodeGeneric(
                size, cte, validFrom, lot
        );
        //list should not be null
        assertNotNull(generateResponseDtoList);
        // list should be at size
        assertEquals(size, generateResponseDtoList.size());
    }

    /**
     * Check elements in list have each a code.
     */
    @Test
    void generateCodeGenericTest0002()
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        // asserting gsi is available
        final long size = Long.parseLong("1");
        final CodeTypeEnum cte = CodeTypeEnum.UUIDv4;
        final OffsetDateTime validFrom = OffsetDateTime.now();
        final long lot = Long.parseLong("123456");

        final List<GenerateResponseDto> generateResponseDtoList = this.gsi.generateCodeGeneric(
                size, cte, validFrom, lot
        );

        //list should not be null
        assertNotNull(generateResponseDtoList);
        // list should be at size
        assertEquals(size, generateResponseDtoList.size());

        generateResponseDtoList.forEach(grDto -> {
            // asserting that generated is not blank
            final String code = grDto.getCode();
            assertTrue(Strings.isNotBlank(code));
        });

    }

    /**
     * Check elements in list have each a code.
     */
    @Test
    void generateCodeGenericTest0002_uuidv4()
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        // asserting gsi is available
        final long size = Long.parseLong("1");
        final CodeTypeEnum cte = CodeTypeEnum.UUIDv4;
        final OffsetDateTime validFrom = OffsetDateTime.now();
        final long lot = Long.parseLong("123456");

        final List<GenerateResponseDto> generateResponseDtoList = this.gsi.generateCodeGeneric(
                size, cte, validFrom, lot
        );

        //list should not be null
        assertNotNull(generateResponseDtoList);
        // list should be at size
        assertEquals(size, generateResponseDtoList.size());

        Pattern p = Pattern.compile("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})");

        generateResponseDtoList.forEach(grDto -> {
            // asserting that code is formatted as UUIDv4 standard
            final String code = grDto.getCode();
            Matcher m = p.matcher(code);
            assertTrue(m.matches());

        });

    }

    /**
     * Check elements in list have each a code.
     */
    @Test
    void generateCodeGenericTest0002_6alphanum()
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        // asserting gsi is available
        final long size = Long.parseLong("1");
        final CodeTypeEnum cte = CodeTypeEnum.ALPHANUM_6;
        final OffsetDateTime validFrom = OffsetDateTime.now();
        final long lot = Long.parseLong("123456");

        final List<GenerateResponseDto> generateResponseDtoList = this.gsi.generateCodeGeneric(
                size, cte, validFrom, lot
        );

        //list should not be null
        assertNotNull(generateResponseDtoList);
        // list should be at size
        assertEquals(size, generateResponseDtoList.size());

        Pattern p = Pattern.compile("([A-Z0-9]{6})");

        generateResponseDtoList.forEach(grDto -> {
            // asserting that code is formatted as UUIDv4 standard
            final String code = grDto.getCode();
            Matcher m = p.matcher(code);
            assertTrue(m.matches());

        });

    }

    /**
     * Check elements in list have the right code type
     */
    @Test
    void generateCodeGenericTest0003()
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        // asserting gsi is available
        final long size = Long.parseLong("1");
        final CodeTypeEnum cte = CodeTypeEnum.UUIDv4;
        final OffsetDateTime validFrom = OffsetDateTime.now();
        final long lot = Long.parseLong("123456");

        final List<GenerateResponseDto> generateResponseDtoList = this.gsi.generateCodeGeneric(
                size, cte, validFrom, lot
        );

        //list should not be null
        assertNotNull(generateResponseDtoList);
        // list should be at size
        assertEquals(size, generateResponseDtoList.size());

        generateResponseDtoList.forEach(grDto -> {
            // assert the returning code corresponding to the given CodeTypeEnum in parameter
            assertEquals(CodeTypeEnum.UUIDv4.getType(), grDto.getTypeAsString());
            assertEquals(Integer.parseInt(
                    CodeTypeEnum.UUIDv4.getTypeCode())
                    , grDto.getTypeAsInt()
            );
        });
    }

    /**
     * Check elements in list have the right validUntil and validFrom format
     */
    @Test
    void generateCodeGenericTest0004()
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        // asserting gsi is available
        final long size = Long.parseLong("1");
        final CodeTypeEnum cte = CodeTypeEnum.UUIDv4;
        final OffsetDateTime validFrom = OffsetDateTime.now();
        final long lot = Long.parseLong("123456");

        final List<GenerateResponseDto> generateResponseDtoList = this.gsi.generateCodeGeneric(
                size, cte, validFrom, lot
        );

        //list should not be null
        assertNotNull(generateResponseDtoList);
        // list should be at size
        assertEquals(size, generateResponseDtoList.size());

        Pattern p = Pattern.compile("^(-?(?:[1-9][0-9]*)?[0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])" +
                "T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\.[0-9]+)?" +
                "(Z|[+-](?:2[0-3]|[01][0-9]):[0-5][0-9])?$");

        generateResponseDtoList.forEach(grDto -> {

            // asserting that date is at the right format
            final String validFrom1 = grDto.getValidFrom();
            final String validUntil1 = grDto.getValidUntil();

            Matcher m = p.matcher(validFrom1);
            assertTrue(m.matches());

            m = p.matcher(validUntil1);
            assertTrue(m.matches());
        });
    }



    /**
     * Persisted codes are uniques
     */
    @Test
    void generateCodeGenericTest0005()
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        // asserting gsi is available
        final long size = Long.parseLong("10");
        final CodeTypeEnum cte = CodeTypeEnum.UUIDv4;
        final OffsetDateTime validFrom = OffsetDateTime.now();
        final long lot = Long.parseLong("1");

        final List<GenerateResponseDto> generateResponseDtoList = this.gsi.generateCodeGeneric(
                size, cte, validFrom, lot
        );

        //list should not be null
        assertNotNull(generateResponseDtoList);
        // list should be at size
        assertEquals(size, generateResponseDtoList.size());

        final HashMap<String, Integer> map = new HashMap<>();
        for (int i = 0; i < generateResponseDtoList.size(); i++) {
            GenerateResponseDto gr = generateResponseDtoList.get(i);
            map.put(gr.getCode(), 0);
        }

        assertEquals(size, map.size());
    }

    /**
     * Number of tries reach
     */
    @Test
    void generateCodeGenericTest0006()
    {
        // asserting gsi is available
        final long size = Long.parseLong("10");
        final CodeTypeEnum cte = CodeTypeEnum.UUIDv4;
        final OffsetDateTime validFrom = OffsetDateTime.now();
        final long lot = Long.parseLong("123456");

        Mockito.when(submissionCodeServiceMock.saveCode(Mockito.any(SubmissionCodeDto.class)))
                .thenThrow(DataIntegrityViolationException.class);

        Mockito.when(uuiDv4CodeService.generateCode())
                .thenReturn("1234-123-123-123-123-1234");

        ReflectionTestUtils.setField(gsiMocked, "NUMBER_OF_TRY_IN_CASE_OF_ERROR", 0);


        NumberOfTryGenerateCodeExceededExcetion notgcee = null;
        try {
            this.gsiMocked.generateCodeGeneric(
                    size, cte, validFrom, lot
            );
        } catch (  NumberOfTryGenerateCodeExceededExcetion e ) {
            notgcee = e;
            assertEquals(String.format("Number of tries exceeded. %s were authorized.", 0), e.getMessage());
        }

        assertNotNull(notgcee);

    }

    /**
     * Number of tries reach
     */
    @Test
    void generateCodeGenericTest0007()
    {
        // asserting gsi is available
        final long size = Long.parseLong("10");
        final CodeTypeEnum cte = CodeTypeEnum.UUIDv4;
        final OffsetDateTime validFrom = OffsetDateTime.now();
        final long lot = Long.parseLong("123456");

        Mockito.when(submissionCodeServiceMock.saveCode(Mockito.any(SubmissionCodeDto.class)))
                .thenThrow(DataIntegrityViolationException.class);

        Mockito.when(uuiDv4CodeService.generateCode())
                .thenReturn("1234-123-123-123-123-1234");

        ReflectionTestUtils.setField(gsiMocked, "NUMBER_OF_TRY_IN_CASE_OF_ERROR", 0);


        NumberOfTryGenerateCodeExceededExcetion notgcee = null;
        try {
            this.gsiMocked.generateCodeGeneric(
                    size, cte, validFrom
            );
        } catch (  NumberOfTryGenerateCodeExceededExcetion e ) {
            notgcee = e;
            assertEquals(String.format("Number of tries exceeded. %s were authorized.", 0), e.getMessage());
        }

        assertNotNull(notgcee);
    }

    /**
     * Persisted codes are uniques
     */
    @Test
    void generateCodeGenericTest0008()
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        // asserting gsi is available
        final long size = Long.parseLong("10");
        final CodeTypeEnum cte = CodeTypeEnum.UUIDv4;
        final OffsetDateTime validFrom = OffsetDateTime.now();
        final long lot = Long.parseLong("1");

        final List<GenerateResponseDto> generateResponseDtoList = this.gsi.generateCodeGeneric(
                size, cte, validFrom
        );

        //list should not be null
        assertNotNull(generateResponseDtoList);
        // list should be at size
        assertEquals(size, generateResponseDtoList.size());

        final HashMap<String, Integer> map = new HashMap<>();
        for (int i = 0; i < generateResponseDtoList.size(); i++) {
            GenerateResponseDto gr = generateResponseDtoList.get(i);
            map.put(gr.getCode(), 0);
        }

        assertEquals(size, map.size());
    }

    /**
     * Calling generateAlphaNumericCode verify that is return one element in its list.
     */
    @Test
    void generateAlphaNumericCodeTest0001()
    {

        NumberOfTryGenerateCodeExceededExcetion notgcee = null;
        try {
            final List<GenerateResponseDto> generateResponseDtoList = this.gsi.generateAlphaNumericCode();

            //list should not be null
            assertNotNull(generateResponseDtoList);
            // list should be at size
            assertEquals(1, generateResponseDtoList.size());



        } catch (  NumberOfTryGenerateCodeExceededExcetion e ) {
            notgcee = e;
            assertEquals(String.format("Number of tries exceeded. %s were authorized.", 0), e.getMessage());
        }
        assertNull(notgcee);
    }


    /**
     * Calling generateUUIDv4Codes and assert that it returns the right size and the right elements
     */
    @Test
    void generateUUIDv4CodesTest0001()
    {
        long size = Long.parseLong("12");

        NumberOfTryGenerateCodeExceededExcetion notgcee = null;
        try {
            final List<GenerateResponseDto> generateResponseDtoList = this.gsi.generateUUIDv4Codes(size);

            //list should not be null
            assertNotNull(generateResponseDtoList);
            // list should be at size
            assertEquals(size, generateResponseDtoList.size());

            // asserting it is UUID type code
            CodeTypeEnum.UUIDv4.getTypeCode().equals(generateResponseDtoList.get(0).getTypeAsInt().toString());
            CodeTypeEnum.UUIDv4.getType().equals(generateResponseDtoList.get(0).getTypeAsString());

            // asserting code matches UUID pattern
            Pattern p = Pattern.compile("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})");
            assertTrue(p.matcher(generateResponseDtoList.get(0).getCode()).matches());


        } catch (  NumberOfTryGenerateCodeExceededExcetion e ) {
            notgcee = e;
            assertEquals(String.format("Number of tries exceeded. %s were authorized.", 0), e.getMessage());
        }
        assertNull(notgcee);

    }

    /**
     * Calling generateCodeFromRequest with generateRequestVo == null
     */
    @Test
    void generateCodeFromRequestTest0001_1()
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
    void generateCodeFromRequestTest0001_2()
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
    void generateCodeFromRequestTest0001_3()
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
    void generateCodeFromRequestTest0001_4()
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
    void generateCodeFromRequestTest0002()
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        UnsupportedDataTypeException udte = null;

        try {
            final List<GenerateResponseDto> generateResponseDtoList = this.gsi.generateCodeFromRequest(
                    GenerateRequestVo.builder()
                            .type(CodeTypeEnum.ALPHANUM_6.getTypeCode())
                            .build()
            );

            this.assertingALPHANUM6Code(generateResponseDtoList.get(0));

        } catch (UnsupportedDataTypeException e ) {
            udte = e;
        }

        assertNull(udte);
    }

    /**
     * Calling generateCodeFromRequest with generateRequestVo.type == CodeTypeEnum.UUIDv4.getTypeCode()
     */
    @Test
    void generateCodeFromRequestTest0003()
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        UnsupportedDataTypeException udte = null;

        try {
            final List<GenerateResponseDto> generateResponseDtoList = this.gsi.generateCodeFromRequest(
                    GenerateRequestVo.builder()
                            .type(CodeTypeEnum.UUIDv4.getTypeCode())
                            .build()
            );

            this.assertingUUIDv4Code(generateResponseDtoList.get(0));

        } catch (UnsupportedDataTypeException e ) {
            udte = e;
        }

        assertNull(udte);
    }


    /**
     * generateCodeFromRequest with Number of tries reach UUIDv4
     */
    @Test
    void generateCodeFromRequestTest0004()
            throws UnsupportedDataTypeException
    {
        Mockito.when(uuiDv4CodeService.generateCode())
                .thenReturn("1234-123-123-123-123-1234");

        Mockito.when(submissionCodeServiceMock.saveCode(Mockito.any(SubmissionCodeDto.class)))
                .thenThrow(DataIntegrityViolationException.class);

        ReflectionTestUtils.setField(gsiMocked, "NUMBER_OF_TRY_IN_CASE_OF_ERROR", 0);
        ReflectionTestUtils.setField(gsiMocked, "NUMBER_OF_UUIDv4_PER_CALL", 2);

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
    void generateCodeFromRequestTest0005()
            throws UnsupportedDataTypeException
    {
        Mockito.when(submissionCodeServiceMock.saveCode(Mockito.any(SubmissionCodeDto.class)))
                .thenThrow(DataIntegrityViolationException.class);

        Mockito.when(uuiDv4CodeService.generateCode())
                .thenReturn("1234-123-123-123-123-1234");

        ReflectionTestUtils.setField(gsiMocked, "NUMBER_OF_TRY_IN_CASE_OF_ERROR", 0);
        ReflectionTestUtils.setField(gsiMocked, "NUMBER_OF_UUIDv4_PER_CALL", 2);

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


    /**
     * asserting code ALPHANUM6
     * {@link #assertingCode(GenerateResponseDto, CodeTypeEnum, String)}
     */
    private void assertingALPHANUM6Code(GenerateResponseDto gr)
    {
        this.assertingCode(gr, CodeTypeEnum.ALPHANUM_6,"([A-Z0-9]{6})");
    }

    /**
     * asserting code UUID
     * {@link #assertingCode(GenerateResponseDto, CodeTypeEnum, String)}
     */
    private void assertingUUIDv4Code(GenerateResponseDto gr)
    {
        this.assertingCode(gr, CodeTypeEnum.UUIDv4,"([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})" );
    }

    /**
     * asserting code
     */
    private void assertingCode(GenerateResponseDto gr, CodeTypeEnum cte, String pattern)
    {
        assertNotNull(gr);

        // asserting it is CodeTypeEnum code
        cte.getTypeCode().equals(gr.getTypeAsInt().toString());
        cte.getType().equals(gr.getTypeAsString());

        // asserting code matches CodeTypeEnum pattern
        Pattern p = Pattern.compile(pattern);
        assertTrue(p.matcher(gr.getCode()).matches());
    }




}