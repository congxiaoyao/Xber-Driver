package com.congxiaoyao.xber_driver.debug;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;

import com.congxiaoyao.stopmlib.client.StompMessage;
import com.congxiaoyao.xber_driver.R;
import com.congxiaoyao.xber_driver.databinding.ActivityListenTaskBinding;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class ListenTaskActivity extends StompActivity {

    ActivityListenTaskBinding binding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_listen_task);
        setSupportActionBar(binding.toolbar);
        initStompClient();
    }

    @Override
    protected void onConnected() {
        binding.contentTextView.append("连接成功\n");
        stompClient.topic("/user/1/system")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<StompMessage>() {
                    @Override
                    public void onCompleted() {
                        binding.contentTextView.append("开始监听\n");
                    }

                    @Override
                    public void onError(Throwable e) {
                        binding.contentTextView.append(e.getMessage());
                    }

                    @Override
                    public void onNext(StompMessage message) {
                        Log.d(com.congxiaoyao.xber_driver.TAG.ME, "onNext: message = "+message.getPayload());
                        binding.contentTextView.append(message.getPayload());
                        binding.contentTextView.append("\n");
                    }
                });
    }
}
