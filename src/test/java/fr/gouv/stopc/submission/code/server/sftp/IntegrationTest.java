package fr.gouv.stopc.submission.code.server.sftp;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;

@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = RANDOM_PORT)
// @SpringBootTest(classes = SubmissionCodeServerApplication.class,
// webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestExecutionListeners(listeners = { SftpManager.class }, mergeMode = MERGE_WITH_DEFAULTS)
@Retention(RUNTIME)
@Target(TYPE)
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public @interface IntegrationTest {
}
