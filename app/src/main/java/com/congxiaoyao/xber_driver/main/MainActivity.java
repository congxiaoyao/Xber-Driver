package com.congxiaoyao.xber_driver.main;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.httplib.response.TaskRsp;
import com.congxiaoyao.xber_driver.Driver;
import com.congxiaoyao.xber_driver.R;
import com.congxiaoyao.xber_driver.WelcomeActivity;
import com.congxiaoyao.xber_driver.databinding.ActivityMainBinding;
import com.congxiaoyao.xber_driver.driverdetail.DriverDetailActivity;
import com.congxiaoyao.xber_driver.location.LocationService;
import com.congxiaoyao.xber_driver.nav.NavBaseActivity;
import com.congxiaoyao.xber_driver.utils.BaiduMapUtils;
import com.congxiaoyao.xber_driver.utils.Token;
import com.congxiaoyao.xber_driver.widget.LoadingLayout;

import static com.congxiaoyao.xber_driver.location.LocationService.ACTION_ERROR;
import static com.congxiaoyao.xber_driver.location.LocationService.ACTION_LOCATION;
import static com.congxiaoyao.xber_driver.location.LocationService.EXTRA_LOCATION;
import static com.congxiaoyao.xber_driver.login.LoginActivity.CODE_RESULT_SUCCESS;


public class MainActivity extends NavBaseActivity implements GetTaskContract.View {

    private ActivityMainBinding binding;
    private GetTaskContract.Presenter presenter;
    private Driver driver;

    private BDLocation lastLocation;
    private LocationClient locationClient;

    private TaskRsp taskRsp;

    private Runnable restartRunnable = new Runnable() {
        @Override
        public void run() {
            finish();
            startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
        }
    };

    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            lastLocation = intent.getParcelableExtra(EXTRA_LOCATION);
            if (binding.btnMyLocation.isChecked()) showLocation(lastLocation);
        }
    };

    private BroadcastReceiver errorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showRestartDialog("抱歉", "上传位置发生错误,请重启软件");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        configBaiduMap(binding.mapView.getMap());
        driver = Driver.fromSharedPreference(this);
        if (driver == null) {
            showRestartDialog("错误", "请重启软件");
            return;
        }

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setLogo(R.drawable.logo_alpha);

        binding.btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastLocation == null) {
                    ((ToggleButton) v).setChecked(false);
                    Toast.makeText(MainActivity.this, "定位中...", Toast.LENGTH_SHORT).show();
                }else {
                    showLocation(lastLocation);
                    ((ToggleButton) v).setChecked(true);
                    Toast.makeText(MainActivity.this, "正在追踪", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btnNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastLocation == null || taskRsp == null || taskRsp.getEndSpot() == null) return;
                Spot spot = new Spot(-1L, "当前位置", lastLocation.getLatitude(),
                        lastLocation.getLongitude());
                startNav(spot, taskRsp.getEndSpot());
                Toast.makeText(MainActivity.this, "正在初始化导航引擎", Toast.LENGTH_SHORT).show();
            }
        });

        Token.value = driver.getToken();
        GetTaskPresenter presenter = new GetTaskPresenter(this);
        binding.loadingView.below(16);
        presenter.subscribe();

        queryLocationAndShow();
        startService();
    }

    private void startService() {
        LocationService.startServiceForLocation(this);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(locationReceiver, new IntentFilter(ACTION_LOCATION));
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(errorReceiver, new IntentFilter(ACTION_ERROR));

    }

    private void queryLocationAndShow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //检查权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        LocationManager locationManager
                = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gps) {
            showRestartDialog("请打开Gps", "请打开Gps并重启软件");
            return;
        }

        locationClient = new LocationClient(getApplicationContext());
        BDLocationListener myListener = new BDLocationListener(){
            @Override
            public void onConnectHotSpotMessage(String s, int i) {

            }

            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                locationClient.unRegisterLocationListener(this);
                locationClient.stop();
                locationClient = null;
                lastLocation = bdLocation;
                showLocation(bdLocation);
            }
        };
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系
        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps
        locationClient.setLocOption(option);
        locationClient.registerLocationListener(myListener);
        locationClient.start();
    }

    private void showLocation(BDLocation location) {
        BaiduMap map = binding.mapView.getMap();
        map.setMyLocationEnabled(true);
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(location.getDirection()).latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        map.setMyLocationData(locData);
        BaiduMapUtils.moveToLatLng(map, location.getLatitude(), location.getLongitude());
    }

    private void showRestartDialog(String title, String message) {
        binding.tvTitle.setText("发生了一些错误");
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("好吧", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restartRunnable.run();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        restartRunnable.run();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        restartRunnable.run();
                    }
                }).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_setting:

                break;
            case R.id.menu_driver_info:
                startActivity(new Intent(this, DriverDetailActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 1) return;
        boolean result = grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED;
        if (result) {
            queryLocationAndShow();
        } else {
            showRestartDialog("抱歉，请重启", "为了提高定位精度，请务必开启Gps");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && taskRsp != null) {
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
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
        if (locationClient != null) {
            locationClient.stop();
            locationClient = null;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(errorReceiver);
        stopService(new Intent(this, LocationService.class));
    }

    private void configBaiduMap(final BaiduMap baiduMap) {
        UiSettings uiSettings = baiduMap.getUiSettings();
        uiSettings.setRotateGesturesEnabled(false);
        baiduMap.showMapIndoorPoi(false);
        binding.mapView.showZoomControls(false);
        baiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                binding.btnMyLocation.setChecked(false);
            }
        });
        baiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                binding.viewHolder.setVisibility(View.GONE);
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
    public void showTask(TaskRsp taskRsp) {
        this.taskRsp = taskRsp;
        binding.loadingView.hideLoading();
        TravelFragment fragment = TravelFragment.getInstance(taskRsp);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.ff_container, fragment)
                .commit();
    }

    @Override
    public void showReload() {
        binding.loadingView.hideLoading();
        View view = getLayoutInflater().inflate(R.layout.view_no_task, binding.loadingView, true);
        TextView hint = (TextView) view.findViewById(R.id.tv_hint);
        hint.setText("点击重试");
        hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.subscribe();
            }
        });
    }

    @Override
    public void showNoTask() {
        binding.loadingView.hideLoading();
        getLayoutInflater().inflate(R.layout.view_no_task, binding.loadingView, true);
    }

    @Override
    public void clearViews() {
        LoadingLayout loadingView = binding.loadingView;
        View view = null;
        while ((view = loadingView.findViewById(R.id.view_hint)) != null) {
            binding.loadingView.removeView(view);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == CODE_RESULT_SUCCESS) {
            presenter.subscribe();
        }
    }
}

