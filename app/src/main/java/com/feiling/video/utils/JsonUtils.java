package com.feiling.video.utils;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Created by mrqiu on 2017/10/15.
 */

public class JsonUtils {
    public static final Gson sGson = new Gson();

    public static String toJson(Object o) {
        return sGson.toJson(o);
    }

    public static <T, E extends T> T fromJson(String s, Class<E> clazz) {
        Object o = sGson.fromJson(s, clazz);
        return (T) o;
    }

    /**
     * Type type = new TypeToken<List<PayMoney>>() {
     * }.getType();
     *
     * @param s
     * @param type
     * @param <T>
     * @param <E>
     * @return
     */
    public static <T, E extends T> T fromTypeJson(String s, Type type) {
        Object o = sGson.fromJson(s, type);
        return (T) o;
    }

}
