package fr.gouv.stopc.submission.code.server.ws.service.impl;

import com.jcraft.jsch.*;
import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.ws.dto.SftpUser;
import fr.gouv.stopc.submission.code.server.ws.service.ISFTPService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Slf4j
@Service
public class SFTPServiceImpl implements ISFTPService {


    @Value("${submission.code.server.sftp.remote.host}")
    private String remoteDir;

    @Value("${submission.code.server.sftp.user}")
    private String username;

    @Value("${submission.code.server.sftp.key}")
    private String keyPrivate;

    @Value("${submission.code.server.sftp.passphrase}")
    private String passphrase;

    @Value("${submission.code.server.sftp.port}")
    private int port;

    @Value("${stop.covid.qr.code.target.zone}")
    private String targetZoneId;

    @Value("${zip.filename.formatter}")
    private String zipFilenameFormat;

    @Value("{submission.code.server.sftp.path")
    private String pathFile;


    @Override
    @Async
    public void transferFileSFTP(ByteArrayInputStream file) throws SubmissionCodeServerException {
        log.info("Transferring zip file to SFTP");

        log.info("SFTP: connection is about to be created");
        ChannelSftp channelSftp = createConnexion();
        log.info("SFTP: connexion created");

        log.info("SFTP: connection is about to be connected");

        try {
            channelSftp.connect();
        } catch (JSchException jshe) {
            log.error(SubmissionCodeServerException.ExceptionEnum.SFTP_CONNECTION_FAILED_ERROR.getMessage(), jshe);
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.SFTP_CONNECTION_FAILED_ERROR,
                    jshe
            );
        }

        log.info("SFTP: connected");

        OffsetDateTime date = OffsetDateTime.now(ZoneId.of(targetZoneId));
        String fileNameZip = String.format(zipFilenameFormat, date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        log.info("SFTP: is about to pushed the zip file.");
        try {
            channelSftp.put(file, pathFile +fileNameZip);
        } catch (SftpException e) {
            log.error(SubmissionCodeServerException.ExceptionEnum.SFTP_FILE_PUSHING_FAILED_ERROR.getMessage(), e);
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.SFTP_FILE_PUSHING_FAILED_ERROR,
                    e
            );
        }
        log.info("SFTP: files have been pushed");

        log.info("SFTP: connection is about to be closed");
        channelSftp.exit();
        log.info("SFTP: connection closed");
    }


    /**
     * TODO: comment method.
     * @return
     */
    private ChannelSftp createConnexion() throws SubmissionCodeServerException{
        try{
            JSch jSch = new JSch();

            jSch.addIdentity(keyPrivate, passphrase);
            Session jsSession= jSch.getSession(username, remoteDir, port);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "false");
            SftpUser userInfo = new SftpUser(username, passphrase);
            jsSession.setUserInfo(userInfo);
            jsSession.setConfig(config);

            // attempting to open a jsSession.
            jsSession.connect();

            return (ChannelSftp) jsSession.openChannel("sftp");
        } catch (JSchException jshe){
            log.error(SubmissionCodeServerException.ExceptionEnum.JSCH_SESSION_CREATION_FAILED_ERROR.getMessage(), jshe);
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.JSCH_SESSION_CREATION_FAILED_ERROR,
                    jshe
            );
        }

    }
}
