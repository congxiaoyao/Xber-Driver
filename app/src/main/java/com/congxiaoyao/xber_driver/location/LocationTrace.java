package com.congxiaoyao.xber_driver.location;

/**
 * Created by congxiaoyao on 2017/2/2.
 */

import android.util.Log;

import com.baidu.location.BDLocation;
import com.congxiaoyao.location.utils.GpsUtils;
import com.congxiaoyao.xber_driver.TAG;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class LocationTrace {

    private static final int MIN_DISTANCE = 10;     //米
    private static final int MIN_TIME_MILL = 10 * 1000; //毫秒

    private BDLocation last;
    private Callback callback;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);


    public LocationTrace(Callback callback) {
        this.callback = callback;
    }

    public void analyze(BDLocation location) {
        if (last == null) {
            last = location;
            callback.onDislocated(location);
            return;
        }
        analyze0(location);
    }

    /**
     * 在存在上一个点的情况下 分析这一点需要做什么处理
     * @param location
     */
    private void analyze0(BDLocation location) {
        //十秒钟必须上传
        if (getTime(location) - getTime(last) > MIN_TIME_MILL) {
            callback.onDislocated(location);
            last = location;
            return;
        }
        //速度过慢 不分析 直接等必须传的时候直接上传
        if (location.getSpeed() <= 2) {
            Log.e(TAG.ME, "analyze0: 速度过慢");
            return;
        }
        //预测位置 如果在推断的范围内则不上传
        BDLocation to = calculateNewBDLocation(last, last.getDirection(),
                last.getSpeed(), (getTime(location) - getTime(last)) / 1000.0f);
        double distance = GpsUtils.getDistance(location.getLatitude(), location.getLongitude()
                , location.getLatitude(), location.getLongitude());
        Log.d(TAG.ME, "analyze0: distance = " + distance + " degree = " +
                (last.getDirection() - location.getDirection()) + "dv = " +
                (location.getSpeed() - last.getSpeed()));

        if (distance < MIN_DISTANCE) {
            Log.d(TAG.ME, "analyze0: 通过检查");
        } else {
            callback.onDislocated(location);
            last = location;
        }
    }

    /**
     * @param degree 360角度制
     * @param speed  米每秒
     * @param time   秒
     * @return
     */
    public BDLocation calculateNewBDLocation(BDLocation location, float degree, float speed, float time) {
        BDLocation newLocation = new BDLocation(location);
        float len = speed * time;
        double theta =  ((90.0f - degree) * Math.PI / 180.0f);
        double horLen =  (len * cos(theta));
        double verLen =  (len * sin(theta));
        double baseDis =  (Math.PI * 6371000 / 180);
        double dLat = verLen / baseDis;
        baseDis = (baseDis * cos(location.getLatitude() * Math.PI / 180));
        double dLng = horLen / baseDis;
        Log.d(TAG.ME, "makeNewPosition: delta纬度 = " +
                String.format("%.6f", dLat*1000) + " delta经度 = " +
                String.format("%.6f", dLng*1000));
        newLocation.setLatitude(location.getLatitude() + dLat);
        newLocation.setLongitude(newLocation.getLongitude() + dLng);
        return newLocation;
    }


    public long getTime(BDLocation location) {
        try {
            return format.parse(location.getTime()).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public double getVlat(BDLocation location) {
        float len = location.getSpeed();
        double theta = ((90.0f - location.getDirection()) * Math.PI / 180.0f);
        double verLen =  (len * sin(theta));
        double baseDis =  (Math.PI * 6371000 / 180);
        return verLen / baseDis;
    }

    public double getVlng(BDLocation location) {
        float len = location.getSpeed();
        double theta = ((90.0f - location.getDirection()) * Math.PI / 180.0f);
        double horLen =  (len * cos(theta));
        double baseDis =  (Math.PI * 6371000 / 180);
        baseDis = (baseDis * cos(location.getLatitude() * Math.PI / 180));
        return horLen / baseDis;
    }

    public interface Callback {

        void onDislocated(BDLocation location);
    }
}
