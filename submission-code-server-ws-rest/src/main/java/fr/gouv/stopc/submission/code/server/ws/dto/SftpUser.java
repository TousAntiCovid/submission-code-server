package fr.gouv.stopc.submission.code.server.ws.dto;

import com.jcraft.jsch.UserInfo;

public class SftpUser implements UserInfo {
    private final String username;
    private final byte[] password;

    public SftpUser(String username, byte[] password){
        this.password = password;
        this.username = username;
    }

    @Override
    public String getPassphrase() {
        return null;
    }

    @Override
    public String getPassword() {
        return new String(this.password);
    }

    @Override
    public boolean promptPassword(String s) {
        return true;
    }

    @Override
    public boolean promptPassphrase(String s) {
        return true;
    }

    @Override
    public boolean promptYesNo(String s) {
        return true;
    }

    @Override
    public void showMessage(String s) {

    }

    public String getUsername() {
        return username;
    }
}
