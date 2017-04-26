package com.congxiaoyao.xber_driver.debug;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.congxiaoyao.location.model.NearestNCarsQueryMessage;
import com.congxiaoyao.stopmlib.client.StompMessage;
import com.congxiaoyao.xber_driver.R;
import com.congxiaoyao.xber_driver.TAG;
import com.congxiaoyao.xber_driver.databinding.ActivityQueryBinding;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class QueryActivity extends StompActivity {

    private NearestNCarsQueryMessage queryMessage;
    private ActivityQueryBinding binding;
    private static final String SEND_TO = "/app/nearestNCars";
    private static final String RECEIVE_FROM = "/user/{userId}/nearestNCars";

    private Gson gson;
    private Set<Long> subcribedUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_query);
        this.binding.setPresenter(new Presenter());
        queryMessage = new NearestNCarsQueryMessage();
        initStompClient();
        gson = new Gson();
        subcribedUserId = new TreeSet<>();
    }

    public class Presenter {

        public void onClick(View view) {
            queryMessage.setLatitude(Double.parseDouble(binding.etLat.getText().toString()));
            queryMessage.setLongitude(Double.parseDouble(binding.etLng.getText().toString()));
            queryMessage.setUserId(Long.parseLong(binding.etUserId.getText().toString()));
            queryMessage.setRadius(Double.parseDouble(binding.etRadius.getText().toString()));
            queryMessage.setQueryId(Long.parseLong(binding.etQueryId.getText().toString()));
            queryMessage.setNumber(10);
            sendRequest();
        }
    }

    public void sendRequest() {
        if (stompClient == null || !stompClient.isConnected()) return;
        if (!subcribedUserId.contains(queryMessage.getUserId())) {
            subcribedUserId.add(queryMessage.getUserId());
            String path = RECEIVE_FROM.replace("{userId}", queryMessage.getUserId().toString());
            stompClient.topic(path)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<StompMessage>() {
                        @Override
                        public void onCompleted() {
                            Log.d(com.congxiaoyao.xber_driver.TAG.ME, "onCompleted: ");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG.ME, "onError: ", e);
                        }

                        @Override
                        public void onNext(StompMessage stompMessage) {
                            Log.d(TAG.ME, "onNext: before analyze");
                            analyzeStompMessage(stompMessage);
                        }
                    });
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stompClient.send(SEND_TO, gson.toJson(queryMessage)).subscribe();
                }
            }, 1000);
            return;
        }
        stompClient.send(SEND_TO, gson.toJson(queryMessage)).subscribe();
    }

    public void analyzeStompMessage(StompMessage message) {
        if (message.isSubscribeCallback()) {
            Log.d(TAG.ME, "analyzeStompMessage: this is a subscribe callback");
        }
        if (message.isByteMessage()) {
            Log.d(TAG.ME, "analyzeStompMessage: " + Arrays.toString(message.getBytePayload()));
        }else {
            Log.d(TAG.ME, "analyzeStompMessage: " + message.getPayload());
        }

//        String[] jbs = message.getPayload().split("#");
//        for (String str : jbs) {
//            byte[] decode = GPSEncoding.decode(str.getBytes());
//            try {
//                GpsSampleRsp gpsSample = GpsSampleRsp.parseFrom(decode);
//                Log.d(TAG.ME, "analyzeStompMessage: gpsSample = " + gpsSample);
//            } catch (InvalidProtocolBufferException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
