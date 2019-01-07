package nss.mobile.video.utils.preferences;

import nss.mobile.video.MyApp;
import nss.mobile.video.utils.PreferencesUtils;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/12/26
 *
 * @author ql
 */
public class CameraRotationCorrectionPreferences {
    public static final String rotation = "rotation";


    public static void saveRotation(int s) {
        PreferencesUtils.saveInt(MyApp.getInstance().getApplicationContext(), rotation, s);
    }

    public static int getRotation() {
        return PreferencesUtils.queryInt(MyApp.getInstance().getApplicationContext(), rotation,-1);
    }
}
