package com.congxiaoyao.httplib.request.body;

/**
 * Created by Jaycejia on 2017/2/18.
 */
public class CarDriverReq {
    private Long carId;
    private Long userId;

    public CarDriverReq(Long carId, Long userId) {
        this.carId = carId;
        this.userId = userId;
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
