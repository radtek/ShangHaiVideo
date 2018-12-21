package nss.mobile.video.http.ali;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.SimpleTimeZone;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import nss.mobile.video.http.OkHttpHeader;
import nss.mobile.video.utils.JsonUtils;
import okhttp3.Call;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/12/14
 *
 * @author ql
 */
public class AliApiHelper {
    //账号AK信息请填写(必选)
    private static String access_key_id = "LTAIhdAHy8685IVU";
    //账号AK信息请填写(必选)
    private static String access_key_secret = "d17aQE1OrvTaSvhirTqmr3us0cdxqe";
    //STS临时授权方式访问时该参数为必选，使用主账号AK和RAM子账号AK不需要填写
    private static String security_token = "";
    //以下参数不需要修改
    private final static String VOD_DOMAIN = "http://vod.cn-shanghai.aliyuncs.com";
    private final static String ISO8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private final static String HTTP_METHOD = "GET";
    private final static String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private final static String UTF_8 = "utf-8";
    private final static Logger LOG = Logger.getLogger("abcd");
    private static Map<String, String> headers = new HashMap<>();

    public static void getToken(OnLoadAliOssTokenListener l) {
        //生成私有参数，不同API需要修改
        Map<String, String> privateParams = generatePrivateParamters();
        //生成公共参数，不需要修改
        Map<String, String> publicParams = generatePublicParamters("android_test_1.mp4", "androidTest1");
//        Map<String, String> publicParams = new HashMap<>();
        String value = generateTimestamp();
        out("date = " + value);
        //生成OpenAPI地址，不需要修改
        String getUrl = generateOpenAPIURL(publicParams, privateParams);
        out(getUrl);

        headers.put("Host", "vod.cn-shanghai.aliyuncs.com");
        final OnLoadAliOssTokenListener fl = l;
        OkHttpUtils.get()
                .url(getUrl)
                .headers(headers)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        out(e.getMessage());
                        fl.onAliOssFailed(e, "获得ali云token异常");
                    }

                    @Override
                    public void onResponse(String response, int id, int code) {
                        out(response);
                        try {
                            AliOssToken aliOssToken = JsonUtils.fromJson(response, AliOssToken.class);
                            String uploadAddressJson = getFromBASE64(aliOssToken.getUploadAddress());
                            UploadAddress uploadAddress = JsonUtils.fromJson(uploadAddressJson, UploadAddress.class);
                            String uploadAuthJson = getFromBASE64(aliOssToken.getUploadAuth());
                            UploadAuth uploadAuth = JsonUtils.fromJson(uploadAuthJson, UploadAuth.class);
                            fl.onAliOssLoad(aliOssToken, uploadAddress, uploadAuth);
                        } catch (Exception e) {
                            e.printStackTrace();
                            fl.onAliOssFailed(e, "数据解析异常");
                        }
                    }
                });
        //发送HTTP GET 请求
//        httpGet(URL);
    }

    public static void getVideoId(String videoId, OnLoadAliVideoDetailsListener l) {
        //生成私有参数，不同API需要修改
        Map<String, String> privateParams = generatePrivateParamtersVideoDetails(videoId);
        //生成公共参数，不需要修改
        Map<String, String> publicParams = generatePublicParamtersVideoDetails();
//        Map<String, String> publicParams = new HashMap<>();
        String value = generateTimestamp();
        out("date = " + value);
        //生成OpenAPI地址，不需要修改
        String getUrl = generateOpenAPIURL(publicParams, privateParams);
        out(getUrl);

        headers.put("Host", "vod.cn-shanghai.aliyuncs.com");

        final OnLoadAliVideoDetailsListener fl = l;
        OkHttpUtils.get().url(getUrl)
                .headers(headers)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        out(e.getMessage());
                        fl.onAliVideoFailed(e, "获得ali云token异常");
                    }

                    @Override
                    public void onResponse(String response, int id, int code) {
                        out(response);
                        try {
                            AliVideoDetails videoDetails = JsonUtils.fromJson(response, AliVideoDetails.class);
                            fl.onAliVideoDetails(videoDetails);
                        } catch (Exception e) {
                            e.printStackTrace();
                            fl.onAliVideoFailed(e, "数据解析异常");
                        }
                    }
                });
    }

    public static void getVideoPlayUrl(String videoId, OnAliVideoPlayInfoListener l) {
        //生成私有参数，不同API需要修改
        /* 接口私有参数列表, 不同API请替换相应参数 */
        Map<String, String> privateParams = new HashMap<>();
        // 视频ID
        privateParams.put("VideoId", videoId);
        privateParams.put("Formats", "mp4");
        privateParams.put("StreamType", "video");
        // API名称
//        privateParams.put("Action", "CreateUploadVideo");
        privateParams.put("Action", "GetPlayInfo");
        //生成公共参数，不需要修改
        Map<String, String> publicParams = generatePublicParamtersVideoDetails();
//        Map<String, String> publicParams = new HashMap<>();
        String value = generateTimestamp();
        out("date = " + value);
        //生成OpenAPI地址，不需要修改
        String getUrl = generateOpenAPIURL(publicParams, privateParams);
        out(getUrl);

        headers.put("Host", "vod.cn-shanghai.aliyuncs.com");
        final OnAliVideoPlayInfoListener fl = l;
        OkHttpUtils.get().headers(headers)
                .url(getUrl)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        out(e.getMessage());
                        fl.onAliVideoPlayFailed(e, "获得ali云token异常");
                    }

                    @Override
                    public void onResponse(String response, int id, int code) {
                        out(response);
                        try {
                            AliPlayInfoResult aliPlayInfoResult = JsonUtils.fromJson(response, AliPlayInfoResult.class);
                            fl.onAliVideoPlaySuccess(aliPlayInfoResult);
                        } catch (Exception e) {
                            e.printStackTrace();
                            fl.onAliVideoPlayFailed(e, "数据解析异常");
                        }
                    }
                });

    }

    /**
     * 生成视频点播OpenAPI私有参数
     * 不同API需要修改此方法中的参数
     *
     * @return
     */
    private static Map<String, String> generatePrivateParamtersVideoDetails(String videoId) {
        /* 接口私有参数列表, 不同API请替换相应参数 */
        Map<String, String> privateParams = new HashMap<>();
        // 视频ID
        privateParams.put("VideoId", videoId);
        // API名称
//        privateParams.put("Action", "CreateUploadVideo");
        privateParams.put("Action", "GetVideoInfo");
        return privateParams;
    }

    /**
     * 生成视频点播OpenAPI公共参数
     * 不需要修改
     *
     * @return
     */
    private static Map<String, String> generatePublicParamtersVideoDetails() {
        Map<String, String> publicParams = new HashMap<>();
        publicParams.put("Format", "JSON");

        publicParams.put("Version", "2017-03-21");
        publicParams.put("AccessKeyId", access_key_id);
        publicParams.put("SignatureMethod", "HMAC-SHA1");
        String value = generateTimestamp();
        out("date = " + value);
        publicParams.put("Timestamp", value);
        publicParams.put("SignatureVersion", "1.0");
        publicParams.put("SignatureNonce", generateRandom());
        if (security_token != null && security_token.length() > 0) {
            publicParams.put("SecurityToken", security_token);
        }
        return publicParams;
    }

    public interface OnLoadAliOssTokenListener {
        void onAliOssLoad(AliOssToken aliOssToken, UploadAddress uploadAddress, UploadAuth uploadAuth);

        void onAliOssFailed(Exception e, String hint);
    }

    public interface OnLoadAliVideoDetailsListener {
        void onAliVideoDetails(AliVideoDetails videoDetails);

        void onAliVideoFailed(Exception e, String hint);
    }

    /**
     * 生成视频点播OpenAPI私有参数
     * 不同API需要修改此方法中的参数
     *
     * @return
     */
    private static Map<String, String> generatePrivateParamters() {
        /* 接口私有参数列表, 不同API请替换相应参数 */
        Map<String, String> privateParams = new HashMap<>();
        // 视频ID
//        privateParams.put("VideoId", "5aed81b74ba84920be578cdfe004af4b");
        // API名称
        privateParams.put("Action", "CreateUploadVideo");
        return privateParams;
    }

    /**
     * 生成视频点播OpenAPI公共参数
     * 不需要修改
     *
     * @return
     */
    private static Map<String, String> generatePublicParamters(String fileName, String title) {
        Map<String, String> publicParams = new HashMap<>();
        publicParams.put("Format", "JSON");
        publicParams.put("FileName", fileName);
        publicParams.put("Title", title);
        publicParams.put("Version", "2017-03-21");
        publicParams.put("AccessKeyId", access_key_id);
        publicParams.put("SignatureMethod", "HMAC-SHA1");
        String value = generateTimestamp();
        out("date = " + value);
        headers.put("Date", value);
        publicParams.put("Timestamp", value);
        publicParams.put("SignatureVersion", "1.0");
        publicParams.put("SignatureNonce", generateRandom());
        if (security_token != null && security_token.length() > 0) {
            publicParams.put("SecurityToken", security_token);
        }
        return publicParams;
    }

    /**
     * 生成OpenAPI地址
     *
     * @param privateParams
     * @return
     * @throws Exception
     */
    private static String generateOpenAPIURL(Map<String, String> publicParams, Map<String, String> privateParams) {
        return generateURL(VOD_DOMAIN, HTTP_METHOD, publicParams, privateParams);
    }

    /**
     * @param domain        请求地址
     * @param httpMethod    HTTP请求方式GET，POST等
     * @param publicParams  公共参数
     * @param privateParams 接口的私有参数
     * @return 最后的url
     */
    private static String generateURL(String domain, String httpMethod, Map<String, String> publicParams, Map<String, String> privateParams) {
        List<String> allEncodeParams = getAllParams(publicParams, privateParams);
        String cqsString = getCQS(allEncodeParams);
        out("CanonicalizedQueryString = " + cqsString);
        String stringToSign = httpMethod + "&" + percentEncode("/") + "&" + percentEncode(cqsString);
        out("StringtoSign = " + stringToSign);
        String signature = hmacSHA1Signature(access_key_secret, stringToSign);
        out("Signature = " + signature);
        String Authorization = "OSS " + access_key_id + ":" + signature;
        headers.put("Authorization", Authorization);
        out("Authorization = " + Authorization);
        return domain + "?" + cqsString + "&" + percentEncode("Signature") + "=" + percentEncode(signature);
//        return signature;
    }

    private static List<String> getAllParams(Map<String, String> publicParams, Map<String, String> privateParams) {
        List<String> encodeParams = new ArrayList<String>();
        if (publicParams != null) {
            for (String key : publicParams.keySet()) {
                String value = publicParams.get(key);
                //将参数和值都urlEncode一下。
                String encodeKey = percentEncode(key);
                String encodeVal = percentEncode(value);
                encodeParams.add(encodeKey + "=" + encodeVal);
            }
        }
        if (privateParams != null) {
            for (String key : privateParams.keySet()) {
                String value = privateParams.get(key);
                //将参数和值都urlEncode一下。
                String encodeKey = percentEncode(key);
                String encodeVal = percentEncode(value);
                encodeParams.add(encodeKey + "=" + encodeVal);
            }
        }
        return encodeParams;
    }

    /**
     * 参数urlEncode
     *
     * @param value
     * @return
     */
    private static String percentEncode(String value) {
        try {
            String urlEncodeOrignStr = URLEncoder.encode(value, "UTF-8");
            String plusReplaced = urlEncodeOrignStr.replace("+", "%20");
            String starReplaced = plusReplaced.replace("*", "%2A");
            String waveReplaced = starReplaced.replace("%7E", "~");
            return waveReplaced;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * 获取CQS 的字符串
     *
     * @param allParams
     * @return
     */
    private static String getCQS(List<String> allParams) {
        ParamsComparator paramsComparator = new ParamsComparator();
        Collections.sort(allParams, paramsComparator);
        String cqString = "";
        for (int i = 0; i < allParams.size(); i++) {
            cqString += allParams.get(i);
            if (i != allParams.size() - 1) {
                cqString += "&";
            }
        }

        return cqString;
    }

    private static class ParamsComparator implements Comparator<String> {
        @Override
        public int compare(String lhs, String rhs) {
            return lhs.compareTo(rhs);
        }
    }

    private static String hmacSHA1Signature(String accessKeySecret, String stringtoSign) {
        try {
            String key = accessKeySecret + "&";
            try {
                SecretKeySpec signKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
                Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
                mac.init(signKey);
                byte[] rawHmac = mac.doFinal(stringtoSign.getBytes());
                //按照Base64 编码规则把上面的 HMAC 值编码成字符串，即得到签名值（Signature）
                return new String(new BASE64Encoder().encode(rawHmac));
            } catch (Exception e) {
                throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
            }
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 生成随机数
     *
     * @return
     */
    private static String generateRandom() {
        String signatureNonce = UUID.randomUUID().toString();
        return signatureNonce;
    }

    /**
     * 生成当前UTC时间戳
     *
     * @return
     */
    public static String generateTimestamp() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat df = new SimpleDateFormat(ISO8601_DATE_FORMAT);
        df.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return df.format(date);
    }

    private static String httpGet(String url) throws IOException {
        /*
         * Read and covert a inputStream to a String.
         * Referred this:
         * http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
         */
        out("URL = " + url);
        @SuppressWarnings("resource")
        Scanner s = new Scanner(new URL(url).openStream(), UTF_8).useDelimiter("\\A");
        try {
            String resposne = s.hasNext() ? s.next() : "true";
            out("Response = " + resposne);
            return resposne;
        } finally {
            s.close();
        }
    }

    private static void out(String newLine) {
        LOG.log(Level.INFO, newLine);
    }

    //将 BASE64 编码的字符串 s 进行解码
    public static String getFromBASE64(String s) {
        if (s == null) return null;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] b = decoder.decodeBuffer(s);
            return new String(b);
        } catch (Exception e) {
            return null;
        }
    }

}
