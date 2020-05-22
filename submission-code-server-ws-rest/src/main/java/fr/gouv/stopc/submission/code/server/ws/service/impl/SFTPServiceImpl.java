package fr.gouv.stopc.submission.code.server.ws.service.impl;

import com.jcraft.jsch.*;
import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.ws.dto.SftpUser;
import fr.gouv.stopc.submission.code.server.ws.service.ISFTPService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Properties;

@Slf4j
@Service
public class SFTPServiceImpl implements ISFTPService {


    private static final String ALGORITHM_SHA256 = "SHA-256";
    private static final String ALGORITHM_MD5 = "MD5";

    @Value("${submission.code.server.sftp.remote.host}")
    private String remoteDir;

    @Value("${submission.code.server.sftp.user}")
    private String username;

    @Value("${submission.code.server.sftp.key}")
    private String keyPrivate;

    /**
     * sftp passphrase stored in a byte array.
     */
    private byte[] passphrase;

    @Value("${submission.code.server.sftp.port}")
    private int port;

    /**TargetZoneId is the time zone id (in the java.time.ZoneId way) on which the submission code server should deliver the codes. eg.: for France is "Europe/Paris"*/
    @Value("${stop.covid.qr.code.targetzone}")
    private String targetZoneId;

    @Value("${zip.filename.formatter}")
    private String zipFilenameFormat;

    @Value("${submission.code.server.sftp.path}")
    private String pathFile;

    @Value("${digest.filename.formatter.sha256}")
    private String digestFileNameFormatSHA256;

    @Value("${digest.filename.formatter.md5}")
    private String digestFileNameFormatMD5;

    public SFTPServiceImpl(@Value("${submission.code.server.sftp.passphrase}") final String passphrase) {
        if(passphrase != null) {
            try {
                this.passphrase = Base64.getDecoder().decode(passphrase);
            } catch(Exception e ) {
                log.error("Error trying to parse Base64 passphrase to byte[] ", e);
                this.passphrase = null;
            }
        }
    }

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

        this.createDigestThenTransferToSFTP(file, channelSftp,ALGORITHM_SHA256,digestFileNameFormatSHA256);
        this.createDigestThenTransferToSFTP(file,channelSftp,ALGORITHM_MD5,digestFileNameFormatMD5);

        log.info("SFTP: connection is about to be closed");
        channelSftp.exit();
        log.info("SFTP: connection closed");
    }


    /**
     * Create connection SFTP to transfer file in server.
     * The connection is created with user and private key of user.
     * @return An object channelSftp.
     */
    private ChannelSftp createConnection() throws SubmissionCodeServerException{
        try{
            JSch jSch = new JSch();

            SftpUser userInfo = new SftpUser(this.username, this.passphrase);
            Session jsSession= jSch.getSession(this.username, this.remoteDir, this.port);
            jSch.addIdentity(this.keyPrivate, this.passphrase);

            Properties config = new Properties();

            config.put("StrictHostKeyChecking", "true");

            config.put("cipher.s2c", "aes256-ctr,aes256-cbc");
            config.put("cipher.c2s", "aes256-ctr,aes256-cbc");

            config.put("mac.s2c", "hmac-sha2-256");
            config.put("mac.c2s", "hmac-sha2-256");

            config.put("kex", "ecdh-sha2-nistp256,ecdh-sha2-nistp384,ecdh-sha2-nistp521");
            config.put("server_host_key", "ssh-rsa,ecdsa-sha2-nistp256,ecdsa-sha2-nistp384,ecdsa-sha2-nistp521");

            jsSession.setUserInfo(userInfo);
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
    private void createDigestThenTransferToSFTP(final ByteArrayOutputStream file, final ChannelSftp channelSftp, String algorithm, String digestFileNameFormat ) throws SubmissionCodeServerException {
        log.info("Transferring digest file to SFTP");

        try{
            // Formatting the name of the digest file
            OffsetDateTime date = OffsetDateTime.now(ZoneId.of(targetZoneId));
            String fileNameDigest = String.format(digestFileNameFormat, date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            byte[] hash = messageDigest.digest(file.toByteArray());


            byte[] data = DatatypeConverter
                    .printHexBinary(hash)
                    .toLowerCase()
                    .getBytes(StandardCharsets.UTF_8);

            log.info("SFTP: is about to pushed the digest file. {}", fileNameDigest);
            channelSftp.put(new ByteArrayInputStream(data), fileNameDigest);
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
