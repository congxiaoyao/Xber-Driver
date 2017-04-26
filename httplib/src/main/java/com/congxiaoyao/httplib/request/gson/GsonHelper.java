package com.congxiaoyao.httplib.request.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by congxiaoyao on 2016/3/29.
 */
public class GsonHelper {

    public static Gson getInstance() {
        return Holder.gson;
    }

    private static class Holder {
        private static Gson gson = createGson();

        private static Gson createGson() {
            return new GsonBuilder()
                    .registerTypeAdapter(Date.class, new DateDeserializer())
                    .registerTypeAdapter(Date.class, new DateSerializer())
                    .setDateFormat(DateFormat.LONG)
                    .create();
        }
    }

}