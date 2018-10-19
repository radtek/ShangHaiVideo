package com.feiling.video.utils.preferences;

import android.content.Context;
import android.text.TextUtils;

import com.feiling.video.utils.DateUtils;
import com.feiling.video.utils.PreferencesUtils;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/8/19
 *
 * @author ql
 */
public class UserDevPreferences {

    private static final String FILE_NAME = "userDev";

    private static final String KEY_UN_BIND = "unBind";
    private static final String KEY_SELECT_DEV_ID = "selectDevId";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_DEV_COUNT = "devCount";
    private static String KEY_IN_DATE = "inDate";//入网时间
    public static final String KEY_NICK_NAME = "nickName";

    public static void saveNickName(Context context, String nickName) {
        if (nickName == null) {
            nickName = "";
        }
        PreferencesUtils.saveString(context, FILE_NAME, KEY_NICK_NAME, nickName);
    }

    public static void saveIsHasDev(Context context, boolean isHasDev) {
        PreferencesUtils.saveBoolean(context, FILE_NAME, KEY_UN_BIND, isHasDev);
    }

    public static void saveInDate(Context context, long indate) {
        PreferencesUtils.saveString(context, FILE_NAME, KEY_IN_DATE, DateUtils.getStringDate5(indate));
    }

    public static void saveDevCount(Context context, int devCount) {
        PreferencesUtils.saveInt(context, FILE_NAME, KEY_DEV_COUNT, devCount);
    }

    public static void saveUserId(Context context, int userId) {
        PreferencesUtils.saveInt(context, FILE_NAME, KEY_USER_ID, userId);
    }

    public static void saveSelectDev(Context context, int devId) {
        PreferencesUtils.saveInt(context, FILE_NAME, KEY_SELECT_DEV_ID, devId);
    }

    public static int getUserId(Context context) {
        return PreferencesUtils.queryInt(context, FILE_NAME, KEY_USER_ID, -1);
    }

    public static int getSelectDev(Context context) {
        return PreferencesUtils.queryInt(context, FILE_NAME, KEY_SELECT_DEV_ID, -1);
    }

    public static boolean isHasDev(Context context) {
        return PreferencesUtils.queryBoolean(context, FILE_NAME, KEY_UN_BIND, false);
    }

    public static void clear(Context context) {
        PreferencesUtils.saveBoolean(context, FILE_NAME, KEY_UN_BIND, false);
        PreferencesUtils.saveInt(context, FILE_NAME, KEY_USER_ID, -1);
        PreferencesUtils.saveInt(context, FILE_NAME, KEY_SELECT_DEV_ID, -1);
    }

    public static int getDevCount(Context context) {
        return PreferencesUtils.queryInt(context, FILE_NAME, KEY_DEV_COUNT, 0);
    }

    public static String getInDate(Context context) {
        String indate = PreferencesUtils.queryString(context, FILE_NAME, KEY_IN_DATE);
        if (TextUtils.isEmpty(indate)) {
            indate = "";
        }
        return indate;
    }

    public static String getUserNickName(Context context) {
        String nickName = PreferencesUtils.queryString(context, FILE_NAME, KEY_NICK_NAME);
        if (TextUtils.isEmpty(nickName)) {
            nickName = "";
        }
        return nickName;
    }
}
