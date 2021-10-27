package fr.gouv.stopc.submission.code.server.verifyservice;

import fr.gouv.stopc.submission.code.server.business.controller.exception.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.business.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.business.service.SubmissionCodeService;
import fr.gouv.stopc.submission.code.server.business.service.VerifyService;
import fr.gouv.stopc.submission.code.server.domain.enums.CodeTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class VerifyServiceVerifyCodeMethodTest {

    private static final String FALSE_CODE = "FALSE_CODE";

    private static final String GOOD_CODE = "AAAAAA";

    @Mock
    SubmissionCodeService submissionCodeService;

    @Spy
    @InjectMocks
    private VerifyService verifyService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "!!!!!!!-!!!!-!!!!-!!!!-!!!!!!!!!!!!!",
            "!!!!!!",
            "!!!!!!!!!!!!"
    })
    void testCodeHasCorrectSize(String code) throws SubmissionCodeServerException {

        Optional<CodeTypeEnum> typeToFound = CodeTypeEnum.searchMatchLength(code.length());

        assertTrue(typeToFound.isPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "!!!!!!!-!!!!-!!!!-!!!!-!!!!!!!!!!!!",
            "!!!!!!!-!!!!-!!!!-!!!!-!!!!!!!!!!!!!!",
            "!!!!!",
            "!!!!!!!",
            "!!!!!!!!!!!",
            "!!!!!!!!!!!!!"
    })
    void testCodeHasIncorrectSize(String code) throws SubmissionCodeServerException {

        Optional<CodeTypeEnum> typeToFound = CodeTypeEnum.searchMatchLength(code.length());

        assertFalse(typeToFound.isPresent());
    }

    @ParameterizedTest
    @CsvSource({
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa , 1",
            "aaaaaa , 2",
            "aaaaaaaaaaaa, 3" })
    void testCodeHasGoodPattern(String code, String type) throws SubmissionCodeServerException {

        when(this.submissionCodeService.getCodeValidity(code))
                .thenReturn(
                        Optional.of(
                                SubmissionCodeDto.builder()
                                        .code(code)
                                        .type(type)
                                        .used(false)
                                        .dateAvailable(OffsetDateTime.now().minusDays(11))
                                        .dateEndValidity(OffsetDateTime.now().plusDays(12))
                                        .build()
                        )
                );

        when(this.submissionCodeService.updateCodeUsed(any()))
                .thenReturn(true);

        final boolean isPresent = this.verifyService.verifyCode(code);
        assertTrue(isPresent);
    }

    @ParameterizedTest
    @CsvSource({
            "!!!!!!!!-!!!!-!!!!-!!!!-!!!!!!!!!!!! , 1",
            "!!!!!! , 2",
            "!!!!!!!!!!!!, 3" })
    void testCodeHasBadPattern(String code, String type) throws SubmissionCodeServerException {

        when(this.submissionCodeService.getCodeValidity(code))
                .thenReturn(
                        Optional.of(
                                SubmissionCodeDto.builder()
                                        .code(code)
                                        .type(type)
                                        .used(false)
                                        .dateAvailable(OffsetDateTime.now().minusDays(11))
                                        .dateEndValidity(OffsetDateTime.now().plusDays(12))
                                        .build()
                        )
                );

        when(this.submissionCodeService.updateCodeUsed(any()))
                .thenReturn(true);

        final boolean isPresent = this.verifyService.verifyCode(code);
        assertFalse(isPresent);
    }

    /**
     * Code does not exists
     */
    @Test
    void testCodeNotExist() throws SubmissionCodeServerException {

        when(this.submissionCodeService.getCodeValidity(FALSE_CODE))
                .thenReturn(Optional.empty());

        final boolean isPresent = this.verifyService.verifyCode(FALSE_CODE);
        assertFalse(isPresent);

    }

    /**
     * Code exists
     */
    @Test
    void testCodeExist() throws SubmissionCodeServerException {

        when(this.submissionCodeService.getCodeValidity(GOOD_CODE))
                .thenReturn(
                        Optional.of(
                                SubmissionCodeDto.builder()
                                        .code(GOOD_CODE)
                                        .type(CodeTypeEnum.SHORT.getTypeCode())
                                        .used(false)
                                        .dateAvailable(OffsetDateTime.now().minusDays(11))
                                        .dateEndValidity(OffsetDateTime.now().plusDays(12))
                                        .build()
                        )
                );

        when(this.submissionCodeService.updateCodeUsed(any()))
                .thenReturn(true);

        final boolean isPresent = this.verifyService.verifyCode(GOOD_CODE);
        assertTrue(isPresent);
    }

    /**
     * Code was already verified
     */
    @Test
    void testCodeAlreadyVerify() throws SubmissionCodeServerException {

        when(this.submissionCodeService.getCodeValidity(GOOD_CODE))
                .thenReturn(
                        Optional.of(
                                SubmissionCodeDto.builder()
                                        .code(GOOD_CODE)
                                        .type(CodeTypeEnum.SHORT.getTypeCode())
                                        .used(true)
                                        .dateAvailable(OffsetDateTime.now().minusDays(11))
                                        .dateEndValidity(OffsetDateTime.now().plusDays(12))
                                        .build()
                        )
                );

        final boolean isPresent2 = this.verifyService.verifyCode(GOOD_CODE);
        assertFalse(isPresent2);
    }

    /**
     * Code has expired
     */
    @Test
    void testExpiredCode() throws SubmissionCodeServerException {

        when(this.submissionCodeService.getCodeValidity(GOOD_CODE))
                .thenReturn(
                        Optional.of(
                                SubmissionCodeDto.builder()
                                        .code(GOOD_CODE)
                                        .type(CodeTypeEnum.SHORT.getTypeCode())
                                        .used(false)
                                        .dateAvailable(OffsetDateTime.now().minusDays(11))
                                        .dateEndValidity(OffsetDateTime.now().minusDays(10))
                                        .build()
                        )
                );

        final boolean isPresent = this.verifyService.verifyCode(GOOD_CODE);
        assertFalse(isPresent);
    }
}
