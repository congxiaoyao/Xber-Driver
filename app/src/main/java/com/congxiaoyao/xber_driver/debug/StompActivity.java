package com.congxiaoyao.xber_driver.debug;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.congxiaoyao.httplib.NetWorkConfig;
import com.congxiaoyao.stopmlib.LifecycleEvent;
import com.congxiaoyao.stopmlib.Stomp;
import com.congxiaoyao.stopmlib.client.StompClient;
import com.congxiaoyao.xber_driver.TAG;

import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by congxiaoyao on 2017/2/12.
 */
public abstract class StompActivity extends AppCompatActivity {

    protected StompClient stompClient;

    protected static final String CLIENT_ID = "20136213";
    protected static final String TOKEN = "Basic " +
            CLIENT_ID + ":29497794aa9cebfe194d2622582de5d5";
    private static final String WS_URL = NetWorkConfig.WS_URL;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void initStompClient() {
        initStompClient(WS_URL);
    }

    public void initStompClient(String wsUrl) {
        Map<String, String> header = new HashMap<>();
        header.put(getHeaderKey(), getHeaderValue());
        stompClient = Stomp.over(WebSocket.class, wsUrl, header);
        stompClient.lifecycle()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LifecycleEvent>() {
                    @Override
                    public void call(LifecycleEvent lifecycleEvent) {
                        String hint = "";
                        switch (lifecycleEvent.getType()) {
                            case OPENED:
                                hint = "OPENED";
                                getSupportActionBar().setTitle("已连接");
                                onConnected();
                                break;
                            case CLOSED:
                                hint = "CLOSED";
                                getSupportActionBar().setTitle("未连接");
                                break;
                            case ERROR:
                                getSupportActionBar().setTitle("错误");
                                hint = "ERROR";
                                break;
                        }
                        Log.d(com.congxiaoyao.xber_driver.TAG.ME, "connect:" + hint);
                    }
                });
        stompClient.connect();
        getSupportActionBar().setTitle("连接中");
    }

    protected String getHeaderKey(){
        return "Authorization";
    }

    protected String getHeaderValue(){
        return TOKEN;
    }

    protected void onConnected() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stompClient != null) {
            stompClient.disconnect();
            stompClient = null;
        }
    }
}
