package fr.gouv.stopc.submission.code.server.ws.service;

public interface IGenerateService {
    /**
     * @return UUIDv4 code certified unique in DB
     */
    public String generateUUIDv4Code();

    /**
     * @return alphanum-6 code certified unique in DB
     */
    public String generateAlphaNumericCode();
}
