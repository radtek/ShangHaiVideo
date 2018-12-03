package nss.mobile.video.service;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/13
 *
 * @author ql
 */
public class UploadFileUtils {
    public static String MOBILE_ID;
    private static final String DATE_FORMAT = "yyyyMMdd";

    public static String getSaveServiceFilePath(String fileName) {
        final String dateStamp = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(System.currentTimeMillis());
        return "/" + dateStamp + "/" + MOBILE_ID + "/" + fileName;
    }
}
