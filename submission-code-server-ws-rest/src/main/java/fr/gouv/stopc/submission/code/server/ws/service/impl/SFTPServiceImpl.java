package fr.gouv.stopc.submission.code.server.ws.service.impl;

import com.jcraft.jsch.*;
import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.ws.service.ISFTPService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    @Value("${submission.code.server.sftp.path}")
    private String pathFile;

    @Value("${md5.filename.formatter}")
    private String md5FileNameFormat;


    @Override
    public void transferFileSFTP(ByteArrayOutputStream file) throws SubmissionCodeServerException {

        log.info("Transferring zip file to SFTP");

        ByteArrayInputStream inputStream = new ByteArrayInputStream(file.toByteArray());

        log.info("SFTP: connection is about to be created");
        ChannelSftp channelSftp = createConnection();
        log.info("SFTP: connexion created");

        log.info("SFTP: connection is about to be connected");


        log.info("SFTP: connected");

        OffsetDateTime date = OffsetDateTime.now(ZoneId.of(targetZoneId));
        String fileNameZip = String.format(zipFilenameFormat, date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        log.info("SFTP: is about to pushed the zip file.");
        try {
            channelSftp.put(inputStream, fileNameZip);
        } catch (SftpException e) {
            channelSftp.exit();
            log.error(SubmissionCodeServerException.ExceptionEnum.SFTP_FILE_PUSHING_FAILED_ERROR.getMessage(), e);
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.SFTP_FILE_PUSHING_FAILED_ERROR,
                    e
            );
        }
        log.info("SFTP: files have been pushed");


        this.createMD5ThenTransferToSFTP(file, channelSftp);

        log.info("SFTP: connection is about to be closed");
        channelSftp.exit();
        log.info("SFTP: connection closed");
    }


    /**
     * Create connection SFTP to transfer file in server.
     * The connexion is create with user and key private of user.
     * @return
     */
    private ChannelSftp createConnection() throws SubmissionCodeServerException{
        try{
            JSch jSch = new JSch();
            jSch.addIdentity(keyPrivate,passphrase);
            Session jsSession= jSch.getSession(username, remoteDir, port);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "yes");
            jsSession.setConfig(config);

            jsSession.connect();

            final ChannelSftp sftp = (ChannelSftp) jsSession.openChannel("sftp");

            // attempting to open a jsSession.
            sftp.connect();

            if(StringUtils.isNotBlank(this.pathFile)) {
                sftp.cd(this.pathFile);
            }

            return sftp;
        } catch (JSchException jshe){
            log.error(SubmissionCodeServerException.ExceptionEnum.JSCH_SESSION_CREATION_FAILED_ERROR.getMessage(), jshe);
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.JSCH_SESSION_CREATION_FAILED_ERROR,
                    jshe
            );
        } catch (SftpException e) {
            log.error(SubmissionCodeServerException.ExceptionEnum.SFTP_WORKING_DIRECTORY_ERROR.getMessage(), e);
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.SFTP_WORKING_DIRECTORY_ERROR,
                    e
            );
        }

    }

    /**
     * Create md5 from file already uploaded on the SFTP Server and transfers md5 to SFTP server.
     * @param file the file from the MD5 should be generated.
     * @param channelSftp already opened channel. Should be an open connection.
     * @throws SubmissionCodeServerException if an error occurs at MD5 instantiation or if the MD5 file cannot be pushed to SFTP server
     */
    private void createMD5ThenTransferToSFTP(final ByteArrayOutputStream file, final ChannelSftp channelSftp) throws SubmissionCodeServerException {
        log.info("Transferring md5 file to SFTP");

        try{
            // Formatting the name of the md5 file
            OffsetDateTime date = OffsetDateTime.now(ZoneId.of(targetZoneId));
            String fileNameMD5 = String.format(md5FileNameFormat, date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] hash = messageDigest.digest(file.toByteArray());


            byte[] data = DatatypeConverter
                    .printHexBinary(hash)
                    .toLowerCase()
                    .getBytes(StandardCharsets.UTF_8);

            log.info("SFTP: is about to pushed the md5 file. {}", fileNameMD5);
            channelSftp.put(new ByteArrayInputStream(data), fileNameMD5);
            log.info("SFTP: files have been pushed");

        }  catch (SftpException | NoSuchAlgorithmException e) {
            log.error(SubmissionCodeServerException.ExceptionEnum.SFTP_FILE_PUSHING_FAILED_ERROR.getMessage(), e);
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.SFTP_FILE_PUSHING_FAILED_ERROR,
                    e
            );
        }

    }
}
