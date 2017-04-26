package com.congxiaoyao.httplib.response.exception;

/**
 * Created by congxiaoyao on 2017/3/16.
 */

public class LoginException extends RuntimeException {

    private static String USERNAME_PWD_ERROR = "�û������������";
    private static String USER_NOT_FOUND = "�Ҳ������û�";

    public LoginException(String message) {
        super(message);
    }

    public static boolean isLoginException(String errorMessage) {
        return USERNAME_PWD_ERROR.equals(errorMessage) ||
                USER_NOT_FOUND.equals(errorMessage);
    }
}
