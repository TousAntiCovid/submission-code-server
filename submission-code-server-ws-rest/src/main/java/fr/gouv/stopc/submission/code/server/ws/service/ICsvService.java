package fr.gouv.stopc.submission.code.server.ws.service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Optional;

public interface ICsvService {
    /**
     * The method return file wrapped with data in database with lots match lots in the request
     * @param lots
     * @return
     */
    Optional<StringWriter> csvExport(String lots) throws IOException;
}
