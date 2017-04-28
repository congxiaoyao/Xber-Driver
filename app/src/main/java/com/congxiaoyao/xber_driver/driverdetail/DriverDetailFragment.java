package com.congxiaoyao.xber_driver.driverdetail;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.congxiaoyao.httplib.response.BasicUserInfo;
import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.xber_driver.Driver;
import com.congxiaoyao.xber_driver.R;
import com.congxiaoyao.xber_driver.databinding.FragmentDriverDetailBinding;

public class DriverDetailFragment extends Fragment {

    private FragmentDriverDetailBinding binding;

    public DriverDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_detail, container, false);
        Driver driver = Driver.fromSharedPreference(getContext());
        CarDetailParcel carDetail = new CarDetailParcel();
        carDetail.setSpec(driver.getSpec());
        carDetail.setPlate(driver.getPlate());
        carDetail.setCarId(driver.getCarId());
        BasicUserInfoParcel userInfo = new BasicUserInfoParcel();
        userInfo.setGender(driver.getGender());
        userInfo.setAge(driver.getAge());
        userInfo.setName(driver.getNickName());
        userInfo.setUserId(driver.getUserId());
        carDetail.setUserInfo(userInfo);
        binding.setData(carDetail);
        return binding.getRoot();
    }
}
