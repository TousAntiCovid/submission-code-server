package fr.gouv.stopc.submission.code.server.business.service;

import fr.gouv.stopc.submission.code.server.business.controller.error.SubmissionCodeServerException;

import java.io.ByteArrayOutputStream;

public interface ISFTPService {

    void transferFileSFTP(ByteArrayOutputStream file) throws SubmissionCodeServerException;
}
