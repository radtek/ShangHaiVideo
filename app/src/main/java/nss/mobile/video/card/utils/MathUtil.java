package nss.mobile.video.card.utils;

import android.text.TextUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @description 数学管理工具类
 * @author liwei
 * @date 2016年10月11日
 * @version icon_1.0
 */
public class MathUtil {

    public static String getMoney(String price) {
        try{
            if(TextUtils.isEmpty(price)) {
                return "";
            }
            double f = Double.valueOf(price);
            return getMoney(f);
        } catch (Exception e) {
            e.printStackTrace();
            return price;
        }
    }

    /**
     * 保留2位小数
     * @param f
     * @return
     */
    private static String getMoney(double f) {
        return formatFloat2String(f, 2);
    }

    //保留4位小数
    public static String formatFloat2String(float price) {
        return formatFloat2String(price, 4);
    }

    //保留n位小数
    public static String formatFloat2String(String price, int num) {
        try {
            double f = Double.valueOf(price);
            return formatFloat2String(f, num);
        } catch (Exception e) {
            return price;
        }
    }

    //保留n位小数
    public static String formatFloat2String(double price, int num) {
        NumberFormat df = DecimalFormat.getNumberInstance();
        df.setMinimumFractionDigits(num);
        df.setMaximumFractionDigits(num);
        String s = df.format(price);
        LogUtil.d("MathUtil", "money:" + s);
        return s;
    }

    //保留n位小数
    public static String formatFloat2String(float price, int num) {
        NumberFormat df = DecimalFormat.getNumberInstance();
        df.setMinimumFractionDigits(num);
        df.setMaximumFractionDigits(num);
        String s = df.format(price);
        return s;
    }
}
