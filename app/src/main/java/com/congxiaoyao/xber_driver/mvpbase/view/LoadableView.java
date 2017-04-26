package com.congxiaoyao.xber_driver.mvpbase.view;


import com.congxiaoyao.xber_driver.mvpbase.presenter.BasePresenter;

/**
 * Created by congxiaoyao on 2016/8/25.
 */
public interface LoadableView<T extends BasePresenter> extends BaseView<T> {

    void showLoading();

    void hideLoading();
}
