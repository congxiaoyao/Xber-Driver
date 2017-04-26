package com.congxiaoyao.httplib.request;

import com.congxiaoyao.httplib.request.body.LaunchTaskRequest;
import com.congxiaoyao.httplib.request.body.StatusChangeRequest;
import com.congxiaoyao.httplib.response.GpsSamplePo;
import com.congxiaoyao.httplib.response.TaskListRsp;

import okhttp3.ResponseBody;
import retrofit2.http.*;
import rx.Observable;

import java.util.List;

/**
 * Created by congxiaoyao on 2017/2/18.
 */
public interface TaskRequest {

    /**
     * 生成运输任务
     *
     * @param request
     */
    @POST("task")
    Observable<String> generateTask(@Body LaunchTaskRequest request,
                                    @Header("Authorization") String token);

    /**
     * 更改任务状态
     *
     * @param request
     */
    @PUT("task/status")
    Observable<String> changeTaskStatus(@Body StatusChangeRequest request,
                                        @Header("Authorization") String token);

    /**
     * 获取任务列表
     *
     * @param userId
     * @param pageIndex
     * @param pageSize
     * @param status
     * @param timestamp
     * @return
     */
    @GET("task")
    Observable<TaskListRsp> getTask(@Query("userId") Long userId,
                                    @Query("pageIndex") Integer pageIndex,
                                    @Query("pageSize") Integer pageSize,
                                    @Query("status") Integer status,
                                    @Query("timestamp") Long timestamp,
                                    @Query("createUserId") Long createUserId,
                                    @Header("Authorization") String token);

    /**
     * 根据任务id获取车辆历史轨迹
     *
     * @param taskId
     * @return
     */
    @GET(value = "task/trace")
    Observable<List<GpsSamplePo>> getCarTrace(@Query("taskId") Long taskId,
                                              @Header("Authorization") String token);

    /**
     * 根据任务id获取车辆历史轨迹
     *
     * @param taskId
     * @return
     */
    @GET(value = "task/trace/bytes")
    Observable<ResponseBody> getCarTraceBytes(@Query("taskId") Long taskId,
                                              @Header("Authorization") String token);
}
