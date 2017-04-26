package com.congxiaoyao.location.utils;

/**
 * Created by congxiaoyao on 2017/3/2.
 */

public class GpsUtils {

    private static final VectorD zero = new VectorD(0, 0);
    private static Ray protractor = new Ray(zero, zero);

    /**
     * 计算两点之间距离
     *
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     *
     * @return mi
     */
    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        lat1 = (Math.PI / 180) * lat1;
        lat2 = (Math.PI / 180) * lat2;

        lng1 = (Math.PI / 180) * lng1;
        lng2 = (Math.PI / 180) * lng2;

        double R = 6371;

        double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.cos(lng2 - lng1)) * R;
        return d * 1000;
    }

    /**
     * 获取速度方向角 弧度制
     *
     * @param lng
     * @param lat
     * @return
     */
    public static double getSpeedAngle(double lng, double lat) {
        protractor.setP1(lng, lat);
        return protractor.getRayAngle();
    }
}
