package fr.gouv.stopc.submission.code.server.commun.service;


public interface IAlphaNumericCodeService {

    /**
     * generate and stringify alphanum-6
     * @return  A randomly generated code of 6 insensitive case alphanumeric char
     */
    String generateCode();

}
