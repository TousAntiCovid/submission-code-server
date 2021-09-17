package fr.gouv.stopc.submission.code.server.sftp.manager;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import fr.gouv.stopc.submission.code.server.business.service.SFTPService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.springframework.test.context.TestExecutionListener;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

@Slf4j
public class SftpManager implements TestExecutionListener {

    private static final int PORT = 22;

    private static final String USER = "user";

    private static final String PASSWORD = "password";

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

    @SneakyThrows
    public static ListAssert<String> assertThatAllFilesFromSftp(SFTPService sftpService) {

        log.debug("SFTP: connection is about to be created");
        ChannelSftp channelSftp = sftpService.createConnection();
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
    public static void purgeSftp(SFTPService sftpService) {
        ChannelSftp channelSftp = sftpService.createConnection();
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
    public static List<File> getAllFilesFromSftp(SFTPService sftpService, File tmpDirectory) {
        List<File> fileList;
        ChannelSftp channelSftp = sftpService.createConnection();
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

}
