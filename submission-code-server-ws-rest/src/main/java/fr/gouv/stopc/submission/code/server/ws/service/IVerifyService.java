package fr.gouv.stopc.submission.code.server.ws.service;

public interface IVerifyService {
    public boolean verifyCode(String code, String type);
}
