package fr.gouv.stopc.submission.code.server.ws.service;

import java.io.StringWriter;
import java.util.Optional;
import java.util.zip.ZipOutputStream;

public interface IFileService {

    /**
     * Method:
     * 1)generate the codes type UUIDv4 between dateFrom to dateTo
     * 2)export from database
     * 3)create one csv file each day between dateFrom to dateTo
     * 4)create file zip with csv files
     * @param numberCodeDay
     * @param lot
     * @param dateFrom
     * @param dateTo
     * @return
     */
    public Optional<ZipOutputStream> zipExport(String numberCodeDay, String lot, String dateFrom, String dateTo) throws Exception;

}
