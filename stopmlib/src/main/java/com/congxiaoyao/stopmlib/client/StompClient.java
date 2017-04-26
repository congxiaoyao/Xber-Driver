package com.congxiaoyao.stopmlib.client;

import android.util.Log;

import com.congxiaoyao.stopmlib.ConnectionProvider;
import com.congxiaoyao.stopmlib.LifecycleEvent;
import com.congxiaoyao.stopmlib.StompHeader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;

/**
 * Created by naik on 05.05.16.
 */
public class StompClient {

    private static final String TAG = StompClient.class.getSimpleName();

    public static final String SUPPORTED_VERSIONS = "1.1,1.0";
    public static final String DEFAULT_ACK = "auto";

    private Subscription mMessagesSubscription;
    private Map<String, Set<Subscriber<? super StompMessage>>> mSubscribers = new HashMap<>();
    private List<ConnectableObservable<Void>> mWaitConnectionObservables;
    private final ConnectionProvider mConnectionProvider;
    private HashMap<String, String> mTopics;
    private boolean mConnected;

    public StompClient(ConnectionProvider connectionProvider) {
        mConnectionProvider = connectionProvider;
        mWaitConnectionObservables = new ArrayList<>();
    }

    /**
     * Connect without reconnect if connected
     */
    public void connect() {
        connect(null);
    }

    public void connect(boolean reconnect) {
        connect(null, reconnect);
    }

    /**
     * Connect without reconnect if connected
     *
     * @param _headers might be null
     */
    public void connect(List<StompHeader> _headers) {
        connect(_headers, false);
    }

    /**
     * If already connected and reconnect=false - nope
     *
     * @param _headers might be null
     */
    public void connect(final List<StompHeader> _headers, boolean reconnect) {
        if (reconnect) disconnect();
        if (mConnected) return;
        mConnectionProvider.getLifecycleReceiver()
                .subscribe(new Action1<LifecycleEvent>() {
                    @Override
                    public void call(LifecycleEvent lifecycleEvent) {
                        switch (lifecycleEvent.getType()) {
                            case OPENED:
                                List<StompHeader> headers = new ArrayList<>();
                                headers.add(new StompHeader(StompHeader.VERSION, SUPPORTED_VERSIONS));
                                if (_headers != null) headers.addAll(_headers);
                                mConnectionProvider.send(new StompMessage(StompCommand.CONNECT, headers, null).compile())
                                        .subscribe();
                                break;

                            case CLOSED:
                                mConnected = false;
                                break;
                        }
                    }
                });

        mMessagesSubscription = mConnectionProvider.messages()
                .map(new Func1<String, StompMessage>() {
                    @Override
                    public StompMessage call(String s) {
                        return StompMessage.from(s);
                    }
                })
                .subscribe(new Action1<StompMessage>() {
                    @Override
                    public void call(StompMessage stompMessage) {
                        if (stompMessage.getStompCommand().equals(StompCommand.CONNECTED)) {
                            mConnected = true;
                            for (ConnectableObservable<Void> observable : mWaitConnectionObservables) {
                                observable.connect();
                            }
                            mWaitConnectionObservables.clear();
                        }
                        callSubscribers(stompMessage);
                    }
                });
    }

    public Observable<Void> send(String destination) {
        return send(new StompMessage(
                StompCommand.SEND,
                Collections.singletonList(new StompHeader(StompHeader.DESTINATION, destination)),
                null));
    }

    public Observable<Void> send(String destination, String data) {
        return send(new StompMessage(
                StompCommand.SEND,
                Collections.singletonList(new StompHeader(StompHeader.DESTINATION, destination)),
                data));
    }

    public Observable<Void> send(StompMessage stompMessage) {
        Observable<Void> observable = mConnectionProvider.send(stompMessage.compile());
        if (!mConnected) {
            ConnectableObservable<Void> deffered = observable.publish();
            mWaitConnectionObservables.add(deffered);
            return deffered;
        } else {
            return observable;
        }
    }

    private void callSubscribers(StompMessage stompMessage) {
        String messageDestination = stompMessage.findHeader(StompHeader.DESTINATION);
        for (String dest : mSubscribers.keySet()) {
            if (dest.equals(messageDestination)) {
                for (Subscriber<? super StompMessage> subscriber : mSubscribers.get(dest)) {
                    subscriber.onNext(stompMessage);
                }
                return;
            }
        }
    }

    public Observable<LifecycleEvent> lifecycle() {
        return mConnectionProvider.getLifecycleReceiver();
    }

    public void disconnect() {
        if (mMessagesSubscription != null) mMessagesSubscription.unsubscribe();
        mConnected = false;
    }

    public Observable<StompMessage> topic(String destinationPath) {
        return topic(destinationPath, null);
    }

    public Observable<StompMessage> topic(final String destinationPath, final List<StompHeader> headerList) {
        return Observable.<StompMessage>create(new Observable.OnSubscribe<StompMessage>() {
            @Override
            public void call(Subscriber<? super StompMessage> subscriber) {
                Set<Subscriber<? super StompMessage>> subscribersSet = mSubscribers.get(destinationPath);
                if (subscribersSet == null) {
                    subscribersSet = new HashSet<>();
                    mSubscribers.put(destinationPath, subscribersSet);
                    subscribePath(destinationPath, headerList);
                }
                subscribersSet.add(subscriber);
            }
        }).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                for (String dest : mSubscribers.keySet()) {
                    Set<Subscriber<? super StompMessage>> set = mSubscribers.get(dest);
                    for (Subscriber<? super StompMessage> subscriber : set) {
                        if (subscriber.isUnsubscribed()) {
                            set.remove(subscriber);
                            if (set.size() < 1) {
                                mSubscribers.remove(dest);
                                unsubscribePath(dest);
                            }
                        }
                    }
                }
            }
        });
    }

    private void subscribePath(String destinationPath, List<StompHeader> headerList) {
        if (destinationPath == null) return;
        String topicId = UUID.randomUUID().toString();

        if (mTopics == null) mTopics = new HashMap<>();
        mTopics.put(destinationPath, topicId);
        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader(StompHeader.ID, topicId));
        headers.add(new StompHeader(StompHeader.DESTINATION, destinationPath));
        headers.add(new StompHeader(StompHeader.ACK, DEFAULT_ACK));
        if (headerList != null) headers.addAll(headerList);
        send(new StompMessage(StompCommand.SUBSCRIBE,
                headers, null)).subscribe();
    }


    private void unsubscribePath(String dest) {
        String topicId = mTopics.get(dest);
        Log.d(TAG, "Unsubscribe path: " + dest + " id: " + topicId);

        send(new StompMessage(StompCommand.UNSUBSCRIBE,
                Collections.singletonList(new StompHeader(StompHeader.ID, topicId)), null));
    }

    public boolean isConnected() {
        return mConnected;
    }
}
