package nss.mobile.video;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;


/**
 * Created by mrqiu on 2017/10/15.
 */

public class C {
    public static final Handler sHandler = new Handler(Looper.getMainLooper());


    public static final Gson sGson = new Gson();
    public static final int NORMAL_PAGE_NUM = 1;
    /**
     * 屏幕宽度
     */
    public static int SCREEN_WIDTH;
    /**
     * 屏幕高度
     */
    public static int SCREEN_HEIGHT;
    /**
     * 宽高比例为  4:3
     */
    public static int SCREEN_HEIGHT_3;
    public static int SCREEN_HEIGHT_9;


    public static String DEV_CODE = "devCode";

    public static class File{
        public static int error_power_0 = 1; //警告等级
        public static int error_power_1 = 10;
    }
}
