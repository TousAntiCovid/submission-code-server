package fr.gouv.stopc.submission.code.server.it.manager;

import org.springframework.test.context.TestExecutionListener;
import org.testcontainers.containers.GenericContainer;

public class PostgresManager implements TestExecutionListener {

    private static final int PORT = 5432;

    private static final GenericContainer POSTGRE_SQL_TEST_CONTAINER = new GenericContainer("postgres:13")
            .withEnv("POSTGRES_USER", "postgres")
            .withEnv("POSTGRES_PASSWORD", "1234")
            .withEnv("POSTGRES_DB", "dev-submission-code-server-schema")
            .withExposedPorts(PORT);

    static {
        POSTGRE_SQL_TEST_CONTAINER.start();
        String jdbcUrl = "jdbc:postgresql://" + POSTGRE_SQL_TEST_CONTAINER.getHost()
                + ":"
                + POSTGRE_SQL_TEST_CONTAINER.getMappedPort(PORT).toString()
                + "/dev-submission-code-server-schema";
        System.setProperty("SUBMISSION_CODE_SERVER_DB_URL", jdbcUrl);
    }
}
