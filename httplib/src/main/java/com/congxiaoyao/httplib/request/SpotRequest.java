package com.congxiaoyao.httplib.request;

import com.congxiaoyao.httplib.response.Spot;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by congxiaoyao on 2017/2/18.
 */
public interface SpotRequest {

    @GET("spot/all")
    Observable<List<Spot>> getAllSpots(@Header("Authorization") String token);

    @POST("spot")
    Observable<String> addSpot(@Body Spot spot, @Header("Authorization") String token);

    @PUT("spot")
    Observable<String> updateSpot(@Body Spot spot, @Header("Authorization") String token);

    @DELETE("spot/{id}")
    Observable<String> deleteSpot(@Path("id") Long id, @Header("Authorization") String token);

}
