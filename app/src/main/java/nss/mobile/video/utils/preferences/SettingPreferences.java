package nss.mobile.video.utils.preferences;

import nss.mobile.video.MyApp;
import nss.mobile.video.utils.PreferencesUtils;

import com.zhy.http.okhttp.utils.L;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/10/26
 *
 * @author ql
 */
public class SettingPreferences {
    public static final int ACTIVITY_VIDEO = 0;
    public static final int ACTIVITY_CARD = 1;

    //多长时间自动播放
    public static final String AUTO_PLAY_LONG = "autoPlayLong";
    //是否自动播放
    public static final String IS_AUTO = "isAuto";

    public static final String ACTIVITY_FIRST = "activityFirst";

    public static void saveAutoPlayLong(long l) {
        PreferencesUtils.saveLong(MyApp.getInstance().getApplicationContext(), AUTO_PLAY_LONG, l);
    }

    public static long getAutoPlayLong() {
        return PreferencesUtils.queryLong(MyApp.getInstance().getApplicationContext(), AUTO_PLAY_LONG, 10);
    }

    public static void saveIsAuto(boolean isAuto) {
        PreferencesUtils.saveBoolean(MyApp.getInstance().getApplicationContext(), IS_AUTO, isAuto);
    }

    public static boolean isAuto() {
        return PreferencesUtils.queryBoolean(MyApp.getInstance().getApplicationContext(), IS_AUTO, false);
    }


    public static void saveActivityFirst(int v) {
        PreferencesUtils.saveInt(MyApp.getInstance(), ACTIVITY_FIRST, v);
    }

    public static int getActivityFirst() {
        return PreferencesUtils.queryInt(MyApp.getInstance(), ACTIVITY_FIRST, 0);
    }


}
