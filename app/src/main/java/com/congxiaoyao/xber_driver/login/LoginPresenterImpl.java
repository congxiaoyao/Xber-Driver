package com.congxiaoyao.xber_driver.login;

import android.app.Activity;
import android.util.Log;

import com.congxiaoyao.httplib.request.LoginRequest;
import com.congxiaoyao.httplib.request.UserRequest;
import com.congxiaoyao.httplib.request.body.LoginBody;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.httplib.response.LoginInfoResponse;
import com.congxiaoyao.httplib.response.exception.LoginException;
import com.congxiaoyao.xber_driver.Driver;
import com.congxiaoyao.xber_driver.TAG;
import com.congxiaoyao.xber_driver.mvpbase.presenter.BasePresenterImpl;
import com.congxiaoyao.xber_driver.utils.Token;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by congxiaoyao on 2017/3/15.
 */

public class LoginPresenterImpl extends BasePresenterImpl<LoginContract.View>
        implements LoginContract.Presenter{

    String userName = "";
    String password = "";

    public LoginPresenterImpl(LoginContract.View view) {
        super(view);
    }

    @Override
    public Driver getDriver() {
        return Driver.fromSharedPreference(view.getContext());
    }

    @Override
    public void login(String userName, String password) {
        this.userName = userName;
        this.password = password;
        subscribe();
    }

    @Override
    public void setLoginResult(int tag) {
        if (view.getContext() instanceof Activity) {
            Activity activity = (Activity) view.getContext();
            activity.setResult(tag);
        }
    }

    @Override
    public void subscribe() {
        final Driver driver = new Driver();
        LoginBody body = new LoginBody();
        body.setClientId(Token.getClientId(view.getContext()));
        body.setUsername(userName);
        body.setPassword(password);
        view.showLoading();
        Subscription subscribe = XberRetrofit.create(LoginRequest.class).login(body).delay(1000, TimeUnit.MILLISECONDS).flatMap(new Func1<LoginInfoResponse, Observable<CarDetail>>() {
            @Override
            public Observable<CarDetail> call(LoginInfoResponse loginInfoResponse) {
                driver.setPassword(password);
                driver.setNickName(loginInfoResponse.getName());
                driver.setUserName(loginInfoResponse.getUsername());
                Token.processTokenAndSave(view.getContext(), loginInfoResponse.getAuthToken());
                driver.setUserId(loginInfoResponse.getUserId());
                driver.setToken(Token.value);
                return XberRetrofit.create(UserRequest.class).getCarInfoByUserId(loginInfoResponse.getUserId(),
                        Token.value);
            }

        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<CarDetail>() {
            @Override
            public void call(CarDetail carDetail) {
                driver.setCarId(carDetail.getCarId());
                driver.setPlate(carDetail.getPlate());
                driver.setSpec(carDetail.getSpec());
                driver.setAge(carDetail.getUserInfo().getAge());
                driver.setGender(carDetail.getUserInfo().getGender());
                driver.save(view.getContext());
                MiPushClient.setUserAccount(view.getContext(), String.valueOf(driver.getUserId()), null);
                setLoginResult(LoginActivity.CODE_RESULT_SUCCESS);
                view.showLoginSuccess();
                ((Activity) view.getContext()).finish();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                exceptionDispatcher.dispatchException(throwable);
            }
        });

        subscriptions.add(subscribe);
    }

    @Override
    public void onLoginError(LoginException exception) {
        String message = exception.getMessage();
        view.showLoginError(message);
    }
}
