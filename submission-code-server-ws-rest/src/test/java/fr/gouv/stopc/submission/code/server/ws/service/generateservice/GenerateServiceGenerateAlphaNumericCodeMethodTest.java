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
@SpringBootTest
class GenerateServiceGenerateAlphaNumericCodeMethodTest {

    @Autowired
    private GenerateServiceImpl gsi;


    @Before
    public void init(){
        log.info("Initialize mokito injection in services...");
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Calling generateAlphaNumericCode verify that is return one element in its list.
     */
    @Test
    void oneCodeReturned6ALPHANUMTest()
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

}