package com.congxiaoyao.xber_driver;

import android.content.Context;
import android.content.SharedPreferences;

import com.congxiaoyao.httplib.request.gson.GsonHelper;

/**
 * Created by congxiaoyao on 2017/3/15.
 */

public class Driver {

    private long userId;
    private String userName;
    private String password;

    private String nickName;

    private String token;

    public Driver(String userName, String password, String nickName, String token) {
        this.userName = userName;
        this.password = password;
        this.nickName = nickName;
        this.token = token;
    }

    public Driver() {

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public static Driver fromSharedPreference(Context context) {
        SharedPreferences xber_sp = context.getSharedPreferences("xber_sp", Context.MODE_PRIVATE);
        return fromSharedPreference(xber_sp);
    }

    private static Driver fromSharedPreference(SharedPreferences sharedPreferences) {
        String json = sharedPreferences.getString("driver", null);
        if (json == null) return null;
        Driver driver = GsonHelper.getInstance().fromJson(json, Driver.class);
        return driver;
    }

    public void save(Context context) {
        SharedPreferences xber_sp = context.getSharedPreferences("xber_sp", Context.MODE_PRIVATE);
        save(xber_sp);
    }

    private void save(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        String json = GsonHelper.getInstance().toJson(this);
        edit.putString("driver", json);
        edit.commit();
    }

    @Override
    public String toString() {
        return "Driver{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", nickName='" + nickName + '\'' +
                ", userId='" + userId + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}

