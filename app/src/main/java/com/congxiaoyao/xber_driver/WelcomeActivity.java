package com.congxiaoyao.xber_driver;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.baidu.mapapi.SDKInitializer;
import com.congxiaoyao.xber_driver.databinding.ActivityWelcomeBinding;
import com.congxiaoyao.xber_driver.login.LoginActivity;
import com.congxiaoyao.xber_driver.main.MainActivity;
import com.congxiaoyao.xber_driver.register.RegisterActivity;
import com.congxiaoyao.xber_driver.utils.Token;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static android.os.Build.MANUFACTURER;

public class WelcomeActivity extends AppCompatActivity {

    public static final int LOGIN_REQUEST_CODE = 100;
    private ActivityWelcomeBinding binding;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            SDKInitializer.initialize(getApplicationContext());
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish();
            overridePendingTransition(0, 0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Token.value = null;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_welcome);
        fitSystemBarTextColor();
        Driver driver = Driver.fromSharedPreference(this);
        if (driver != null) {
            binding.llContainer.setVisibility(View.GONE);
            delayAndJump();
//            if (Math.random() < 0.5) {
//                getSharedPreferences("xber_sp", Context.MODE_PRIVATE)
//                        .edit().putString("driver", null).apply();
//            }
        }else {
            binding.setPresenter(new Presenter());
            showBottomBar();
        }
    }

    private void showBottomBar() {
        binding.getRoot().post(new Runnable() {
            @Override
            public void run() {
                int translateY = binding.llContainer.getHeight();
                binding.llContainer.setTranslationY(translateY);
                ObjectAnimator animator = ObjectAnimator.ofFloat(binding.llContainer, "translationY", translateY, 0)
                        .setDuration(450);
                animator.setStartDelay(300);
                animator.start();
            }
        });

    }

    private void delayAndJump() {
        binding.llContainer.postDelayed(runnable, 500);
    }

    private void fitSystemBarTextColor() {
        if (MANUFACTURER.equals("Xiaomi")) {
            MIUISetStatusBarLightMode(getWindow(), true);
        } else if (isFlyme()) {
            FlymeSetStatusBarLightMode(getWindow(), true);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE && resultCode == LoginActivity.CODE_RESULT_SUCCESS) {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.llContainer.removeCallbacks(runnable);
    }

    public class Presenter{

        public void onLoginClick(View view) {
            startActivityForResult(new Intent(WelcomeActivity.this, LoginActivity.class), 100);
        }

        public void onRegisterClick(View view) {
            startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class));
        }
    }

    public static boolean isFlyme() {
        try {
            final Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (final Exception e) {
            return false;
        }
    }

    public static boolean FlymeSetStatusBarLightMode(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception e) {

            }
        }
        return result;
    }

    /**
     * 设置状态栏字体图标为深色，需要MIUIV6以上
     * @param window 需要设置的窗口
     * @param dark 是否把状态栏字体及图标颜色设置为深色
     * @return  boolean 成功执行返回true
     *
     */
    public static boolean MIUISetStatusBarLightMode(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            Class clazz = window.getClass();
            try {
                int darkModeFlag = 0;
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                if(dark){
                    extraFlagField.invoke(window,darkModeFlag,darkModeFlag);//状态栏透明且黑色字体
                }else{
                    extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字体
                }
                result=true;
            }catch (Exception e){

            }
        }
        return result;
    }
}
