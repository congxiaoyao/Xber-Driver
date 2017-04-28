package com.congxiaoyao.xber_driver.driverdetail;

import com.congxiaoyao.httplib.response.TaskRsp;
import com.congxiaoyao.xber_driver.mvpbase.presenter.PagedListLoadablePresenter;
import com.congxiaoyao.xber_driver.mvpbase.view.ListLoadableView;

/**
 * Created by guo on 2017/3/29.
 */

public interface HistoryTaskContract {

    interface View extends ListLoadableView<Presenter,TaskRsp> {

        void addExecutingTask(final TaskRsp taskRsp);

        Long getDriverId();

        void clearHeader();
    }

    interface Presenter extends PagedListLoadablePresenter {
    }
}
