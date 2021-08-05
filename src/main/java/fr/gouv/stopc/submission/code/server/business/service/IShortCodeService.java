package fr.gouv.stopc.submission.code.server.business.service;

public interface IShortCodeService {

    /**
     * generate and stringify short code
     * 
     * @return A randomly generated code of insensitive case alphanumeric char
     */
    String generateCode();

}
