package fr.gouv.stopc.submission.code.server.it.steps;

import fr.gouv.stopc.submission.code.server.business.model.CodeSimpleDto;
import fr.gouv.stopc.submission.code.server.business.service.GenerateService;
import fr.gouv.stopc.submission.code.server.business.service.VerifyService;
import fr.gouv.stopc.submission.code.server.data.repository.SubmissionCodeRepository;
import fr.gouv.stopc.submission.code.server.domain.enums.CodeTypeEnum;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestCodesSteps {

    @Autowired
    GenerateService generateService;

    @Autowired
    VerifyService verifyService;

    @Autowired
    protected SubmissionCodeRepository submissionCodeRepository;

    private CodeSimpleDto codeSimpleDto;

    private Boolean verify;

    @Before
    public void initialization() {
        submissionCodeRepository.deleteAll();
    }

    @When("I request a \"test\" submission code")
    @Given("A \"test\" submission code has been generated")
    public void we_ask_for_a_new_test_code() {
        codeSimpleDto = generateService.generateTestCode();
        Assertions.assertNotNull(codeSimpleDto);
    }

    @Then("I received a 12 characters code valid until now plus 3 days")
    public void the_generated_code_is_a_well_formatted_code() {
        Pattern p = Pattern.compile("([a-zA-Z0-9]{12})");
        final String code = codeSimpleDto.getCode();
        Matcher m = p.matcher(code);
        Assertions.assertTrue(m.matches());

        Instant inThreeDays = Instant.now().truncatedTo(ChronoUnit.DAYS)
                .plus(3, ChronoUnit.DAYS);
        Instant validUntil = Instant.parse(codeSimpleDto.getValidUntil())
                .truncatedTo(ChronoUnit.DAYS);
        validUntil.equals(inThreeDays);
    }

    @When("I request it's verification")
    @And("It's verification has already been requested")
    public void i_request_it_s_verification() {
        verify = verifyService.verifyCode(codeSimpleDto.getCode(), CodeTypeEnum.TEST.getTypeCode());
    }

    @Then("The verification response is successful")
    public void the_verification_response_is_successful() {
        Assertions.assertTrue(verify);
    }

    @Then("The verification response is failed")
    public void the_verification_response_is_failed() {
        Assertions.assertFalse(verify);
    }

    @Given("I generate an INVALID \"test\" submission code")
    public void i_request_the_verification_of_an_invalid_test_code() {
        codeSimpleDto = CodeSimpleDto.builder().code("a_false_code").build();
    }

    @After
    public void clean_vars_between_scenarios() {
        codeSimpleDto = null;
        verify = null;
    }

}
