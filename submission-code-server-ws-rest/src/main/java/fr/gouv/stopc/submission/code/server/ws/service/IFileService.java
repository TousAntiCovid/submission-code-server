package fr.gouv.stopc.submission.code.server.ws.service;

import java.io.StringWriter;
import java.util.Optional;
import java.util.zip.ZipOutputStream;

public interface IFileService {

    /**
     *
     * @param numberCodeDay
     * @param lot
     * @param dateFrom
     * @param dateTo
     * @return
     */
    public Optional<ZipOutputStream> zipExport(String numberCodeDay, String lot, String dateFrom, String dateTo);

}
