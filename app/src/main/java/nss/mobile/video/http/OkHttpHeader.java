package nss.mobile.video.http;

import nss.mobile.video.utils.JsonUtils;
import nss.mobile.video.utils.LogUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.OtherRequestBuilder;
import com.zhy.http.okhttp.callback.Callback;


import java.util.Map;

/**
 * @author ql
 *         邮箱 email:strive_bug@yeah.net
 *         创建时间 2017/11/23
 */

public class OkHttpHeader {
    private static Map<String, String> headerMap;

    public static class HeaderSetting {
        public static void setHeaderMap(Map<String, String> headerMap) {
            OkHttpHeader.headerMap = headerMap;
        }

  /*      public static void setHeader(Token token) {
            OkHttpHeader.headerMap = token.getHeaderMap();
        }*/

        public static void clearHeader() {
            OkHttpHeader.headerMap = null;
        }
    }

    public static void get(String url, Object params, Callback callback, Object tag) {
        OkHttpHelper.get(url, params, headerMap, callback, tag);
    }

    public static void get(String url, Object params, Callback callback) {
        get(url, params, callback, null);
    }

    public static void get(String url, Callback callback, Object tag) {
        get(url, null, callback, tag);
    }

    public static void get(String url, Callback callback) {
        get(url, null, callback, null);
    }

    public static void post(String url, Object params, Callback callback, Object tag) {
        OkHttpHelper.post(url, params, headerMap, callback, tag);
    }

    public static void post(String url, Object params, Callback callback) {
        post(url, params, callback, null);
    }

    public static void put(String url, Object params, Callback callback, Object tag) {
        OtherRequestBuilder put = OkHttpUtils.put();
        if (headerMap != null) {
            put.headers(headerMap);
        }
        put.url(url);
        String p = "";
        if (params != null) {
            p = JsonUtils.toJson(params);
        }
        put.requestBody(p);
        if (tag != null) {
            put.tag(tag);
        }
        put.build().execute(callback);
    }

    public static void put(String url, Callback callback, Object tag) {
        put(url, null, callback, tag);
    }

    public static void put(String url, Callback callback) {
        put(url, null, callback, null);
    }


    public static void delete(String url, Object params, Callback callback, Object tag) {
        OtherRequestBuilder delete = OkHttpUtils.delete();
        if (headerMap != null) {
            delete.headers(headerMap);
        }
        delete.url(url);
        String p = "";
        if (params != null) {
            p = JsonUtils.toJson(params);
        }
        delete.requestBody(p);
        if (tag != null) {
            delete.tag(tag);
        }
        delete.build().execute(callback);
    }

    public static void delete(String url, Callback callback, Object tag) {
        delete(url, null, callback, tag);
    }

    public static void delete(String url, Callback callback) {
        delete(url, null, callback, null);
    }


}
