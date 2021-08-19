package fr.gouv.stopc.submission.code.server.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import fr.gouv.stopc.submission.code.server.business.controller.exception.SubmissionCodeServerException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Slf4j
public class SftpManager implements TestExecutionListener {

    private static final int PORT = 22;

    private static final String USER = "user";

    private static final String PASSWORD = "password";

    private static final GenericContainer SFTP = new GenericContainer(
            new ImageFromDockerfile()
                    .withDockerfileFromBuilder(
                            builder -> builder
                                    .from("atmoz/sftp:latest")
                                    .run("mkdir -p /home/" + USER + "/upload; chmod -R 007 /home/" + USER)
                                    .build()
                    )
    )
            .withExposedPorts(PORT)
            .withCommand(USER + ":" + PASSWORD + ":1001:::upload");

    static {
        SFTP.start();
        System.setProperty("test.sftp.host", SFTP.getHost());
        System.setProperty("test.sftp.port", SFTP.getMappedPort(PORT).toString());
    }

    @Override
    public void afterTestClass(TestContext testContext) {
        SFTP.close();
    }

    @SneakyThrows
    public static ListAssert<String> assertThatAllFilesFromSftp() {

        log.debug("SFTP: connection is about to be created");
        ChannelSftp channelSftp = SftpManager.createConnection();
        log.debug("SFTP: connexion created");

        log.debug("===> SFTP: ls -lah /home/foo/upload");
        List<String> listAssert = new ArrayList<>();
        Vector<ChannelSftp.LsEntry> ls = channelSftp.ls("upload");
        for (ChannelSftp.LsEntry lsEntry : ls) {
            listAssert.add(lsEntry.getFilename());
            log.debug(">>> {}", String.valueOf(lsEntry.getFilename()));
        }
        log.debug("<=== SFTP: ls -lah /home/foo/upload");

        log.debug("SFTP: connection is about to be closed");
        channelSftp.exit();
        log.debug("SFTP: connection closed");

        return Assertions.assertThat(listAssert);
    }

    /**
     * Create connection SFTP to transfer file in server. The connection is created
     * with user and private key of user.
     *
     * @return An object channelSftp.
     */
    public static ChannelSftp createConnection() throws SubmissionCodeServerException {
        JSch jSch = new JSch();
        try {
            log.debug("SFTP: host : {}", System.getProperty("test.sftp.host"));
            log.debug("SFTP: port : {}", System.getProperty("test.sftp.port"));
            Session jsSession = jSch.getSession(
                    "user", System.getProperty("test.sftp.host"),
                    Integer.parseInt(System.getProperty("test.sftp.port"))
            );
            jsSession.setConfig("StrictHostKeyChecking", "no");
            jsSession.setPassword("password");
            jsSession.connect();
            final ChannelSftp sftp = (ChannelSftp) jsSession.openChannel("sftp");
            sftp.connect();
            return sftp;
        } catch (JSchException e) {
            e.printStackTrace();
            return null;
        }
    }

}
