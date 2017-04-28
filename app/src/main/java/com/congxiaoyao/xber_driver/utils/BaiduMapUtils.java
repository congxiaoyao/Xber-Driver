package com.congxiaoyao.xber_driver.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.congxiaoyao.location.utils.Line;

/**
 * Created by congxiaoyao on 2017/3/24.
 */

public class BaiduMapUtils {

    private static Point screenCenter = null;
    private static Point screenSize = null;

    private static final Point ZERO = new Point();
    private static final Point temp = new Point();

    public static LatLng getScreenCenterLatLng(Context context, BaiduMap baiduMap) {
        if (screenCenter == null) {
            screenCenter = new Point();
            ((Activity) context).getWindowManager().getDefaultDisplay().getSize(screenCenter);
            screenCenter.x = screenCenter.x / 2;
            screenCenter.y = screenCenter.y / 2;
        }
        return baiduMap.getProjection().fromScreenLocation(screenCenter);
    }

    public static double getScreenRadius(Context context, BaiduMap baiduMap) {
        if (screenSize == null) {
            screenSize = new Point();
            ((Activity) context).getWindowManager().getDefaultDisplay().getSize(screenSize);
        }
        Projection projection = baiduMap.getProjection();
        LatLng rb = projection.fromScreenLocation(ZERO);
        LatLng lt = projection.fromScreenLocation(screenSize);
        Line line = new Line(lt.longitude, lt.latitude, rb.longitude, rb.latitude);
        return line.getLength() / 2;
    }

    public static void moveToBounds(BaiduMap map, LatLngBounds bounds, int width, int height) {
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLngBounds(bounds,
                width, height);
        map.setMapStatus(update);
    }

    public static void moveToLatLng(BaiduMap map, double lat, double lng, int zoom, boolean animate) {
        if (map == null) return;
        if (animate) {
            moveToLatLng(map, lat, lng, zoom);
            return;
        }
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(new LatLng(lat, lng))
                .zoom(zoom).build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        map.setMapStatus(mMapStatusUpdate);
    }

    public static void moveToLatLng(BaiduMap map, double lat, double lng, int zoom) {
        if (map == null) return;
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(new LatLng(lat, lng))
                .zoom(zoom)
                .build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        map.animateMapStatus(mMapStatusUpdate);
    }

    public static void moveToLatLng(BaiduMap map, double lat, double lng) {
        moveToLatLng(map, lat, lng, 16);
    }

    public static void moveToLatLngWithoutZoom(BaiduMap map, double lat, double lng) {
        if (map == null) return;
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(new LatLng(lat, lng))
                .build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        map.animateMapStatus(mMapStatusUpdate);
    }

    public static void moveToPoint(BaiduMap map, Point point) {
        MapStatus mMapStatus = new MapStatus.Builder()
                .targetScreen(point).build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        map.animateMapStatus(mMapStatusUpdate);
    }

    public static void moveToPoint(BaiduMap map, int x, int y) {
        temp.x = x;
        temp.y = y;
        moveToPoint(map, temp);
    }

    public static void zoom(BaiduMap map, float level) {
        MapStatus mMapStatus = new MapStatus.Builder()
                .zoom(level).build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        map.animateMapStatus(mMapStatusUpdate);
    }

    public static void zoomToDefault(BaiduMap map) {
        zoom(map, 16);
    }
}
