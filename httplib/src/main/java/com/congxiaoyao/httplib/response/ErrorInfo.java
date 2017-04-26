package com.congxiaoyao.httplib.response;

/**
 * Created by Jaycejia on 2016/12/4.
 */
public class ErrorInfo {

    public static final int UNKNOWN = -1;
    public static final int AUTHENTICATION_ERROR = -2;
    public static final int AUTHORIZATION_ERROR = -3;
    public static final int DAO_ERROR = -4;

    private int errorCode;
    private String errorMessage;
    private String exceptionName;
    private Throwable exception;

    public ErrorInfo() {
    }

    public ErrorInfo(int errorCode, String errorMessage, Throwable exp) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.exceptionName = exp.getClass().getName();
        this.exception = exp;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getExceptionName() {
        return exceptionName;
    }

    public void setExceptionName(String exceptionName) {
        this.exceptionName = exceptionName;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }
}
