package fr.gouv.stopc.submission.code.server.ws.service.generateservice;

import fr.gouv.stopc.submission.code.server.commun.service.impl.AlphaNumericCodeServiceImpl;
import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.entity.Lot;
import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;
import fr.gouv.stopc.submission.code.server.database.service.impl.SubmissionCodeServiceImpl;
import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.ws.dto.CodeDetailedDto;
import fr.gouv.stopc.submission.code.server.ws.service.impl.GenerateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class GenerateServiceGenerateAlphaNumericCodeMethodTest {

    @Mock
    SubmissionCodeServiceImpl submissionCodeService;

    @Mock
    AlphaNumericCodeServiceImpl alphaNumericCodeService;

    @Spy
    @InjectMocks
    GenerateServiceImpl generateService;


    @BeforeEach
    public void init(){
        MockitoAnnotations.initMocks(this);

    }

    /**
     * Calling generateAlphaNumericCode verify that is return one element in its list.
     */
    @Test
    void testOneCodeReturned6ALPHANUM() throws SubmissionCodeServerException {

        Mockito.when(this.submissionCodeService.saveCode(
                Mockito.any(SubmissionCodeDto.class), Mockito.any(Lot.class)
        )).thenReturn(Optional.of(new SubmissionCode()));

        Mockito.when(
                this.alphaNumericCodeService.generateCode()
        ).thenReturn("B150US");

        final CodeDetailedDto codeDetailedResponseDtoList = this.generateService.generateAlphaNumericDetailedCode();
        //list should not be null
        assertNotNull(codeDetailedResponseDtoList);



    }

}