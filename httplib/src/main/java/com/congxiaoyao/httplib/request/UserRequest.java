package com.congxiaoyao.httplib.request;

import com.congxiaoyao.httplib.request.body.User;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
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
}
