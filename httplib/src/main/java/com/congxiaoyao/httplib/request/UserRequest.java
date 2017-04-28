package com.congxiaoyao.httplib.request;

import com.congxiaoyao.httplib.request.body.User;
import com.congxiaoyao.httplib.response.CarDetail;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by congxiaoyao on 2017/2/19.
 */
public interface UserRequest {

    @POST(value = "user")
    Observable<String> registerDriver(@Body User user, @Header("Authorization") String token);

    @GET(value = "user/{userId}")
    Observable<User> getUserDetail(@Path("userId") Long userId,
                                   @Header("Authorization") String token);

    /**
     * 根据user id获取车辆信息
     *
     * @param userId
     * @return
     */
    @GET(value = "car/user")
    Observable<CarDetail> getCarInfoByUserId(@Query("userId") Long userId,
                                             @Header("Authorization") String token);

}
