package fr.gouv.stopc.submission.code.server.it.utils;

import fr.gouv.stopc.submission.code.server.SubmissionCodeServerApplication;
import fr.gouv.stopc.submission.code.server.it.manager.PostgresManager;
import fr.gouv.stopc.submission.code.server.it.manager.SftpManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;

@ActiveProfiles("dev")
@TestPropertySource("classpath:application-dev.properties")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestExecutionListeners(listeners = { SftpManager.class, PostgresManager.class }, mergeMode = MERGE_WITH_DEFAULTS)
@Retention(RUNTIME)
@Target(TYPE)
@ContextConfiguration(classes = SubmissionCodeServerApplication.class)
public @interface IntegrationTest {
}
