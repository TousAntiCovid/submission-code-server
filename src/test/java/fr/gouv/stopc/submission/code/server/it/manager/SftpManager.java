package fr.gouv.stopc.submission.code.server.it.manager;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.springframework.test.context.TestExecutionListener;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.stream.Collectors;

@Slf4j
public class SftpManager implements TestExecutionListener {

    private static final int PORT = 22;

    private static final String USER = "user";

    private static final String PASSWORD = "password";

    private static final byte[] PASSPHRASE = Base64.getDecoder().decode("cGFzc3BocmFzZQ==");

    private static final String HOST_KNOWN_FILE = "~/.ssh/known_hosts";

    private static final String HOST_KEY_ALGORITHM = "ssh-rsa";

    private static final String FSP = MountableFile.forClasspathResource("sftp/rsa_scs").getResolvedPath();

    private static final GenericContainer SFTP = new GenericContainer(
            new ImageFromDockerfile()
                    .withFileFromClasspath(".", "sftp/docker")
    )
            .withExposedPorts(PORT)
            .withCommand(USER + ":" + PASSWORD + ":1001:::upload");

    public static int getMappedPort() {
        return SFTP.getMappedPort(PORT);
    }

    static {
        SFTP.start();
        System.setProperty("SUBMISSION_CODE_SERVER_SFTP_HOST", SFTP.getHost());
        System.setProperty("SUBMISSION_CODE_SERVER_SFTP_PORT", SFTP.getMappedPort(PORT).toString());
        System.setProperty("SUBMISSION_CODE_SERVER_SFTP_KEY", FSP);
    }

    @SneakyThrows
    public static ListAssert<String> assertThatAllFilesFromSftp() {

        log.debug("SFTP: connection is about to be created");
        ChannelSftp channelSftp = createConnection();
        log.debug("SFTP: connexion created");

        log.debug("SFTP: list files in upload directory");
        List<String> listAssert;
        Vector<ChannelSftp.LsEntry> ls = channelSftp.ls("/upload");
        listAssert = ls.stream()
                .filter(lsEntry -> !lsEntry.getAttrs().isDir())
                .map(ChannelSftp.LsEntry::getFilename)
                .collect(Collectors.toList());

        log.debug("SFTP: connection is about to be closed");
        channelSftp.exit();
        log.debug("SFTP: connection closed");

        return Assertions.assertThat(listAssert);
    }

    @SneakyThrows
    public static void purgeSftp() {
        ChannelSftp channelSftp = createConnection();
        Vector<ChannelSftp.LsEntry> ls = channelSftp.ls("/upload");
        ls.stream()
                .filter(lsEntry -> !lsEntry.getAttrs().isDir()).forEach(lsEntry -> {
                    try {
                        channelSftp.rm(lsEntry.getFilename());
                    } catch (SftpException e) {
                        throw new RuntimeException(e);
                    }
                });
        channelSftp.exit();
    }

    @SneakyThrows
    public static List<File> getAllFilesFromSftp(File tmpDirectory) {
        List<File> fileList;
        ChannelSftp channelSftp = createConnection();
        Vector<ChannelSftp.LsEntry> ls = channelSftp.ls("/upload");
        fileList = ls.stream()
                .filter(lsEntry -> !lsEntry.getAttrs().isDir())
                .map(lsEntry -> {
                    try {
                        channelSftp.get(
                                "/upload/"
                                        .concat(lsEntry.getFilename()),
                                tmpDirectory.getAbsolutePath()
                        );
                        return new File(
                                tmpDirectory.getAbsolutePath()
                                        .concat(File.separator)
                                        .concat(lsEntry.getFilename())
                        );
                    } catch (SftpException e) {
                        throw new RuntimeException(e);
                    }

                })
                .collect(Collectors.toList());
        channelSftp.exit();
        return fileList;
    }

    /**
     * Create connection SFTP to transfer file in server. The connection is created
     * with user and private key of user.
     *
     * @return An object channelSftp.
     */
    private static ChannelSftp createConnection() {
        try {
            JSch jSch = new JSch();

            Session jsSession = jSch.getSession(USER, SFTP.getHost(), SFTP.getMappedPort(PORT));
            jSch.addIdentity(FSP, PASSPHRASE);

            jSch.setKnownHosts(HOST_KNOWN_FILE);

            Properties config = new Properties();

            config.put("StrictHostKeyChecking", "no");

            config.put("cipher.s2c", "aes256-ctr,aes256-cbc");
            config.put("cipher.c2s", "aes256-ctr,aes256-cbc");

            config.put("mac.s2c", "hmac-sha2-256");
            config.put("mac.c2s", "hmac-sha2-256");

            config.put("kex", "ecdh-sha2-nistp256,ecdh-sha2-nistp384,ecdh-sha2-nistp521");

            config.put("server_host_key", HOST_KEY_ALGORITHM);

            jsSession.setConfig(config);

            jsSession.connect();

            final ChannelSftp sftp = (ChannelSftp) jsSession.openChannel("sftp");

            // attempting to open a jsSession.
            sftp.connect();

            sftp.cd("/upload");

            return sftp;
        } catch (JSchException | SftpException e) {
            throw new UnsupportedOperationException("unexpected error occurred", e);
        }
    }

}
