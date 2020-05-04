package fr.gouv.stopc.submission.code.server.ws.service.generateservice;

import fr.gouv.stopc.submission.code.server.commun.service.impl.UUIDv4CodeServiceImpl;
import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.ws.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.ws.errors.NumberOfTryGenerateCodeExceededExcetion;
import fr.gouv.stopc.submission.code.server.ws.service.GenerateServiceImpl;
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

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
class GenerateServiceGenerateCodeGenericMethodTest {

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
    void sizeOfGenerateResponseDtoListTest()
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
    void codeNotBlankTest()
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
    void codeWithUUIDv4PatternTest()
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
    void codeWith6ALPHANUMPatternTest()
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
    void codeTypeTest()
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
    void validUntilAndValidFromFormatTest()
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
    void uniqueCodeTest()
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
    void reachNumberOfTriesTest()
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
    void reachNumberOfTriesWithoutLotParameterTest()
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
    void uniqueCodeWithoutLotParameterTest()
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

}