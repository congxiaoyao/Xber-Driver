package com.congxiaoyao.httplib;

import com.congxiaoyao.httplib.request.CarRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.exception.AuthException;
import com.congxiaoyao.httplib.response.exception.EmptyDataException;
import com.congxiaoyao.httplib.response.exception.ExceptionDispatcher;
import com.congxiaoyao.httplib.response.exception.IExceptionHandler;
import com.congxiaoyao.httplib.response.exception.LoginException;
import com.congxiaoyao.httplib.response.exception.NetWorkException;
import com.congxiaoyao.httplib.response.exception.PermissionDeniedException;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;

public class MainClass implements IExceptionHandler{

    public static void main(String[] args) throws InterruptedException {

    }

    @Override
    public void onDispatchException(Throwable throwable) {

    }

    @Override
    public void onLoginError(LoginException exception) {

    }

    @Override
    public void onAuthError(AuthException exception) {

    }

    @Override
    public void onPermissionError(PermissionDeniedException exception) {

    }

    @Override
    public void onEmptyDataError(EmptyDataException exception) {

    }

    @Override
    public boolean onTimeoutError(SocketTimeoutException exception) {
        return false;
    }

    @Override
    public boolean onUnknowHostError(UnknownHostException exception) {
        return false;
    }

    @Override
    public void onNetworkError(NetWorkException exception) {

    }

    @Override
    public void unKnowError(Throwable throwable) {

    }
}
