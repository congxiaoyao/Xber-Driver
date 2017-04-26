package com.congxiaoyao.xber_driver.login;

import com.congxiaoyao.xber_driver.Driver;
import com.congxiaoyao.xber_driver.mvpbase.presenter.BasePresenter;
import com.congxiaoyao.xber_driver.mvpbase.view.LoadableView;

/**
 * Created by congxiaoyao on 2017/3/15.
 */

public interface LoginContract {

    interface View extends LoadableView<Presenter> {

        void showLoginError(String msg);

        void showLoginSuccess();
    }

    interface Presenter extends BasePresenter {

        Driver getDriver();

        void login(String userName, String password);

        void setLoginResult(int tag);
    }
}
