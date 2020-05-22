package fr.gouv.stopc.submission.code.server.ws.service.generateservice;

import fr.gouv.stopc.submission.code.server.commun.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.commun.service.impl.UUIDv4CodeServiceImpl;
import fr.gouv.stopc.submission.code.server.database.entity.Lot;
import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;
import fr.gouv.stopc.submission.code.server.database.service.impl.SubmissionCodeServiceImpl;
import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.ws.service.impl.GenerateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class GenerateServiceGenerateUUIDv4CodesMethodTest {
    @Mock
    private UUIDv4CodeServiceImpl uuiDv4CodeService;

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
    }
    /**
     * Calling generateUUIDv4Codes and assert that it returns the right size and the right elements
     */
    @Test
    void testSizeGenerateResponseDtoListUUID() throws SubmissionCodeServerException {
        int size = 12;

        Mockito.when(this.submissionCodeService.saveCode(Mockito.any(), Mockito.any()))
                .thenReturn(Optional.of(new SubmissionCode()));

        this.generateService.generateCodeGeneric(size, CodeTypeEnum.UUIDv4, OffsetDateTime.now(), new Lot());

        verify(uuiDv4CodeService, times(12)).generateCode();

    }

}