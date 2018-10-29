package nss.mobile.video.http;

import nss.mobile.video.bean.BaseResult;
import nss.mobile.video.utils.JsonUtils;
import nss.mobile.video.utils.LogUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/8/18
 *
 * @author ql
 */
public abstract class ResultCallback2 extends StringCallback{

    /**
     * 直接进入onError中的错误状态值
     */
    public static final int ERROR_EXCEPTION = Integer.MAX_VALUE;
    /**
     * 未登陆返回的状态值
     */
    public static final int ERROR_NOT_LOGIN = 401;
    /**
     * 可以直接显示错误的值
     */
    public static final int ERROR_HINT = 400;

    @Override
    public String parseNetworkResponse(Response response, int id) throws IOException {
        String string = response.body().string();
        return string;
    }

    @Override
    public void onError(Call call, Exception e, int id) {
        String error;
        if (e instanceof TimeoutException || e instanceof SocketTimeoutException) {
            error = "连接超时";
        } else {
            error = "连接异常";
        }
        onFailed(error, ERROR_EXCEPTION);
    }

    protected abstract void onFailed(String error, int code);

    @Override
    public void onResponse(String response, int id, int code) {
        if (code == 200) {
            BaseResult o = JsonUtils.fromJson(response, BaseResult.class);
            onSuccess(o, id);
            return;
        } else {
            try {
                Error o = JsonUtils.fromJson(response, Error.class);
                if (o == null) {
                    throw new IllegalArgumentException("解析错误");
                }
                onFailed(o.getMessage(), code);
            } catch (Exception e) {
                e.printStackTrace();
                onFailed("异常", code);
            }
        }
    }


    @Override
    public boolean validateResponse(Response response, int id) {
        return true;
    }

    protected abstract void onSuccess(BaseResult response, int id);

}
