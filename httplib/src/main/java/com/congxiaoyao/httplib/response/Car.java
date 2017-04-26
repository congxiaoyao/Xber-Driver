package com.congxiaoyao.httplib.response;

/**
 * Created by Jaycejia on 2017/2/18.
 */
public class Car {
    private Long carId;
    private String plate;
    private String spec;

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    @Override
    public String toString() {
        return "Car{" +
                "carId=" + carId +
                ", plate='" + plate + '\'' +
                ", spec='" + spec + '\'' +
                '}';
    }
}
