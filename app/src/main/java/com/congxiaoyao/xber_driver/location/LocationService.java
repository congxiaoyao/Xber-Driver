package com.congxiaoyao.xber_driver.location;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.congxiaoyao.httplib.NetWorkConfig;
import com.congxiaoyao.location.utils.GPSEncoding;
import com.congxiaoyao.stopmlib.LifecycleEvent;
import com.congxiaoyao.stopmlib.Stomp;
import com.congxiaoyao.stopmlib.client.StompClient;
import com.congxiaoyao.xber_driver.Driver;
import com.congxiaoyao.xber_driver.R;
import com.congxiaoyao.xber_driver.TAG;
import com.congxiaoyao.xber_driver.main.MainActivity;
import com.congxiaoyao.xber_driver.utils.Token;
import com.congxiaoyao.xber_driver.utils.WakeLockHelper;

import org.java_websocket.WebSocket;
import org.litepal.crud.DataSupport;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.congxiaoyao.location.model.GpsSampleOuterClass.GpsSample;

/**
 * Created by congxiaoyao on 2017/4/27.
 */

public class LocationService extends Service implements LocationTrace.Callback{

    public static final int NOTIFICATION_ID = 100;
    private StompClient client;

    public static final String TASK_ID = "TASK_ID";

    public static final String COMMAND_KEY = "COMMAND_KEY";
    public static final int COMMAND_LOCATION = 0;
    public static final int COMMAND_UPLOAD = 1;

    public static final String ACTION_LOCATION = "ACTION_LOCATION";
    public static final String ACTION_ERROR = "ACTION_ERROR";

    public static final String EXTRA_LOCATION = "EXTRA_LOCATION";

    private LocationClient locationClient;

    private LocationTrace locationTrace;

    private long taskId;
    private long carId;

    public static void startServiceForLocation(Context context) {
        Intent intent = new Intent(context, LocationService.class);
        intent.putExtra(COMMAND_KEY, COMMAND_LOCATION);
        context.startService(intent);
    }

    public static void startServiceForUpload(Context context, long taskId) {
        Intent intent = new Intent(context, LocationService.class);
        intent.putExtra(COMMAND_KEY, COMMAND_UPLOAD);
        intent.putExtra(TASK_ID, taskId);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupForeground();
        WakeLockHelper.acquireCpuWakeLock(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        int command = intent.getIntExtra(COMMAND_KEY, -1);
        if (command == (COMMAND_LOCATION)) {
            setupLocationClient();
        } else if (command == (COMMAND_UPLOAD)) {
            taskId = intent.getLongExtra(TASK_ID, -1);
            carId = Driver.fromSharedPreference(LocationService.this).getCarId();
            if (taskId != -1) setupUploadClient();
        }
        return START_STICKY;
    }

    private void setupForeground() {
        Notification.Builder builder = new Notification.Builder
                (this.getApplicationContext());
        Intent nfIntent = new Intent(this, MainActivity.class);

        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                .setContentTitle("Xber司机")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("正在获取您的位置")
                .setWhen(System.currentTimeMillis());

        startForeground(NOTIFICATION_ID, builder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setupLocationClient() {
        locationClient = new LocationClient(getApplicationContext());
        initLocation(locationClient);
        locationClient.registerLocationListener(new MyLocationListener());
        locationClient.start();
    }

    private void initLocation(LocationClient locationClient) {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(1000);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps
        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        locationClient.setLocOption(option);
    }

    private void setupUploadClient() {
        locationTrace = new LocationTrace(this);

        Map<String, String> header = new HashMap<>();
        header.put(NetWorkConfig.AUTH_KEY, Token.value);
        Action1<LifecycleEvent> lifecycleEvent = new Action1<LifecycleEvent>() {
            @Override
            public void call(LifecycleEvent lifecycleEvent) {
                switch (lifecycleEvent.getType()) {
                    case OPENED:
                        Log.d(TAG.ME, "WebSocket opened!");
                        break;
                    case CLOSED:
                        sendError();
                    case ERROR:
                        sendError();
                        break;
                }
            }
        };
        client = Stomp.over(WebSocket.class, NetWorkConfig.WS_URL, header);
        client.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent, new DefaultRxErrorHandler("lifecycleEvent"));
        client.onConnected().subscribeOn(Schedulers.io()).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                sendSavedData();
            }
        }, new DefaultRxErrorHandler("onConnected"));
        client.connect();
    }

    private void sendDataOrSave(BDLocation location) {
        //如果网络可用
        if (client != null && client.isConnected()) {
            //分析数据，决定是否要上传
            locationTrace.analyze(location);
        }
        //暂时保存 等下次再传
        else {
            Position position = gpsSampleToPosition(GpsSample.newBuilder().setTaskId(taskId)
                    .setTime(System.currentTimeMillis())
                    .setCarId(carId)
                    .setLat(location.getLatitude())
                    .setLng(location.getLongitude())
                    .setVlat(0)
                    .setVlng(0)
                    .build());
            position.saveFast();
        }
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //发广播
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(LocationService.this);
            Intent intent = new Intent(ACTION_LOCATION);
            intent.putExtra(EXTRA_LOCATION, location);
            manager.sendBroadcast(intent);

            //更新通知栏
            updateNotification(location);

            //上传或缓存数据
            sendDataOrSave(location);
        }

        private void updateNotification(BDLocation location) {
            String content = "网络定位";
            if (location.getLocType() == BDLocation.TypeGpsLocation){
                content = "GPS定位";
            }
            String time = location.getTime();
            content += (" " + time + " " + location.getDirection());
            Notification.Builder builder = new Notification.Builder
                    (LocationService.this.getApplicationContext());
            Intent nfIntent = new Intent(LocationService.this, MainActivity.class);
            builder.setContentIntent(PendingIntent.getActivity(LocationService.this, 0, nfIntent, 0))
                    .setLargeIcon(BitmapFactory.decodeResource(LocationService.this.getResources(), R.mipmap.ic_launcher))
                    .setContentTitle("Xber司机")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText(content)
                    .setWhen(System.currentTimeMillis());
            NotificationManagerCompat.from(LocationService.this).notify(100, builder.build());
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }

    }

    /**
     * 当LocationTrace认为需要上传位置的时候的回调
     *
     * @param location
     */
    @Override
    public void onDislocated(BDLocation location) {
        GpsSample gpsSample = BDLocationToGpsSample(location);
        client.send(NetWorkConfig.UPLOAD_PATH,
                GPSEncoding.encode(gpsSample.toByteArray())).subscribe();
    }

    private void sendSavedData() {
        DataSupport.deleteAll(Position.class, "handled = ?", "1");
        List<Position> positions = DataSupport.where("handled = ?", "0").find(Position.class);
        Iterator<Position> iterator = positions.iterator();
        while (iterator.hasNext()) {
            Position position = iterator.next();
            GpsSample gpsSample = positionToGpsSimple(position);
            client.send(NetWorkConfig.UPLOAD_PATH,
                    GPSEncoding.encode(gpsSample.toByteArray())).subscribe();
        }
    }

    private void sendError() {
        LocalBroadcastManager.getInstance(LocationService.this)
                .sendBroadcast(new Intent(ACTION_ERROR));
    }

    private GpsSample BDLocationToGpsSample(BDLocation location) {
        return GpsSample.newBuilder().setCarId(carId)
                .setLat(location.getLatitude())
                .setLng(location.getLongitude())
                .setVlat(locationTrace.getVlat(location))
                .setVlng(locationTrace.getVlng(location))
                .setTime(locationTrace.getTime(location))
                .setTaskId(taskId)
                .build();
    }

    private GpsSample positionToGpsSimple(Position position) {
        return GpsSample.newBuilder().setCarId(position.getCarId())
                .setLat(position.getLat())
                .setLng(position.getLng())
                .setVlat(position.getvLat())
                .setVlng(position.getvLng())
                .setTime(position.getTime())
                .setTaskId(position.getTaskId())
                .build();
    }

    private Position gpsSampleToPosition(GpsSample gpsSample) {
        Position position = new Position();
        position.setCarId(gpsSample.getCarId());
        position.setLat(gpsSample.getLat());
        position.setLng(gpsSample.getLng());
        position.setvLat(gpsSample.getVlat());
        position.setvLng(gpsSample.getVlng());
        position.setTime(gpsSample.getTime());
        position.setTaskId(gpsSample.getTaskId());
        return position;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        if (locationClient != null) {
            locationClient.stop();
            locationClient = null;
        }
        super.onDestroy();
        WakeLockHelper.releaseCpuLock();
    }
    
    public class DefaultRxErrorHandler implements Action1<Throwable> {

        private String title;

        public DefaultRxErrorHandler(String title) {
            this.title = title;
        }

        @Override
        public void call(Throwable throwable) {
            Log.e(TAG.ME, title + ": ", throwable);
            sendError();
        }
    }
}
