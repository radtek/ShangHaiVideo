package nss.mobile.video.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.SocketTimeoutException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import nss.mobile.video.utils.Base64Utils;
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
        String trim = sign.trim();
        header.put("Sign", trim);
        handlerSendRunStatus("header-->" + JsonUtils.toJson(header));
        Map map = new HashMap();
        map.put("idNumber", idNumber);
        map.put("name", name);
        map.put("image", image);
        handlerSendRunStatus(JsonUtils.toJson(map));
        OkHttpHelper.post("http://iep.justice.org.cn/api/identify/face", map, header, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                handlerSendRunStatus(e.getMessage());
                String error;
                if (e instanceof TimeoutException || e instanceof SocketTimeoutException) {
                    error = "连接超时" + e.getLocalizedMessage();
                } else {
                    error = e.getMessage();
                }
                onFailed(error);
            }

            private void onFailed(String error) {
                fl.onFailed(error);
            }

            @Override
            public void onResponse(String response, int id, int code) {
                Map o = JsonUtils.fromTypeJson(response, Map.class);
                double rCode = (double) o.get("code");
                if (rCode == 200) {
                    fl.onSuccess(o);
                    return;
                }

                String description = (String) o.get("description");
                onFailed(description);
            }
        }, 0);

    }

    private static void handlerSendRunStatus(String s) {
//        EventBus.getDefault().post(s);
    }


    public static String encodeImage(Bitmap bitmap) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//        //读取图片到ByteArrayOutputStream
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); //参数如果为100那么就不压缩
//        byte[] bytes = baos.toByteArray();

        String strbm = Base64Utils.bitmapToBase64(bitmap);
        return strbm;
    }


    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 60) { //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * 图片按比例大小压缩方法
     *
     * @param image （根据Bitmap图片压缩）
     * @return
     */
    public static Bitmap compressScale(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        // 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
        if (baos.toByteArray().length / 1024 > 1024) {
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 80, baos);// 这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        // float hh = 800f;// 这里设置高度为800f
        // float ww = 480f;// 这里设置宽度为480f
        float hh = 300f;
        float ww = 300f;
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) { // 如果高度高的话根据高度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be; // 设置缩放比例
        // newOpts.inPreferredConfig = Config.RGB_565;//降低图片从ARGB888到RGB565
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
        //return bitmap;
    }

    public interface OnCheckCardFaceListener {

        void onFailed(String error);

        void onSuccess(Map o);
    }
}
