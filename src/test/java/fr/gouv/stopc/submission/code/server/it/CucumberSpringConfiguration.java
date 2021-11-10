package fr.gouv.stopc.submission.code.server.it;

import fr.gouv.stopc.submission.code.server.data.repository.SubmissionCodeRepository;
import fr.gouv.stopc.submission.code.server.it.utils.IntegrationTest;
import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;
import lombok.RequiredArgsConstructor;

@CucumberContextConfiguration
@IntegrationTest
@RequiredArgsConstructor
public class CucumberSpringConfiguration {

    private final SubmissionCodeRepository submissionCodeRepository;

    @Before
    public void initialization() {
        submissionCodeRepository.deleteAll();
    }
}
