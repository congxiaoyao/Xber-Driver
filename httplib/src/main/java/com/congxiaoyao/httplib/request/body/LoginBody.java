package com.congxiaoyao.httplib.request.body;

/**
 * Created by congxiaoyao on 2017/2/12.
 */
public class LoginBody {

    private String username;
    private String password;
    private String clientId;

    public LoginBody() {
    }

    public LoginBody(String username, String password, String clientId) {
        this.username = username;
        this.password = password;
        this.clientId = clientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
