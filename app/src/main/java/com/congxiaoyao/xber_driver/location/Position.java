package com.congxiaoyao.xber_driver.location;

import org.litepal.crud.DataSupport;

/**
 * Created by congxiaoyao on 2017/4/29.
 */

public class Position extends DataSupport {

    private long carId;
    private long taskId;
    private double vLat;
    private double vLng;
    private double lat;
    private double lng;
    private long time;

    private int handled = 0;

    public Position() {
    }

    public long getCarId() {
        return carId;
    }

    public void setCarId(long carId) {
        this.carId = carId;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public double getvLat() {
        return vLat;
    }

    public void setvLat(double vLat) {
        this.vLat = vLat;
    }

    public double getvLng() {
        return vLng;
    }

    public void setvLng(double vLng) {
        this.vLng = vLng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setHandled(int handled) {
        this.handled = handled;
    }

    public int getHandled() {
        return handled;
    }

    @Override
    public String toString() {
        return "Position{" +
                "carId=" + carId +
                ", taskId=" + taskId +
                ", vLat=" + vLat +
                ", vLng=" + vLng +
                ", lat=" + lat +
                ", lng=" + lng +
                ", time=" + time +
                '}';
    }
}
