package com.congxiaoyao.httplib.request.okhttp;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * 自定义了cookie管理的单例的OkHttpClient，虽然不一定用得到cookie，但先写着呗
 *
 * Created by congxiaoyao on 2016/6/27.
 */
public class MyOkHttp {

    private static OkHttpClient okHttpClient;

    private static final Map<String, List<Cookie>> cookiesStore = new HashMap<>();

    public static OkHttpClient getInstance() {
        if (okHttpClient == null) {
            synchronized (MyOkHttp.class) {
                if (okHttpClient == null) {
                    okHttpClient = new OkHttpClient.Builder().cookieJar(new CookieJar() {
                        @Override
                        public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
                            cookiesStore.put(httpUrl.host(), list);
                        }

                        @Override
                        public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                            List<Cookie> cookies = cookiesStore.get(httpUrl.host());
                            return cookies == null ? Collections.EMPTY_LIST : cookies;
                        }
                    }).build();
//                            .addInterceptor(chain -> {
//                        Request request = chain.request().newBuilder()
//                                .addHeader("User-Agent", "SegmentFault for Android v3.2.4 VCODE:53")
//                                .addHeader("X-Version", "2").build();
//                        return chain.proceed(request);
//                    })
                }
            }
        }
        return okHttpClient;
    }
}
