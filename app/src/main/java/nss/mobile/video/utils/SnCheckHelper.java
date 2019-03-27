package nss.mobile.video.utils;

import com.zhy.http.okhttp.callback.StringCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nss.mobile.video.http.OkHttpHeader;
import nss.mobile.video.info.UrlApi;
import okhttp3.Call;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2019/3/6
 *
 * @author ql
 */
public class SnCheckHelper {


    public static void checkSn(String sn, OnSnCheckListener listener) {
        // TODO: 2019/3/6 需要验证序列号的 接口
        Map<String, Object> map = new HashMap<>();
        map.put("box-code", sn.trim());
        OkHttpHeader.get(UrlApi.update_app, map, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                listener.onSnCheckFailed("检验异常e1000");
            }

            @Override
            public void onResponse(String response, int id, int code) {
                Map res = JsonUtils.fromJson(response, Map.class);
                Object result = res.get("result");
                if (result.equals(0.0)) {
                    Object dataList = res.get("dataList");
                    if (dataList != null) {
                        List l = (List) dataList;
                        if (l.size() > 0) {
                            listener.onSnCheckSuccess(sn);
                            return;
                        }
                    }

                }
                listener.onSnCheckFailed("非认证注册设备，请联系供应商");
            }
        });
    }


    public interface OnSnCheckListener {

        /**
         * 验证序列号成功 回调
         *
         * @param sn
         */
        void onSnCheckSuccess(String sn);

        /**
         * 验证序列号失败
         *
         * @param error 错误信息
         */
        void onSnCheckFailed(String error);
    }

}
