package com.congxiaoyao.httplib.response;

/**
 * Created by Jaycejia on 2017/2/18.
 */
public class BasicUserInfo {
    private Long userId;

    private String name;

    private Byte gender;

    private String avatar;

    private Integer age;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Byte getGender() {
        return gender;
    }

    public void setGender(Byte gender) {
        this.gender = gender;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "BasicUserInfo{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                '}';
    }
}
