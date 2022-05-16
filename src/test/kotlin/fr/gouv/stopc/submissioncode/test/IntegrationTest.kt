package fr.gouv.stopc.submissioncode.test

import fr.gouv.stopc.submissioncode.SubmissionCodeServerApplication
import org.junit.jupiter.api.DisplayNameGeneration
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.CLASS

@ActiveProfiles("dev", "test")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = [SubmissionCodeServerApplication::class])
@TestExecutionListeners(
    listeners = [RestAssuredManager::class, PostgresqlManager::class, JWTManager::class],
    mergeMode = MERGE_WITH_DEFAULTS
)
@DisplayNameGeneration(ReplaceUnderscores::class)
@Retention(RUNTIME)
@Target(ANNOTATION_CLASS, CLASS)
annotation class IntegrationTest()
