package nss.mobile.video.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.SocketTimeoutException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import nss.mobile.video.utils.JsonUtils;
import nss.mobile.video.utils.SignatureUtils;
import okhttp3.Call;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2019/1/9
 *
 * @author ql
 */
public class CardCheckHttp {
    public static final String APP_ID = "f688c732292d422aa1809db1fd5fb12c";
    public static final String App_Secret = "U1WAKbNUflwMg8gCTgqmT5FXl6o1fcDnRQon1cR2";

    public static void checkDard(String name, String idNumber, String image, OnCheckCardFaceListener l) {

        final OnCheckCardFaceListener fl = l;
        String sign = null;
        String Nonce = String.valueOf(System.currentTimeMillis());
        try {
            sign = SignatureUtils.sign(Nonce, App_Secret);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        Map<String, String> header = new HashMap<>();
        header.put("AppId", APP_ID);
        header.put("Nonce", Nonce);
        header.put("Sign", sign);
        OkHttpUtils.post()
                .url("http://iep.justice.org.cn/api/identify/face")
                .headers(header)
                .addParams("name", name)
                .addParams("idNumber", idNumber)
                .addParams("image", image)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        String error;
                        if (e instanceof TimeoutException || e instanceof SocketTimeoutException) {
                            error = "连接超时";
                        } else {
                            error = "连接异常";
                        }
                        onFailed(error);
                    }

                    private void onFailed(String error) {
                        fl.onFailed(error);
                    }

                    @Override
                    public void onResponse(String response, int id, int code) {
                        Map o = JsonUtils.fromTypeJson(response, Map.class);
                        int rCode = (int) o.get("code");
                        if (rCode == 200) {
                            fl.onSuccess(o);
                            return;
                        }

                        String description = (String) o.get("description");
                        onFailed(description);
                    }
                });
    }


    public static String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        //读取图片到ByteArrayOutputStream
        bitmap.compress(Bitmap.CompressFormat.PNG, 40, baos); //参数如果为100那么就不压缩
        byte[] bytes = baos.toByteArray();

        String strbm = Base64.encodeToString(bytes, Base64.DEFAULT);
        return strbm;
    }


    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) { //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    public interface OnCheckCardFaceListener {

        void onFailed(String error);

        void onSuccess(Map o);
    }
}
