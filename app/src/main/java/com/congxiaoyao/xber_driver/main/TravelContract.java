package com.congxiaoyao.xber_driver.main;

import com.congxiaoyao.httplib.response.TaskRsp;
import com.congxiaoyao.xber_driver.mvpbase.presenter.BasePresenter;
import com.congxiaoyao.xber_driver.mvpbase.view.LoadableView;

/**
 * Created by congxiaoyao on 2017/4/29.
 */

public interface TravelContract {

    interface View extends LoadableView<Presenter> {

        void showFinishWarning();

        void showFinishButton();

    }

    interface Presenter extends BasePresenter{

        void finishTask(TaskRsp taskRsp, boolean ignoreWarning);

        void startTask(TaskRsp taskRsp);

        void startService(TaskRsp taskRsp);
    }
}
