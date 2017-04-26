package com.congxiaoyao.httplib.request;

import com.congxiaoyao.httplib.request.body.LoginBody;
import com.congxiaoyao.httplib.response.LoginInfoResponse;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by congxiaoyao on 2017/2/12.
 */
public interface LoginRequest {

    @POST("login")
    Observable<LoginInfoResponse> login(@Body LoginBody body);

    @GET("testLogin")
    Observable<LoginInfoResponse> test(@Header("Authorization") String token);

}
