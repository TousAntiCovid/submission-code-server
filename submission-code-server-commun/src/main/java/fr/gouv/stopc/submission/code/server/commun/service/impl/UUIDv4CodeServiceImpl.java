package fr.gouv.stopc.submission.code.server.commun.service.impl;

import fr.gouv.stopc.submission.code.server.commun.service.IUUIDv4CodeService;

import java.util.UUID;

public class UUIDv4CodeServiceImpl implements IUUIDv4CodeService {

    /**
     * generate and stringify uuidv4
     * @return  A randomly generated {@code UUID}
     */
    public String generateCode() {
        return   UUID.randomUUID().toString();
    }
}
