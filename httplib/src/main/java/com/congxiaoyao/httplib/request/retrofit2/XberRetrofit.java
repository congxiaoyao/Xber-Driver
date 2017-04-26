package com.congxiaoyao.httplib.request.retrofit2;

import com.congxiaoyao.httplib.NetWorkConfig;
import com.congxiaoyao.httplib.request.gson.GsonHelper;
import com.congxiaoyao.httplib.request.okhttp.MyOkHttp;
import com.congxiaoyao.httplib.request.retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import com.congxiaoyao.httplib.request.retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Retrofit;
import rx.schedulers.Schedulers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * 单例的Retrofit，完成了一些基本的初始化操作，缓存请求接口以便复用
 *
 * Created by congxiaoyao on 2016/6/28.
 */
public class XberRetrofit {

    private volatile static Retrofit retrofit;
    //TODO 这里的ConcurrentHashMap在安卓中要换成SparseArray
    private volatile static Map<Class, Object> map = new ConcurrentHashMap<>();

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            synchronized (XberRetrofit.class) {
                if (retrofit == null) {
                    retrofit = newRetrofit();
                }
            }
        }
        return retrofit;
    }

    private static Retrofit newRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(NetWorkConfig.BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create(GsonHelper.getInstance()))
                .client(MyOkHttp.getInstance())
                .build();
    }

    /**
     * 建议通过create方法创建请求接口，以便缓存结果
     * @param clazz
     * @param <T>
     * @return 跟Retrofit的返回值是一样的
     */
    public static <T> T create(Class<T> clazz) {
        T t = (T) map.get(clazz);
        if (t == null) {
            t = getRetrofit().create(clazz);
            map.put(clazz, t);
        }
        return t;
    }
}
