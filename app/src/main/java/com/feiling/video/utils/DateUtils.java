package com.feiling.video.utils;/**
 * Created by mrqiu on 2017/11/16.
 */

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ql email:strive_bug@yeah.net
 */
public class DateUtils {
    static {
        hourTime = 60 * 60 * 1000;
        oneDayTime = 24 * 60 * 60 * 1000;
    }

    private static long oneDayTime;//一天的时常；
    private static long hourTime;

    /**
     * 获取现在时间
     *
     * @return返回字符串格式 yyyy-MM-dd HH:mm:ss
     */
    public static String getStringDate(long date) {
        Date currentTime = new Date(date);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     * 获取现在时间
     *
     * @return返回字符串格式 yyyy/MM/dd
     */
    public static String getStringDate2(long date) {
        Date currentTime = new Date(date);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     * 获取现在时间
     *
     * @return返回字符串格式 yyyy年MM月
     */
    public static String getStringDate3(long date) {
        Date currentTime = new Date(date);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     * 获取现在时间
     *
     * @return返回字符串格式 yyyy-MM-dd
     */
    public static String getStringDate4(long date) {
        Date currentTime = new Date(date);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     * 获取现在时间
     *
     * @return返回字符串格式 yyyy-MM
     */
    public static String getStringDate5(long date) {
        Date currentTime = new Date(date);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    public static String getLastDay(long time) {
        long lastTime = System.currentTimeMillis() - time;
        if (lastTime < 0) {
            return "";
        }
        long day = lastTime / oneDayTime;

        if (day == 0) {
            long l = lastTime / hourTime;
            if (l == 0) {
                return "今天";
            } else {
                return String.format("%d小时前", l);
            }

        }
        return String.format("%d天前", day);
    }
}

