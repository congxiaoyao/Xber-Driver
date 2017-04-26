package com.congxiaoyao.httplib.request;

import com.congxiaoyao.httplib.request.body.CarDriverReq;
import com.congxiaoyao.httplib.request.body.NewCar;
import com.congxiaoyao.httplib.response.Car;
import com.congxiaoyao.httplib.response.CarDetail;

import retrofit2.http.*;
import rx.Observable;

import java.util.List;

/**
 * Created by congxiaoyao on 2017/2/19.
 */
public interface CarRequest {

    @POST(value = "car")
    Observable<String> addCar(@Body NewCar car,
                              @Header("Authorization") String token);

    /**
     * 根据car id获取车辆信息
     *
     * @param carId
     * @return
     */
    @GET(value = "car")
    Observable<CarDetail> getCarInfo(@Query("carId") Long carId,
                                     @Header("Authorization") String token);

    /**
     * 获取没有分配司机的车辆
     *
     * @return
     */
    @GET(value = "car/unused")
    Observable<List<Car>> getCarsWithoutDriver(@Header("Authorization") String token);

    /**
     * 获取闲置车辆列表
     *
     * @param startTime
     * @param endTime
     * @return
     */
    @GET(value = "car/free")
    Observable<List<CarDetail>> getFreeCars(@Query("startTime") long startTime,
                                            @Query("endTime") long endTime,
                                            @Header("Authorization") String token);

    /**
     * 根据目的地始发地获取目前正在执行任务的车辆，若传空为不指定
     *
     * @param startSpot
     * @param endSpot
     * @return
     */
    @GET(value = "car/onTask")
    Observable<List<CarDetail>> getCarsOnTask(@Query("startSpot") Long startSpot,
                                              @Query("endSpot") Long endSpot,
                                              @Header("Authorization") String token);

    /**
     * 根据车牌获取车辆信息(模糊匹配)
     *
     * @param plate
     * @return
     */
    @GET(value = "car/{plate}/plate")
    Observable<List<CarDetail>> getCarsByPlate(@Path("plate") String plate,
                                               @Header("Authorization") String token);

    /**
     * 根据用户姓名获取车辆信息
     *
     * @param name
     * @return
     */
    @GET(value = "car/{name}/name")
    Observable<List<CarDetail>> getCarsByName(@Path("name") String name,
                                              @Header("Authorization") String token);

    /**
     * 为车辆分配司机
     *
     * @param req
     * @return
     */
    @PUT(value = "car/driver")
    Observable<String> changeCarDriver(@Body CarDriverReq req,
                                       @Header("Authorization") String token);
}
