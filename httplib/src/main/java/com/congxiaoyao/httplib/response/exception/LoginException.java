package com.congxiaoyao.httplib.response.exception;

/**
 * Created by congxiaoyao on 2017/3/16.
 */

public class LoginException extends RuntimeException {

    private static String USERNAME_PWD_ERROR = "用户名或密码错误";
    private static String USER_NOT_FOUND = "找不到该用户";

    public LoginException(String message) {
        super(message);
    }

    public static boolean isLoginException(String errorMessage) {
        return USERNAME_PWD_ERROR.equals(errorMessage) ||
                USER_NOT_FOUND.equals(errorMessage);
    }
}
