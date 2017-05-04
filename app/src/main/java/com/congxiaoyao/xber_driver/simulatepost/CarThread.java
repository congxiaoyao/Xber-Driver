package com.congxiaoyao.xber_driver.simulatepost;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.congxiaoyao.httplib.NetWorkConfig;
import com.congxiaoyao.stopmlib.client.StompClient;
import com.congxiaoyao.location.utils.GPSEncoding;
import com.congxiaoyao.location.utils.GpsUtils;
import com.congxiaoyao.xber_driver.TAG;
import com.congxiaoyao.xber_driver.main.MainActivity;

import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.congxiaoyao.location.model.GpsSampleOuterClass.GpsSample;
import static com.congxiaoyao.xber_driver.simulatepost.SimulatePostActivity.LocBean;
import static com.congxiaoyao.xber_driver.simulatepost.SimulatePostActivity.latlngFileNames;

/**
 * 模拟一辆车行进 其中轨迹点集由构造函数传入 此线程将持续不断按进度模拟点的上传过程
 * 直至全部点上传完毕
 *
 * Created by congxiaoyao on 2017/3/1.
 */

public class CarThread extends HandlerThread {

    public static final int UPDATING = 0;
    public static final int FINISHED = 1;

    private final Long carId;
    private Long endSpotId;
    private Long taskId;
    private List<LocBean> data;
    private StompClient client;
    private Handler innerHandler;
    private float progress = 0;

    private int uploadCount = 0;

    private ProgressListener progressListener;

    private Handler observer = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (progressListener == null) return;
            switch (msg.what) {
                case UPDATING:
                    progress = msg.arg1 / (float) data.size();
                    progressListener.onUpload(progress);
                    break;
                case FINISHED:
                    progressListener.onFinished();
                    break;
            }
        }
    };

    public CarThread(Long carId, Long taskId, Long endSpotId, StompClient client,
                     Context context) {
        super("car" + carId + "thread");
        if (client == null) throw new NullPointerException();
        this.carId = carId;
        this.taskId = taskId;
        this.endSpotId = endSpotId;
        this.client = client;

        List<UploadState> states = DataSupport
                .where("carid = ?", String.valueOf(carId))
                .find(UploadState.class);
        if (states != null && states.size() != 0) {
            UploadState state = states.get(0);
            this.uploadCount = state.getUpdated();
            this.taskId = state.getTaskId();
            this.endSpotId = state.getEndSpotId();
        } else {
            if (this.taskId == null) throw new NullPointerException();
            if (this.carId == null) throw new NullPointerException();
            if(this.endSpotId == null) throw new NullPointerException();
            UploadState state = new UploadState();
            state.setCarId(carId);
            state.setUpdated(0);
            state.setTaskId(taskId);
            state.setEndSpotId(endSpotId);
            state.save();
        }

        initDataBySpotId(context);
    }

    @Override
    public synchronized void start() {
        super.start();
        innerHandler = new Handler(getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                CarThread.this.handleMessage(msg);
            }
        };
        if (data == null || data.size() == 0) {
            quit();
            return;
        }
        innerHandler.sendEmptyMessage(0);
    }

    public void handleMessage(Message msg) {
        if (uploadCount == data.size()) {
            quit();
            observer.sendEmptyMessage(FINISHED);
            return;
        }
        LocBean nowLocBean = data.get(uploadCount);
        LocBean nextLocBean = null;
        if (uploadCount == data.size() - 1) {
            nextLocBean = new LocBean();
            nextLocBean.lat = nowLocBean.lat;
            nextLocBean.lng = nowLocBean.lng;
        } else {
            nextLocBean = data.get(uploadCount + 1);
        }
        int delay = sendLocBeanToServer(nowLocBean, nextLocBean);
        uploadCount++;
        recodeUpdateCount();
        Message message = Message.obtain();
        message.what = UPDATING;
        message.arg1 = uploadCount;
        observer.sendMessage(message);
        innerHandler.sendEmptyMessageDelayed(0, delay);
    }

    private int sendLocBeanToServer(LocBean now, LocBean next) {
        double distance = GpsUtils.getDistance(now.lat, now.lng, next.lat, next.lng);
        double speed;
        double time;
        double vLat;
        double vLng;
        if (distance == 0) {
            time = 0;
            vLat = 0;
            vLng = 0;
        } else {
            speed = Math.random() * 6 + 27;
            time = distance / speed;
            Log.d(TAG.ME, "sendLocBeanToServer: time =" + time);
            vLat = (next.lat - now.lat) / time;
            vLng = (next.lng - now.lng) / time;
        }
        GpsSample gpsSample = GpsSample.newBuilder().setTaskId(taskId)
                .setTime(System.currentTimeMillis())
                .setCarId(carId)
                .setLat(now.lat)
                .setLng(now.lng)
                .setVlat(vLat)
                .setVlng(vLng).build();
        byte[] encode = GPSEncoding.encode(gpsSample.toByteArray());
        while (!client.isConnected()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        synchronized (client) {
            client.send(NetWorkConfig.UPLOAD_PATH, encode).subscribe();
        }
        return (int) (time * 1000);
    }

    private void recodeUpdateCount() {
        ContentValues values = new ContentValues();
        values.put("updated", uploadCount);
        DataSupport.updateAll(UploadState.class, values, "carid = ?", String.valueOf(carId));
    }

    private void initDataBySpotId(Context context) {
        AssetManager manager = context.getAssets();
        List<LocBean> data = null;
        try {
            String fileName = latlngFileNames.get(endSpotId);
            InputStream open = manager.open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(open));
            int size = Integer.parseInt(reader.readLine());
            data = new ArrayList<>(size);
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                LocBean bean = new LocBean();
                bean.lat = Double.parseDouble(split[0]);
                bean.lng = Double.parseDouble(split[1]);
                data.add(bean);
            }
            reader.close();
            open.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.data = data;
    }

    public Long getCarId() {
        return carId;
    }

    public float getProgress() {
        return progress;
    }

    public Long getEndSpotId() {
        return endSpotId;
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public interface ProgressListener {

        void onUpload(float progress);

        void onFinished();
    }

}
