package fr.gouv.stopc.submission.code.server.ws.service;

import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import org.springframework.scheduling.annotation.Async;

import java.io.ByteArrayInputStream;

public interface ISFTPService {
     void transferFileSFTP(ByteArrayInputStream file) throws SubmissionCodeServerException;

    @Async
     void transferFileSFTPAsync(ByteArrayInputStream file) throws SubmissionCodeServerException;

}
