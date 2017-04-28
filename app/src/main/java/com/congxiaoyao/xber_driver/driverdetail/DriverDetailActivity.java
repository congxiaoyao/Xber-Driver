package com.congxiaoyao.xber_driver.driverdetail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.congxiaoyao.xber_driver.Driver;
import com.congxiaoyao.xber_driver.R;
import com.congxiaoyao.xber_driver.databinding.ActivityDriverDetailBinding;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

/**
 * Created by guo on 2017/3/29.
 */

public class DriverDetailActivity extends SwipeBackActivity implements CollapsibleHeader {

    private CarDetailParcel parcel;
    private ActivityDriverDetailBinding binding;
    private Drawable shadow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_driver_detail);
        Driver driver = Driver.fromSharedPreference(this);
        binding.tvCarPlate.setText(driver.getPlate());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(driver.getNickName());

        binding.llContainer.post(new Runnable() {
            @Override
            public void run() {
                int height = binding.toolbar.getHeight();
                binding.llContainer.setTranslationY(height / 2);
            }
        });

        LayerDrawable background = (LayerDrawable) binding.appbar.getBackground();
        shadow = background.findDrawableByLayerId(R.id.drawable_shadow);
        shadow.setAlpha(0);

        binding.tabs.setTabMode(TabLayout.MODE_FIXED);
        binding.viewPager.setAdapter(null);
        binding.viewPager.setOffscreenPageLimit(2);
        binding.viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        binding.tabs.setupWithViewPager(binding.viewPager);
        binding.tabs.setSelectedTabIndicatorColor(ContextCompat
                .getColor(this, R.color.colorWhite));

        binding.imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences xber_sp = getSharedPreferences("xber_sp", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = xber_sp.edit();
                edit.putString("driver", null);
                edit.apply();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public int getToolbarHeight() {
        return binding.toolbar.getHeight();
    }

    @Override
    public int getTabBarHeight() {
        return binding.tabs.getHeight();
    }

    @Override
    public int getAppbarHeight() {
        return binding.appbar.getHeight();
    }

    @Override
    public View getDriverView() {
        return binding.llContainer;
    }

    @Override
    public Drawable getBackground() {
        return shadow;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private Fragment[] fragments;
        private String[] titles;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new Fragment[2];
            fragments[0] = new HistoryTaskFragment();
            new HistoryTaskPresenterImpl((HistoryTaskContract.View) fragments[0]);
            fragments[1] = new DriverDetailFragment();
            titles = new String[]{"历史记录", "司机详情"};
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}
