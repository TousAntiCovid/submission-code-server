package fr.gouv.stopc.submission.code.server.business.service;

import com.jcraft.jsch.*;
import fr.gouv.stopc.submission.code.server.business.controller.exception.SubmissionCodeServerException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class SFTPService {

    private static final String ALGORITHM_SHA256 = "SHA-256";

    public static final String DATEFORMATFILE = "yyyyMMddHHmmss";

    @Value("${submission.code.server.sftp.host}")
    private String host;

    @Value("${submission.code.server.sftp.host.port}")
    @Setter
    private int port;

    @Value("${submission.code.server.sftp.host.key.algorithm}")
    private String hostKeyAlgorithm;

    @Value("${submission.code.server.sftp.knownhosts.file}")
    private String knownHostFile;

    @Value("${submission.code.server.sftp.host.path}")
    private String pathFile;

    @Value("${submission.code.server.sftp.user}")
    private String username;

    @Value("${submission.code.server.sftp.key}")
    private String keyPrivate;

    /**
     * sftp passphrase stored in a byte array.
     */
    private byte[] passphrase;

    /**
     * TargetZoneId is the time zone id (in the java.time.ZoneId way) on which the
     * submission code server should deliver the codes. eg.: for France is
     * "Europe/Paris"
     */
    @Value("${stop.covid.qr.code.targetzone}")
    private String targetZoneId;

    @Value("${zip.filename.formatter}")
    private String zipFilenameFormat;

    @Value("${digest.filename.formatter.sha256}")
    private String digestFileNameFormatSHA256;

    @Value("${submission.code.server.sftp.enablestricthost}")
    private String strictHostCheck;

    public SFTPService(@Value("${submission.code.server.sftp.passphrase}") final String passphrase) {
        if (passphrase != null) {
            try {
                this.passphrase = Base64.getDecoder().decode(passphrase);
            } catch (Exception e) {
                log.error("Error trying to parse Base64 passphrase to byte[] ", e);
                this.passphrase = null;
            }
        }
    }

    public void transferFileSFTP(ByteArrayOutputStream file) throws SubmissionCodeServerException {
        ChannelSftp channelSftp = null;
        log.info("Transferring zip file to SFTP");

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(file.toByteArray())) {

            log.info("SFTP: connection is about to be created");
            channelSftp = createConnection();
            log.info("SFTP: connexion created");

            OffsetDateTime date = OffsetDateTime.now(ZoneId.of(targetZoneId));
            String dateFile = date.format(DateTimeFormatter.ofPattern(DATEFORMATFILE));
            String fileNameZip = String.format(zipFilenameFormat, dateFile);

            log.info("SFTP: is about to pushed the zip file.");
            pushStreamToSftp(channelSftp, inputStream, fileNameZip);
            log.info("SFTP: files have been pushed");

            this.createDigestThenTransferToSFTP(
                    file, channelSftp, ALGORITHM_SHA256, digestFileNameFormatSHA256, dateFile
            );
        } catch (IOException e) {
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.UNQUALIFIED_ERROR,
                    e
            );
        } finally {
            closeConnection(channelSftp);
        }
    }

    public void pushStreamToSftp(ChannelSftp channelSftp, InputStream inputStream, String fileNameZip) {
        try {
            channelSftp.put(inputStream, fileNameZip);
        } catch (SftpException e) {
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.SFTP_FILE_PUSHING_FAILED_ERROR,
                    e
            );
        }
    }

    public void closeConnection(ChannelSftp channelSftp) {
        try {
            if (null != channelSftp) {
                log.info("SFTP: connection is about to be closed");
                channelSftp.exit();
                channelSftp.getSession().disconnect();
                log.info("SFTP: connection closed");
            }
        } catch (JSchException e) {
            throw new SubmissionCodeServerException(SubmissionCodeServerException.ExceptionEnum.UNQUALIFIED_ERROR, e);
        }
    }

    /**
     * Create connection SFTP to transfer file in server. The connection is created
     * with user and private key of user.
     * 
     * @return An object channelSftp.
     */
    public ChannelSftp createConnection() throws SubmissionCodeServerException {
        try {
            JSch jSch = new JSch();

            Session jsSession = jSch.getSession(this.username, this.host, this.port);
            jSch.addIdentity(this.keyPrivate, this.passphrase);

            // /!\ mandatory set the knownhosts file.
            if (StringUtils.isNotBlank(this.knownHostFile)) {
                log.info("Using known_hosts file specified in configuration: {} ", this.knownHostFile);
                jSch.setKnownHosts(this.knownHostFile);
            }

            Properties config = new Properties();

            config.put("StrictHostKeyChecking", strictHostCheck);

            config.put("cipher.s2c", "aes256-ctr,aes256-cbc");
            config.put("cipher.c2s", "aes256-ctr,aes256-cbc");

            config.put("mac.s2c", "hmac-sha2-256");
            config.put("mac.c2s", "hmac-sha2-256");

            config.put("kex", "ecdh-sha2-nistp256,ecdh-sha2-nistp384,ecdh-sha2-nistp521");

            // /!\ set of only one server host key algorithm
            if (StringUtils.isNotBlank(this.hostKeyAlgorithm)) {
                log.info("Using Host key algorithm specified in configuration: {} ", this.hostKeyAlgorithm);
                config.put("server_host_key", this.hostKeyAlgorithm);
            }

            jsSession.setConfig(config);

            jsSession.connect();

            final ChannelSftp sftp = (ChannelSftp) jsSession.openChannel("sftp");

            // attempting to open a jsSession.
            sftp.connect();

            if (StringUtils.isNotBlank(this.pathFile)) {
                sftp.cd(this.pathFile);
            }

            return sftp;
        } catch (JSchException jshe) {
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.JSCH_SESSION_CREATION_FAILED_ERROR,
                    jshe
            );
        } catch (SftpException e) {
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.SFTP_WORKING_DIRECTORY_ERROR,
                    e
            );
        }

    }

    /**
     * Create sha256 from file already uploaded on the SFTP Server and transfers
     * sha256 to SFTP server.
     * 
     * @param file        the file from the sha256 should be generated.
     * @param channelSftp already opened channel. Should be an open connection.
     * @throws SubmissionCodeServerException if an error occurs at sha256
     *                                       instantiation or if the sha256 file
     *                                       cannot be pushed to SFTP server
     */
    private void createDigestThenTransferToSFTP(final ByteArrayOutputStream file, final ChannelSftp channelSftp,
            String algorithm, String digestFileNameFormat, String dateFile) throws SubmissionCodeServerException {
        log.info("Transferring digest file to SFTP");

        try {
            // Formatting the name of the digest file
            String fileNameDigest = String.format(digestFileNameFormat, dateFile);

            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            byte[] hash = messageDigest.digest(file.toByteArray());

            byte[] data = DatatypeConverter
                    .printHexBinary(hash)
                    .toLowerCase()
                    .getBytes(StandardCharsets.UTF_8);

            log.info("SFTP: is about to pushed the digest file. {}", fileNameDigest);
            channelSftp.put(new ByteArrayInputStream(data), fileNameDigest);
            log.info("SFTP: files have been pushed");

        } catch (SftpException | NoSuchAlgorithmException e) {
            log.error(SubmissionCodeServerException.ExceptionEnum.SFTP_FILE_PUSHING_FAILED_ERROR.getMessage(), e);
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.SFTP_FILE_PUSHING_FAILED_ERROR,
                    e
            );
        }

    }
}
