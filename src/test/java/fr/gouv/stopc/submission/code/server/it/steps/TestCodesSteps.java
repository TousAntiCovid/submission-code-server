package fr.gouv.stopc.submission.code.server.it.steps;

import fr.gouv.stopc.submission.code.server.business.model.CodeSimpleDto;
import fr.gouv.stopc.submission.code.server.business.service.GenerateService;
import fr.gouv.stopc.submission.code.server.business.service.VerifyService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class TestCodesSteps {

    private final GenerateService generateService;

    private final VerifyService verifyService;

    private CodeSimpleDto codeSimpleDto;

    private Boolean verify;

    @When("I request a \"test\" submission code")
    @Given("A \"test\" submission code has been generated")
    public void we_ask_for_a_new_test_code() {
        codeSimpleDto = generateService.generateTestCode();
        Assertions.assertNotNull(codeSimpleDto);
    }

    @Then("I received a 12 characters code valid until now plus 3 days")
    public void the_generated_code_is_a_well_formatted_code() {
        assertThat(codeSimpleDto.getCode())
                .matches("([a-zA-Z0-9]{12})");

        Instant inThreeDays = Instant.now().truncatedTo(ChronoUnit.DAYS)
                .plus(3, ChronoUnit.DAYS);
        Instant validUntil = Instant.parse(codeSimpleDto.getValidUntil());
        assertThat(inThreeDays).isEqualTo(validUntil);
    }

    @When("I request it's verification")
    @And("It's verification has already been requested")
    public void i_request_it_s_verification() {
        verify = verifyService.verifyCode(codeSimpleDto.getCode());
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

}
