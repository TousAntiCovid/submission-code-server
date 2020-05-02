package fr.gouv.stopc.submission.code.server.commun.service;

import java.util.List;

public interface IUUIDv4CodeService {

    /**
     * generate and stringify uuidv4
     * @return  A randomly generated {@code UUID}
     */
     String generateCode();

    /**
     *
     * @param size number of code to be generated
     * @return
     */
     List<String> generateCodes(long size);

}
