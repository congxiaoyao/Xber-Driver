package com.congxiaoyao.xber_driver.mvpbase.presenter;

import com.congxiaoyao.httplib.response.Page;

import java.util.Date;

/**
 * Created by congxiaoyao on 2016/8/26.
 */
public interface PagedListLoadablePresenter extends BasePresenter {

    void savePage(Date latestDate, Page page);

    boolean hasMoreData();

    void loadMoreData();

    void refreshData();
}
