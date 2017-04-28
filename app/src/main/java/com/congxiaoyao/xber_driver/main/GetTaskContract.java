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

        void showTask(TaskRsp taskRsp);

        void showReload();

        void showNoTask();

        void clearViews();
    }


    interface Presenter extends BasePresenter {

    }

}

