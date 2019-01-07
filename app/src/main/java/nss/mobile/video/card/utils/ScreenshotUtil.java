package nss.mobile.video.card.utils;

import android.os.Environment;

/**
 * 截图操作
 * Created by Shall on 2015-07-22.
 */
public class ScreenshotUtil {

    private final static String FILE_SAVEPATH = Environment
            .getExternalStorageDirectory().getAbsolutePath() ;
    public static String pathfile = FILE_SAVEPATH + "/ScreenshotUtil.png";
    public static int h = 0;


}
