package fr.gouv.stopc.submission.code.server.commun.service;

import java.util.UUID;

public interface IAlphaNumericCodeService {

    /**
     * generate and stringify alphanum-6
     * @return  A randomly generated code of 6 insensitive case alphanumeric char
     */
    String generateCode();

}
