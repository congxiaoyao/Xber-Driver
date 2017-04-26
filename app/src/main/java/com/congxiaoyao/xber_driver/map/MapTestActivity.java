package com.congxiaoyao.xber_driver.map;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.UiSettings;
import com.congxiaoyao.xber_driver.R;
import com.congxiaoyao.xber_driver.TAG;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

public class MapTestActivity extends AppCompatActivity {

    private MapView mMapView;
    private BaiduMap mBaiduMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map_test);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        UiSettings uiSettings = mBaiduMap.getUiSettings();
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setCompassEnabled(false);
        uiSettings.setOverlookingGesturesEnabled(false);
        mBaiduMap.setBuildingsEnabled(false);
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                Log.d(TAG.ME, "onMapStatusChangeFinish: ");
            }
        });

        final GLBitmap bitmap = new GLBitmap();

        mBaiduMap.setOnMapDrawFrameCallback(new BaiduMap.OnMapDrawFrameCallback() {
            @Override
            public void onMapDrawFrame(GL10 gl, MapStatus mapStatus) {
                gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
                gl.glLoadIdentity();
                if (!bitmap.isLoaded()) bitmap.loadGLTexture(gl, MapTestActivity.this);
                bitmap.draw(gl);
            }

            @Override
            public void onMapDrawFrame(MapStatus mapStatus) {

            }
        });
    }

    private void copyMapConfigToSdCard() {
        File file = new File("/sdcard/custom_config");
        if (!file.exists()) {
            try {
                file.createNewFile();
                InputStream inputStream = getAssets().open("custom_config_0323");
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] bytes = new byte[1024 * 10];
                int read = inputStream.read(bytes);
                outputStream.write(bytes, 0, read);
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

}
