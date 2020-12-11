package fr.gouv.stopc.submission.code.server.ws.service;

public interface IPurgeOlderCodesService {
    void deleteExpiredCodes();
}
