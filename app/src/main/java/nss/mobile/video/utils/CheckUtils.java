package nss.mobile.video.utils;

import android.text.TextUtils;

/**
 * Created by mrqiu on 2017/10/15.
 */

public class CheckUtils {


    public static boolean isMobileNO(String mobiles) {
        //String telRegex = "13\\d{9}|14[57]\\d{8}|15[012356789]\\d{8}|18[01256789]\\d{8}|17[0678]\\d{8}";
        String telRegex = "^1([358][0-9]|4[579]|66|7[0135678]|9[89])[0-9]{8}$";
        if (TextUtils.isEmpty(mobiles)) return false;
        else return mobiles.matches(telRegex);
    }

    public static boolean isEmail(String strEmail) {
        String strPattern = "^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
        if (TextUtils.isEmpty(strPattern)) {
            return false;
        } else {
            return strEmail.matches(strPattern);
        }
    }

    public static boolean isCardId(String cardId) {
        String ps = "^\\d{14}[0-9a-zA-Z]$|^\\d{17}[0-9a-zA-Z]$";
        if (TextUtils.isEmpty(cardId)) {
            return false;
        }
        return cardId.matches(ps);

    }

    public static boolean isPrice(String prices) {
        String strPattern = "^(([1-9][0-9]*)|0)\\.[0-9]{1,2}$|^[1-9][0-9]*$/";
        if (TextUtils.isDigitsOnly(prices)) {
            return false;
        }

        return prices.matches(strPattern);
    }

    public static boolean isChineseName(String name) {
        String strPattern = "^[\\u2E80-\\uFE4F]{2,6}$";
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        return name.matches(strPattern);
    }

    public static boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean isDouble(String input71) {
        try {
            double v = Double.parseDouble(input71);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isBank(String bank) {
        return !TextUtils.isEmpty(bank);
    }


    public static boolean isMsgCode(String msgCode) {
        if (TextUtils.isEmpty(msgCode)) {
            return false;
        }
        if (msgCode.length() < 4) {
            return false;
        }
        return true;
    }
}
