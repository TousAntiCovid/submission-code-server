package fr.gouv.stopc.submissioncode.test

import org.flywaydb.core.Flyway
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener
import org.testcontainers.containers.PostgreSQLContainer
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES

class PostgresqlManager : TestExecutionListener {

    companion object {

        private var JDBC_TEMPLATE: JdbcTemplate = JdbcTemplate()

        init {
            PostgreSQLContainer<Nothing>("postgres:9.6").apply {
                start()
                System.setProperty("spring.datasource.url", jdbcUrl)
                System.setProperty("spring.datasource.username", username)
                System.setProperty("spring.datasource.password", password)
            }
        }

        fun givenTableSubmissionCodeContainsCode(
            type: String,
            code: String,
            generatedOn: Instant = Instant.now(),
            availableFrom: Instant = Instant.now(),
            expiresOn: Instant = Instant.now().plus(5, MINUTES)
        ) {
            JDBC_TEMPLATE.execute(
                """
                    insert into submission_code(type_code, code, date_generation, date_available, date_end_validity, used) values
                    ('$type', '$code', '$generatedOn', '$availableFrom', '$expiresOn', false)
                """.trimIndent()
            )
        }
    }

    override fun beforeTestMethod(testContext: TestContext) {
        JDBC_TEMPLATE = testContext.applicationContext.getBean(JdbcTemplate::class.java)
        JDBC_TEMPLATE.execute(
            """
                    drop schema public cascade;
                    create schema public;
            """.trimIndent()
        )
        testContext.applicationContext
            .getBean(Flyway::class.java)
            .migrate()
    }
}
