/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.congxiaoyao.xber_driver.map;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.congxiaoyao.xber_driver.R;
import com.congxiaoyao.xber_driver.TAG;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class OpenglDemo extends Activity {

	private static final String LTAG = OpenglDemo.class.getSimpleName();
	// 地图相关
	MapView mMapView;
	BaiduMap mBaiduMap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_opengl);
		// 初始化地图
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		List<LatLng> list = null;
		AssetManager assets = getAssets();
		try {
			InputStream open = assets.open("hengshui.latlngs");
			BufferedReader reader = new BufferedReader(new InputStreamReader(open));
			int size = Integer.parseInt(reader.readLine());
			list = new ArrayList<>(size);
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] split = line.split(",");
				LatLng latLng = new LatLng(Double.parseDouble(split[0]),
						Double.parseDouble(split[1]));
				list.add(latLng);
			}
			open.close();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		final MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marka));
		markerOptions.position(list.get(0));
		final Marker marker = (Marker) mBaiduMap.addOverlay(markerOptions);
		MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(list.get(0));
		mBaiduMap.animateMapStatus(msu);

		final int[] index = {0};
		final List<LatLng> finalList = list;

		Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if (index[0] >= finalList.size()) return;
				LatLng latLng = finalList.get(index[0]);
				marker.setPosition(latLng);
				index[0]++;
				Log.d(TAG.ME, "handleMessage: latLng = " + latLng);
				sendEmptyMessageDelayed(0, 16);
			}
		};
		handler.sendEmptyMessage(0);
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mMapView.onDestroy();
		super.onDestroy();
	}
}
