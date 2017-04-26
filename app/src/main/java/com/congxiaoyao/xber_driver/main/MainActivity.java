package com.congxiaoyao.xber_driver.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.congxiaoyao.httplib.response.Task;
import com.congxiaoyao.httplib.response.TaskRsp;
import com.congxiaoyao.xber_driver.Driver;
import com.congxiaoyao.xber_driver.R;
import com.congxiaoyao.xber_driver.databinding.ActivityMainBinding;
import com.congxiaoyao.xber_driver.utils.BaiduMapUtils;
import com.congxiaoyao.xber_driver.utils.Token;
import com.congxiaoyao.xber_driver.widget.LoadingLayout;


public class MainActivity extends AppCompatActivity implements GetTaskContract.View {

    private ActivityMainBinding binding;
    private GetTaskContract.Presenter presenter;
    private Driver driver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        configBaiduMap(binding.mapView.getMap());
        driver = Driver.fromSharedPreference(this);
        if (driver == null) {
            handleUnLogin();
            return;
        }

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setLogo(R.drawable.logo_alpha);

        Token.value = driver.getToken();
        GetTaskPresenter presenter = new GetTaskPresenter(this);
        binding.loadingView.below(16);
        presenter.subscribe();
    }

    private void handleUnLogin() {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                finish();
            }
        };
        binding.tvTitle.setText("发生了一些错误");
        new AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage("请重启软件")
                .setNegativeButton("好吧", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        runnable.run();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        runnable.run();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        runnable.run();
                    }
                }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this)
                    .setTitle("退出")
                    .setMessage("确定要退出吗")
                    .setPositiveButton("完全退出", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("后台运行", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            startActivity(intent);
                        }
                    })
                    .show();

        }

        return false;

    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.mapView.onDestroy();
        if (presenter != null) {
            presenter.unSubscribe();
        }
    }

    private void configBaiduMap(final BaiduMap baiduMap) {
        UiSettings uiSettings = baiduMap.getUiSettings();
        uiSettings.setRotateGesturesEnabled(false);
        baiduMap.showMapIndoorPoi(false);
        binding.mapView.showZoomControls(false);

        baiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                BaiduMapUtils.moveToLatLng(baiduMap, 39.066252, 117.147011);
            }
        });
    }

    @Override
    public void showLoading() {
        binding.loadingView.showLoading();
    }

    @Override
    public void hideLoading() {
        binding.loadingView.hideLoading();
    }

    @Override
    public void setPresenter(GetTaskContract.Presenter presenter) {
        this.presenter = presenter;

    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public Driver getDriver() {
        return driver;
    }

    @Override
    public LoadingLayout getLoadingLayout() {
        return binding.loadingView;
    }

    @Override
    public void showTask(TaskRsp taskRsp) {
        getLoadingLayout().hideLoading();
        TravelFragment fragment = TravelFragment.getInstance(taskRsp);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.ff_container, fragment)
                .commit();
    }

}

