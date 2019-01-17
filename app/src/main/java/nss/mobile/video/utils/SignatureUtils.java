package nss.mobile.video.utils;

import org.apache.commons.net.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2019/1/9
 *
 * @author ql
 */
public class SignatureUtils {

    public static final String HMAC_SHA_256 = "HmacSHA256";

    public static String sign(String nonce, String secret) throws InvalidKeyException, NoSuchAlgorithmException {
        return hmacSha256(secret, nonce + secret);
    }

    public static String hmacSha256(String key, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA_256);
        Mac mac = Mac.getInstance(HMAC_SHA_256);
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeBase64String(rawHmac);
    }
}
