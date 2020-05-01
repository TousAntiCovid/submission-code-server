package fr.gouv.stopc.submission.code.server.commun.service;

import java.util.UUID;

public interface IUUIDv4CodeService {

    /**
     * generate and stringify uuidv4
     * @return  A randomly generated {@code UUID}
     */
     String generateCode();

}
