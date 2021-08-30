package fr.gouv.stopc.submission.code.server.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import fr.gouv.stopc.submission.code.server.business.service.SFTPService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

@Slf4j
public class SftpManager implements TestExecutionListener {

    private static final int PORT = 22;

    private static final String USER = "user";

    private static final String PASSWORD = "password";

    private static final String UPLOAD = "/upload";

    private static final String SEPARATOR = "/";

    private static final GenericContainer SFTP = new GenericContainer(
            new ImageFromDockerfile()
                    .withFileFromClasspath(".", "sftp/docker")
    )
            .withExposedPorts(PORT)
            .withCommand(USER + ":" + PASSWORD + ":1001:::upload");

    static {
        SFTP.start();
        System.setProperty("SUBMISSION_CODE_SERVER_SFTP_HOST", SFTP.getHost());
        System.setProperty("SUBMISSION_CODE_SERVER_SFTP_PORT", SFTP.getMappedPort(PORT).toString());
        String fsp = MountableFile.forClasspathResource("sftp/rsa_scs").getResolvedPath();
        System.setProperty("SUBMISSION_CODE_SERVER_SFTP_KEY", fsp);
    }

    @Override
    public void afterTestClass(TestContext testContext) {
        SFTP.close();
    }

    @SneakyThrows
    public static ListAssert<String> assertThatAllFilesFromSftp(SFTPService sftpService) {

        log.debug("SFTP: connection is about to be created");
        ChannelSftp channelSftp = sftpService.createConnection();
        log.debug("SFTP: connexion created");

        log.debug("SFTP: list files in upload directory");
        List<String> listAssert = new ArrayList<>();
        Vector<ChannelSftp.LsEntry> ls = channelSftp.ls(UPLOAD);
        for (ChannelSftp.LsEntry lsEntry : ls) {
            if (!lsEntry.getAttrs().isDir()) {
                listAssert.add(lsEntry.getFilename());
                log.debug(">>> {}", String.valueOf(lsEntry.getFilename()));
            }
        }

        log.debug("SFTP: connection is about to be closed");
        channelSftp.exit();
        log.debug("SFTP: connection closed");

        return Assertions.assertThat(listAssert);
    }

    @SneakyThrows
    public static File getFileFromSftp(SFTPService sftpService, String fileName, File tmpDirectory) {
        ChannelSftp channelSftp = sftpService.createConnection();
        channelSftp.get(
                UPLOAD
                        .concat(SEPARATOR)
                        .concat(fileName),
                tmpDirectory.getAbsolutePath()
        );
        File file = new File(
                tmpDirectory.getAbsolutePath()
                        .concat(File.separator)
                        .concat(fileName)
        );
        return file;
    }

    @SneakyThrows
    public static List<String> getAllFilesFromSftp(SFTPService sftpService, File tmpDirectory) {
        List<String> fileList;
        ChannelSftp channelSftp = sftpService.createConnection();

        Vector<ChannelSftp.LsEntry> ls = channelSftp.ls(UPLOAD);
        log.debug("Start list files");
        fileList = ls.stream()
                .filter(lsEntry -> !lsEntry.getAttrs().isDir())
                .map(lsEntry -> {
                    try {
                        channelSftp.get(
                                UPLOAD
                                        .concat(SEPARATOR)
                                        .concat(lsEntry.getFilename()),
                                tmpDirectory.getAbsolutePath()
                        );
                        return tmpDirectory.getAbsolutePath()
                                .concat(File.separator)
                                .concat(lsEntry.getFilename());
                    } catch (SftpException e) {
                        throw new RuntimeException(e);
                    }

                })
                .collect(Collectors.toList());
        log.debug("End list files");

        return fileList;
    }

    @SneakyThrows
    public static void pushFileToSftp(SFTPService sftpService, File file, String fileNameDest) {
        ChannelSftp channelSftp = sftpService.createConnection();
        log.debug("SFTP: is about to pushed the zip file.");
        try {
            channelSftp.put(new FileInputStream(file), fileNameDest);
        } catch (SftpException e) {
            throw new RuntimeException(e);
        } finally {
            channelSftp.exit();
        }
        log.debug("SFTP: files have been pushed");
    }

}
