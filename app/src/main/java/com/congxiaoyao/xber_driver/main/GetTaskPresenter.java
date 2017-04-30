package com.congxiaoyao.xber_driver.main;

import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Toast;

import com.congxiaoyao.httplib.NetWorkConfig;
import com.congxiaoyao.httplib.request.TaskRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.Task;
import com.congxiaoyao.httplib.response.TaskListRsp;
import com.congxiaoyao.xber_driver.Driver;
import com.congxiaoyao.xber_driver.mvpbase.presenter.BasePresenterImpl;
import com.congxiaoyao.xber_driver.utils.Token;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class GetTaskPresenter extends BasePresenterImpl<GetTaskContract.View>
        implements GetTaskContract.Presenter {

    public GetTaskPresenter(GetTaskContract.View view) {
        super(view);
    }

    public void onClick(View v) {
        Toast.makeText(view.getContext(), "toast", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void subscribe() {
        view.clearViews();
        Observable<TaskListRsp> executing = XberRetrofit.getRetrofit().create(TaskRequest.class)
                .getTask(view.getDriver().getUserId(), 0, 1, Task.STATUS_EXECUTING,
                        null, null, Token.value);
        view.showLoading();
        Subscription subscribe = executing.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<TaskListRsp>() {
                    @Override
                    public void call(TaskListRsp taskListRsp) {
                        int size = taskListRsp.getCurrentPageData().size();
                        if (size == 0) {
                            subscribe2();
                        } else {
                            view.showTask(taskListRsp.getTaskList().get(0));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        exceptionDispatcher.dispatchException(throwable);
                    }
                });
        subscriptions.add(subscribe);
    }

    public void subscribe2() {
        Observable<TaskListRsp> delivered = XberRetrofit.getRetrofit().create(TaskRequest.class)
                .getTask(view.getDriver().getUserId(), 0, 1, Task.STATUS_DELIVERED,
                        null, null, Token.value);
        Subscription subscribe = delivered.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<TaskListRsp>() {
                    @Override
                    public void call(TaskListRsp taskListRsp) {
                        if (taskListRsp.getCurrentPageData().size() == 0) {
                            view.showNoTask();
                            MainActivity context = ((MainActivity) view.getContext());
                            LocalBroadcastManager.getInstance(context).registerReceiver(
                                    context.getNewTaskReceiver(),
                                    new IntentFilter(MainActivity.ACTION_NEW_TASK));
                        } else {
                            view.showTask(taskListRsp.getTaskList().get(0));
                        }
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
    public void onDispatchException(Throwable throwable) {
        super.onDispatchException(throwable);
        view.showReload();
    }
}