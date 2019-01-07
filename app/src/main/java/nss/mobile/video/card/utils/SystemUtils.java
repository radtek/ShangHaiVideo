package nss.mobile.video.card.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;
import java.util.Calendar;
import java.util.List;

/**
 * @author liwei
 * @version icon_1.0
 * @description 系统应用工具类
 * @date 2016年10月11日
 */
public class SystemUtils {

    /**
     * 获取应用程序名称
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * [获取应用程序版本名称信息]
     *
     * @param context
     * @return 当前应用的版本名
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断后台服务是否在运行
     *
     * @param className
     * @return
     */
    public static boolean isServiceRunning(Context context, String className) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(Integer.MAX_VALUE);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
            ComponentName serviceName = serviceInfo.service;

            if (serviceName.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生日转换年龄
     *
     * @param s 生日，仅支持四位数字年份开头的生日，如“1980”
     * @return
     */
    public static int birth2Age(String s) {
        if (!TextUtils.isEmpty(s)) {
            return 0;
        }
        Calendar mycalendar = Calendar.getInstance();// 获取现在时间
        String year = String.valueOf(mycalendar.get(Calendar.YEAR));
        int now = Integer.parseInt(year);
        int birthYear = Integer.parseInt(s.substring(0, 4));
        if (now < birthYear) {
            return 0;
        }
        return now - birthYear;
    }

    /**
     * 安装一个apk的安装包
     */
    public static void installApk(Context context, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        if (file.exists()) {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }

    /**
     * 判断应用是否已经启动
     *
     * @param context     一个context
     * @param packageName 要判断应用的包名
     * @return boolean
     */
    public static boolean isAppAlive(Context context, String packageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        for (int i = 0; i < processInfos.size(); i++) {
            if (processInfos.get(i).processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断应用是否存在于手机
     *
     * @param context
     * @return
     */
    public static boolean isApplicationAvailable(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isExitMainActivity(Context context, Class cls) {
        Intent intent = new Intent(context, cls);
        ComponentName cmpName = intent.resolveActivity(context.getPackageManager());
        boolean flag = false;
        if (cmpName != null) { // 说明系统中存在这个activity
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfoList = am.getRunningTasks(10);
            for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
                if (taskInfo.baseActivity.equals(cmpName)) { // 说明它已经启动了
                    flag = true;
                    break;  //跳出循环，优化效率
                }
            }
        }
        return flag;
    }

    /**
     * 判断是否在当前Activity是否在栈顶
     */
    public static boolean isThisActivity(Context context, Class className) {
        return isThisActivity(context, className.getName());
    }

    /**
     * 判断是否在当前Activity是否在栈顶
     */
    public static boolean isThisActivity(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否在当前Activity是否在栈顶
     */
    public static boolean isThisActivitys(Context context, Class... classNames) {
        if (context == null || classNames == null || classNames.length == 0) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            for (Class className : classNames) {
                if (className.getName().equals(cpn.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否在当前Activity是否在栈顶
     */
    public static boolean isThisActivity(Context context) {
        if (context == null) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (context.getClass().getName().equals(cpn.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取当前手机的型号
     */
    public String getPhoneModel() {
        String model = android.os.Build.MODEL;
        return model;
    }

    /**
     * 获取当前手机的SDK版本
     */
    public String getPhoneSdkVersion() {
        String sdkVersion = android.os.Build.VERSION.SDK;
        return sdkVersion;
    }

    /**
     * 获取当前手机的系统版本
     */
    public String getPhoneReleasVversion() {
        String releasVersion = android.os.Build.VERSION.RELEASE;
        return releasVersion;
    }
}