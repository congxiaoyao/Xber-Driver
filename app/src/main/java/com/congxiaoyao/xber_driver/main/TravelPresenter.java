package com.congxiaoyao.xber_driver.main;

import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.congxiaoyao.httplib.request.TaskRequest;
import com.congxiaoyao.httplib.request.body.StatusChangeRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.Task;
import com.congxiaoyao.httplib.response.TaskRsp;
import com.congxiaoyao.location.utils.GpsUtils;
import com.congxiaoyao.xber_driver.location.LocationService;
import com.congxiaoyao.xber_driver.mvpbase.presenter.BasePresenterImpl;
import com.congxiaoyao.xber_driver.utils.Token;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by congxiaoyao on 2017/4/29.
 */

public class TravelPresenter extends BasePresenterImpl<TravelContract.View>
        implements TravelContract.Presenter, Action1<Throwable> {

    private static final int MIN_DISTANCE = 10000;  //米
    private TaskRsp taskRsp;

    public TravelPresenter(TravelContract.View view) {
        super(view);
    }

    @Override
    public void finishTask(TaskRsp taskRsp, boolean ignoreWarning) {
        this.taskRsp = taskRsp;
        if (taskRsp == null || !Integer.valueOf(Task.STATUS_EXECUTING).equals(taskRsp.getStatus())) {
            exceptionDispatcher.dispatchException(new RuntimeException("任务提交失败"));
            return;
        }
        if (!ignoreWarning) {
            BDLocation location = ((MainActivity) view.getContext()).getLastLocation();
            if (location == null || GpsUtils.getDistance(location.getLatitude(),
                    location.getLongitude(), taskRsp.getEndSpot().getLatitude(),
                    taskRsp.getEndSpot().getLongitude()) > MIN_DISTANCE) {
                view.showFinishWarning();
                return;
            }
        }
        subscribe();
    }

    @Override
    public void startTask(TaskRsp taskRsp) {
        this.taskRsp = taskRsp;
        if (taskRsp == null || !Integer.valueOf(Task.STATUS_DELIVERED).equals(taskRsp.getStatus())) {
            exceptionDispatcher.dispatchException(new RuntimeException("无法开始任务"));
            return;
        }
        subscribe();
    }

    @Override
    public void subscribe() {
        if (taskRsp == null) {
            exceptionDispatcher.dispatchException(new RuntimeException());
            return;
        }
        view.showLoading();
        StatusChangeRequest request = new StatusChangeRequest();
        request.setTaskId(taskRsp.getTaskId());
        request.setStatus(taskRsp.getStatus() + 1);
        Subscription subscribe = XberRetrofit.create(TaskRequest.class).changeTaskStatus(request, Token.value)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        view.hideLoading();
                        Toast.makeText(view.getContext(), s, Toast.LENGTH_SHORT).show();
                        if (taskRsp.getStatus() == Task.STATUS_DELIVERED) {
                            startService(taskRsp);
                            taskRsp.setStatus(Task.STATUS_EXECUTING);
                        } else {
                            LocationService.stopUploadService(view.getContext());
                            ((MainActivity) view.getContext()).reRequestTask();
                        }
                    }
                }, this);
        subscriptions.add(subscribe);
    }

    @Override
    public void startService(TaskRsp taskRsp) {
        view.showFinishButton();
        LocationService.startServiceForUpload(view.getContext(), taskRsp.getTaskId());
    }

    @Override
    public void call(Throwable throwable) {
        exceptionDispatcher.dispatchException(throwable);
    }
}
