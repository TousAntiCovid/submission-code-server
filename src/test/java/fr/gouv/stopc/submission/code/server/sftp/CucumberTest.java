package fr.gouv.stopc.submission.code.server.sftp;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(glue = "fr.gouv.stopc.submission.code.server", features = { "classpath:features" }, plugin = {
        "pretty", "html:cucumber-reports.html" })
public class CucumberTest {
}
