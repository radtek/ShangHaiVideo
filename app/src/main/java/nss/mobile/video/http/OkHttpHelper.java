package nss.mobile.video.http;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import nss.mobile.video.utils.JsonUtils;
import nss.mobile.video.utils.LogUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.builder.PostStringBuilder;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.request.RequestCall;

import java.util.Map;

import okhttp3.CacheControl;
import okhttp3.MediaType;

/**
 * Created by mrqiu on 2017/10/12.
 */

public class OkHttpHelper {
    //json请求
    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    public static void get(String url, Object params, Callback callback, Object tag) {
        String s = JsonUtils.toJson(params);
        Map<String, String> map = new Gson().fromJson(s, new TypeToken<Map<String, String>>() {
        }.getType());
        get(url, map, null, callback, tag);
    }

    public static void get(String url, Object params, Callback callback) {
        Map<String, String> paramsMap = null;
        if (params != null) {
            String s = JsonUtils.toJson(params);
            paramsMap = new Gson().fromJson(s, new TypeToken<Map<String, String>>() {
            }.getType());
        }
        get(url, paramsMap, callback);
    }

    public static void get(String url, Object params, Object headers, Callback callback) {
        Map<String, String> paramsMap = null;
        Map<String, String> headerMap = null;
        Gson gson = new Gson();
        if (params != null) {
            String s = JsonUtils.toJson(params);
            paramsMap = gson.fromJson(s, new TypeToken<Map<String, String>>() {
            }.getType());
        }
        if (headers != null) {
            String s = JsonUtils.toJson(headers);
            headerMap = gson.fromJson(s, new TypeToken<Map<String, String>>() {
            }.getType());
        }
        get(url, paramsMap, headerMap, callback);
    }

    public static void get(String url, Object params, Object headers, Callback callback, Object tag) {
        Map<String, String> paramsMap = null;
        Map<String, String> headerMap = null;
        Gson gson = new Gson();
        if (params != null) {
            String s = JsonUtils.toJson(params);
            paramsMap = gson.fromJson(s, new TypeToken<Map<String, String>>() {
            }.getType());
        }
        if (headers != null) {
            String s = JsonUtils.toJson(headers);
            headerMap = gson.fromJson(s, new TypeToken<Map<String, String>>() {
            }.getType());
        }
        get(url, paramsMap, headerMap, callback, tag);
    }


    public static void get(String url, Map<String, String> params, Map<String, String> headers, Callback callback, Object tag) {

        GetBuilder params1 = OkHttpUtils.get().url(url);
        if (params != null) {
            params1.params(params);
        }
        if (tag != null) {
            params1.tag(tag);
        }
        if (headers != null) {
            params1.headers(headers);
        }
        RequestCall build = params1.build();
        CacheControl cacheControl = build.getRequest().cacheControl();
        build.execute(callback);


    }

    public static void get(String url, Map<String, String> params, Map<String, String> headers, Callback callback) {
        get(url, params, headers, callback, null);
    }

    public static void get(String url, Map<String, String> params, Callback callback) {
        get(url, params, null, callback, null);
    }


    @NonNull
    private static PostStringBuilder getPostStringBuilderFromJson() {
        PostStringBuilder postStringBuilder = OkHttpUtils.postString();
        postStringBuilder.mediaType(JSON);
        return postStringBuilder;
    }

    public static void post(String url, Object params, Object headers, Callback callback, Object tag) {
        Map<String, String> headerMap = null;
        if (headers != null) {
            Gson gson = new Gson();
            String s = JsonUtils.toJson(headers);
            headerMap = gson.fromJson(s, new TypeToken<Map<String, String>>() {
            }.getType());
        }
        post(url, params, headerMap, callback, tag);
    }

    public static void post(String url, Object params, Map<String, String> headers, Callback callback, Object tag) {
        PostStringBuilder postStringBuilder = getPostStringBuilderFromJson();
        postStringBuilder.url(url);
        if (params == null) {
            postStringBuilder.content("");
        } else {
            String content = JsonUtils.toJson(params);
            postStringBuilder.content(content);
        }

        if (headers != null) {
            postStringBuilder.headers(headers);
        }
        if (tag != null) {
            postStringBuilder.tag(tag);
        }

        postStringBuilder.build().execute(callback);
    }

    public static void post(String url, Object params, Callback callback, Object tag) {
        post(url, params, null, callback, tag);
    }

    public static void post(String url, Object params, Map<String, String> headers, Callback callback) {
        post(url, params, headers, callback, null);
    }

    public static void post(String url, Object params, Callback callback) {
        post(url, params, null, callback, null);
    }


}
