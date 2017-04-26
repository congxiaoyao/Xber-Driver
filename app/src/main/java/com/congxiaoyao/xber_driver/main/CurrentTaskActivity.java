package com.congxiaoyao.xber_driver.main;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.transition.TransitionManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.congxiaoyao.httplib.request.UserRequest;
import com.congxiaoyao.httplib.request.body.User;
import com.congxiaoyao.httplib.request.gson.GsonHelper;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.TaskRsp;
import com.congxiaoyao.xber_driver.R;
import com.congxiaoyao.xber_driver.TAG;
import com.congxiaoyao.xber_driver.databinding.ActivityCurrentTaskBinding;
import com.congxiaoyao.xber_driver.utils.RxUtils;
import com.congxiaoyao.xber_driver.utils.Token;

import java.text.SimpleDateFormat;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class CurrentTaskActivity extends AppCompatActivity {

    private SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH点mm分");
    public ActivityCurrentTaskBinding binding;
    private TaskRsp taskRsp;
    private Subscription subscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_current_task);
        String json = getIntent().getStringExtra(TravelFragment.KEY_TASK_RSP);

        if (json == null) {
            setSupportActionBar(binding.toolbar);
            getSupportActionBar().setTitle("发生了一些错误");
            binding.llContainer.removeAllViews();
            View view = getLayoutInflater().inflate(R.layout.view_empty,
                    binding.llContainer, true);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)
                    view.getLayoutParams();
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            view.requestLayout();
            return;

        }
        taskRsp = GsonHelper.getInstance().fromJson(json, TaskRsp.class);
        binding.setFormat(format);
        binding.setTaskRsp(taskRsp);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("任务编号" + taskRsp.getTaskId());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        requestHeader();
    }

    private void requestHeader() {
        subscribe = XberRetrofit.create(UserRequest.class).getUserDetail(taskRsp.getCreateUser(), Token.value)
                .compose(RxUtils.<User>delayWhenTimeEnough(250))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        TransitionManager.beginDelayedTransition((ViewGroup) binding.getRoot());
                        binding.setUser(user);
                        binding.senderContainer.setVisibility(View.VISIBLE);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG.ME, "call: ", throwable);
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscribe != null) {
            subscribe.unsubscribe();
        }
        if (binding != null) {
            binding.unbind();
        }
    }
}
