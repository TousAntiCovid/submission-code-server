package fr.gouv.stopc.submission.code.server.ws.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ICsvService {
    /**
     * The method return file wrapped with data in database with lots match lots in the request
     * @param lots
     * @return
     */
    File csvExport(String lots) throws IOException;
}
