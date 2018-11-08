package nss.mobile.video.utils;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/10/30
 *
 * @author ql
 */
public class UnitHelper {

    private static final long unitKb = 1024;
    private static final long unitMb = 1024 * 1024;
    private static final long unitGB = 1024 * 1024 * 1024;

    public static String formatterFileSize(long size) {
        float k = 0;
        String unit = null;
        if (size / unitKb < 1000) {
            //K
            k = size * 1.0f / unitKb;
            unit = "KB";
        } else if (size / unitMb < 1000) {
            //kb
            k = size * 1.0f / unitMb;
            unit = "M";

        } else {
            k = size * 1.0f / unitGB;
            unit = "G";
        }
        return String.format("%.2f %s", k, unit);
    }
}
