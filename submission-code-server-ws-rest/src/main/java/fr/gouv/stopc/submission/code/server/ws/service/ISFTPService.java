package fr.gouv.stopc.submission.code.server.ws.service;

import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;

import java.io.ByteArrayInputStream;

public interface ISFTPService {
     void transferFileSFTP(ByteArrayInputStream file) throws SubmissionCodeServerException;

}
