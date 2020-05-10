package fr.gouv.stopc.submission.code.server.ws.service.verifyservice;


import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.commun.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.ws.errors.NumberOfTryGenerateCodeExceededExcetion;
import fr.gouv.stopc.submission.code.server.ws.service.GenerateServiceImpl;
import fr.gouv.stopc.submission.code.server.ws.service.VerifyServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Slf4j
@SpringBootTest
public class VerifyServiceVerifyCodeMethodTest {

    @Autowired
    private GenerateServiceImpl gsi;

    @Autowired
    private VerifyServiceImpl vsi;

    /**
     * Code does not exists
     */
    @Test
    void codeNotExistTest() {
        final String falseCode = "FALSE_CODE";
        final boolean isPresent = this.vsi.verifyCode(falseCode, CodeTypeEnum.ALPHANUM_6.getType());
        assertFalse(isPresent);

    }

    /**
     * Code exists for given CodeType
     */
    @Test
    void codeExistForGivenCodeTypeTest() throws NumberOfTryGenerateCodeExceededExcetion {

        final List<GenerateResponseDto> grdList = this.gsi.generateAlphaNumericCode();
        final GenerateResponseDto grd = grdList.get(0);
        final String falseCode = grd.getCode();

        final boolean isPresent = this.vsi.verifyCode(falseCode, CodeTypeEnum.ALPHANUM_6.getTypeCode());
        assertTrue(isPresent);
    }

    /**
     * Code exists but not for given CodeType
     */
    @Test
    void codeNotExistForGivenCodeTypeTest() throws NumberOfTryGenerateCodeExceededExcetion {

        final List<GenerateResponseDto> grdList = this.gsi.generateAlphaNumericCode();
        final GenerateResponseDto grd = grdList.get(0);
        final String falseCode = grd.getCode();

        final boolean isPresent = this.vsi.verifyCode(falseCode, CodeTypeEnum.UUIDv4.getTypeCode());
        assertFalse(isPresent);
    }

    /**
     * Code was already verified
     */
    @Test
    void codeAlreadyVerifyTest() throws NumberOfTryGenerateCodeExceededExcetion {

        final List<GenerateResponseDto> grdList = this.gsi.generateAlphaNumericCode();
        final GenerateResponseDto grd = grdList.get(0);
        final String falseCode = grd.getCode();

        final boolean isPresent1 = this.vsi.verifyCode(falseCode, CodeTypeEnum.ALPHANUM_6.getTypeCode());
        assertTrue(isPresent1);

        final boolean isPresent2 = this.vsi.verifyCode(falseCode, CodeTypeEnum.ALPHANUM_6.getTypeCode());
        assertFalse(isPresent2);
    }

    /**
     * Code has expired
     */
    @Test
    void expiredCodeTest() throws NumberOfTryGenerateCodeExceededExcetion {

        // Generate Code in past
        final OffsetDateTime offsetDateTime = OffsetDateTime.now().withYear(0);

        // set validity time of ALPHANUM TO 1 minute
        ReflectionTestUtils.setField(this.gsi, "TARGET_ZONE_ID", "Europe/Paris");
        ReflectionTestUtils.setField(this.gsi, "TIME_VALIDITY_ALPHANUM", 1);

        final List<GenerateResponseDto> grdList = this.gsi.generateCodeGeneric(
                1,
                CodeTypeEnum.ALPHANUM_6,
                offsetDateTime
        );

        final GenerateResponseDto grd = grdList.get(0);
        final String falseCode = grd.getCode();

        final boolean isPresent = this.vsi.verifyCode(falseCode, CodeTypeEnum.ALPHANUM_6.getTypeCode());
        assertFalse(isPresent);
    }
}
