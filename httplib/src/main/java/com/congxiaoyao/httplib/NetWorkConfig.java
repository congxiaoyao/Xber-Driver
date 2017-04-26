package com.congxiaoyao.httplib;

/**
 * Created by congxiaoyao on 2017/3/1.
 */

public class NetWorkConfig {

//    public static final String HOST_ADDR = "www.congxiaoyao.cn";
    public static final String HOST_ADDR = "192.168.31.203";
//    public static final String PORT = "80";
    public static final String PORT = "8080";
    public static final String ROOT_PATH = "/Xber/";
    public static final String BASE_URL = "http://" + HOST_ADDR + ":" + PORT + ROOT_PATH;
    public static final String WS_URL = "ws://" + HOST_ADDR + ":" + PORT + ROOT_PATH +
            "websocket-handshake/websocket";
    public static final String AUTH_KEY = "Authorization";

    public static final String UPLOAD_PATH = "/app/gpsSample/upload";

    public static final String NEAREST_N_ASK_PATH = "/app/trace/nearestNCars";
    public static final String NEAREST_N_RSP_PATH = "/user/{userId}/trace/nearestNCars";
    public static final String SPECIFIED_ASK_PATH = "/app/trace/specifiedCars";
    public static final String SPECIFIED_RSP_PATH = "/user/{userId}/trace/specifiedCars";

    public static final String TASK_STATUS_CHANGE = "/topic/task";
    public static final String USER_SYSTEM = "/user/{userId}/system";

    protected static final String CLIENT_ID = "20136213";
    public static String TOKEN_TEST = "Basic " +
            CLIENT_ID + ":29497794aa9cebfe194d2622582de5d5";
}
