package fr.gouv.stopc.submission.code.server.sftp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

@Slf4j
public class SftpManager implements TestExecutionListener {

    private static final int PORT = 22;

    private static final String USER = "user";

    private static final String PASSWORD = "password";

    private static final String FILE_NAME = "test.txt";

    private static final String REMOTE_PATH = "/upload/";

    private static final GenericContainer SFTP = new GenericContainer(
            new ImageFromDockerfile()
                    .withDockerfileFromBuilder(
                            builder -> builder
                                    .from("atmoz/sftp:latest")
                                    .run("mkdir -p /home/" + USER + "/upload; chmod -R 777 /home/" + USER)
                                    .build()
                    )
    )
            .withExposedPorts(PORT)
            .withCommand(USER + ":" + PASSWORD + ":1001:::upload");

    static {
        SFTP.start();
        System.setProperty("spring.sftp.host", SFTP.getHost());
        System.setProperty("spring.sftp.port", SFTP.getFirstMappedPort().toString());
        log.info("🐳 Sftp container started.");
    }

    @Override
    public void afterTestClass(TestContext testContext) {
        SFTP.close();
    }

}
