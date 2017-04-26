package com.congxiaoyao.httplib.response.exception;

import com.congxiaoyao.httplib.request.gson.GsonHelper;
import com.congxiaoyao.httplib.request.retrofit2.adapter.rxjava.HttpException;
import com.congxiaoyao.httplib.response.ErrorInfo;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by congxiaoyao on 2016/9/8.
 */
public class ExceptionDispatcher {

    private IExceptionHandler exceptionHandler;

    public void dispatchException(Throwable throwable) {

        //分发错误之前回调下
        exceptionHandler.onDispatchException(throwable);

        if (throwable instanceof HttpException) {
            ErrorInfo errorInfo = null;
            HttpException exception = (HttpException) throwable;
            try {
                String errorbody = exception.response().errorBody().string();
                errorInfo = GsonHelper.getInstance().fromJson(errorbody, ErrorInfo.class);
            } catch (IOException e) {
                exceptionHandler.onNetworkError(new NetWorkException(e.getMessage()));
            }
            if(errorInfo == null) {
                exceptionHandler.unKnowError(throwable);
                return;
            }
            int errorCode = errorInfo.getErrorCode();
            String errorMessage = errorInfo.getErrorMessage();
            switch (errorCode) {
                case ErrorInfo.AUTHORIZATION_ERROR:
                    exceptionHandler.onPermissionError(new PermissionDeniedException());
                    return;
                case ErrorInfo.AUTHENTICATION_ERROR:
                    if (LoginException.isLoginException(errorMessage)) {
                        exceptionHandler.onLoginError(new LoginException(errorMessage));
                    }else {
                        exceptionHandler.onAuthError(new AuthException(errorMessage));
                    }
                    return;
                case ErrorInfo.DAO_ERROR:
                case ErrorInfo.UNKNOWN:
                    exceptionHandler.unKnowError(throwable);
                    return;
            }
        }

        //如果网络超时
        if (throwable instanceof SocketTimeoutException) {
            boolean handled = exceptionHandler.onTimeoutError((SocketTimeoutException) throwable);
            if (!handled) {
                exceptionHandler.onNetworkError(new NetWorkException(throwable.getMessage()));
            }
            return;
        }

        //如果未知域名错误
        else if (throwable instanceof UnknownHostException) {
            boolean handled = exceptionHandler.onUnknowHostError((UnknownHostException) throwable);
            if (!handled) {
                exceptionHandler.onNetworkError(new NetWorkException(throwable.getMessage()));
            }
            return;
        }

        if (throwable instanceof NetWorkException) {
            exceptionHandler.onNetworkError((NetWorkException) throwable);
            return;
        }

        if (throwable instanceof EmptyDataException) {
            exceptionHandler.onEmptyDataError((EmptyDataException) throwable);
            return;
        }

        //不是已知的错误
        exceptionHandler.unKnowError(throwable);
    }

    public void setExceptionHandler(IExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
}
