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
    //多长时间自动播放
    public static final String AUTO_PLAY_LONG = "autoPlayLong";
    //是否自动播放
    public static final String IS_AUTO = "isAuto";

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
        return PreferencesUtils.queryBoolean(MyApp.getInstance().getApplicationContext(), IS_AUTO, true);
    }


}
