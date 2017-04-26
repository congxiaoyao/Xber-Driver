package com.congxiaoyao.xber_driver.main;

import com.congxiaoyao.httplib.response.TaskRsp;
import com.congxiaoyao.xber_driver.Driver;
import com.congxiaoyao.xber_driver.mvpbase.presenter.BasePresenter;
import com.congxiaoyao.xber_driver.mvpbase.view.LoadableView;
import com.congxiaoyao.xber_driver.widget.LoadingLayout;

/**
 * Created by congxiaoyao on 2017/4/27.
 */

public interface GetTaskContract {

    interface View extends LoadableView<Presenter> {

        Driver getDriver();

        LoadingLayout getLoadingLayout();

        void showTask(TaskRsp taskRsp);
    }


    interface Presenter extends BasePresenter {

    }

}

