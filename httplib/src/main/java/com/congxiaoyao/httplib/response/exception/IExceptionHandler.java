package com.congxiaoyao.httplib.response.exception;

import com.congxiaoyao.httplib.request.retrofit2.adapter.rxjava.HttpException;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by congxiaoyao on 2016/8/25.
 */
public interface IExceptionHandler {

    void onDispatchException(Throwable throwable);

    void onLoginError(LoginException exception);

    void onAuthError(AuthException exception);

    void onPermissionError(PermissionDeniedException exception);

    void onEmptyDataError(EmptyDataException exception);

    boolean onTimeoutError(SocketTimeoutException exception);

    boolean onUnknowHostError(UnknownHostException exception);

    void onNetworkError(NetWorkException exception);

    void unKnowError(Throwable throwable);

}
