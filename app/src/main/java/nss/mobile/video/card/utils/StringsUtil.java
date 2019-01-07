package nss.mobile.video.card.utils;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description 字符串工具类
 * @author liwei
 * @date 2016年10月11日
 * @version icon_1.0
 */
public final class StringsUtil {

	private static final String TAG = "Strings";

	private StringsUtil() {
		throw new AssertionError();
	}

	public final static boolean isNull(String src) {
		return src == null;
	}

	public final static boolean isEmpty(String src) {
		return src == null || src.length() == 0;
	}

	public final static boolean isBlank(String src) {
		return src != null && "".equals(trimAll(src));
	}

	public final static boolean equalsNull(String src) {
		return src != null && "null".equalsIgnoreCase(trimAll(src));
	}

	public final static String trimAll(String src) {
		if (src == null) {
			return null;
		}
		return src.replaceAll(" ", "");
	}

	public final static boolean isMeaningful(String src) {
		return !isNull(src) && !isBlank(src) && !equalsNull(src);
	}

	public final static String bytes2Hex(byte[] bytes) {
		return byte2Hex(bytes);
	}

	/**
	 * 返回格式化字符串
	 * @param format
	 * @param arg
	 * @return
	 */
	public static String getFormatStr(String format, String arg) {
		String result = "";
		result = String.format(format, arg);
		return result;
	}

	public static String formatBankNo(String bankNo) {
		if (!TextUtils.isEmpty(bankNo)) {
			return bankNo.replaceAll("([\\d]{4})(?=\\d)", "$icon_1 ");
		}
		return bankNo;
	}

	public final static String byte2Hex(byte[] src) {
		if (src == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				sb.append(0);
			}
			sb.append(hv);
		}
		return sb.toString();
	}

	public final static int toInt(String input) {
		try {
			return Integer.parseInt(input);
		} catch (Exception e) {
		}
		return 0;
	}

	public final static long toLong(String input) {
		try {
			return Long.parseLong(input);
		} catch (Exception e) {
		}
		return 0;
	}

	// 获取email的正确格式
	public static boolean isEmailValid(String email) {
		boolean isValid = false;
		String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		CharSequence inputStr = email;
		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		if (matcher.matches()) {
			isValid = true;
		}
		return isValid;
	}

	/**
	 * 验证手机格式
	 */
	public static boolean isMobileNO(String mobiles) {
			/*
			 * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
			 * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
			 * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
			 * "[icon_1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
			 */
		String telRegex = "[1][358]\\d{9}";
		if (TextUtils.isEmpty(mobiles))
			return false;
		else
			return mobiles.matches(telRegex);
	}

	public static boolean isEmpty(CharSequence str) {
		return (str == null || str.length() == 0);
	}

	public static int length(CharSequence str) {
		return str == null ? 0 : str.length();
	}

	public static String nullStrToEmpty(Object str) {
		return (str == null ? "" : (str instanceof String ? (String)str : str.toString()));
	}

	/**
	 * 将第一个字母变成大写
	 * @param str
	 * @return
	 */
	public static String capitalizeFirstLetter(String str) {
		if (isEmpty(str)) {
			return str;
		}
		char c = str.charAt(0);
		return (!Character.isLetter(c) || Character.isUpperCase(c)) ? str : new StringBuilder(str.length())
				.append(Character.toUpperCase(c)).append(str.substring(1)).toString();
	}

	/**
	 * 将字符串格式转化为UTF-8
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException if an error occurs
	 */
	public static String utf8Encode(String str) {
		if (!isEmpty(str) && str.getBytes().length != str.length()) {
			try {
				return URLEncoder.encode(str, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("UnsupportedEncodingException occurred. ", e);
			}
		}
		return str;
	}

	/**
	 * 将字符串格式转化为UTF-8
	 *出现异常就转化为默认的
	 * @param str
	 * @param defultReturn
	 * @return
	 */
	public static String utf8Encode(String str, String defultReturn) {
		if (!isEmpty(str) && str.getBytes().length != str.length()) {
			try {
				return URLEncoder.encode(str, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				return defultReturn;
			}
		}
		return str;
	}

	/**
	 * get innerHtml from href
	 * @param href
	 * @return <ul>
	 *         <li>if href is null, returns ""</li>
	 *         <li>if not match regx, returns source</li>
	 *         <li>returns the last string that match regx</li>
	 *         </ul>
	 */
	public static String getHrefInnerHtml(String href) {
		if (isEmpty(href)) {
			return "";
		}
		String hrefReg = ".*<[\\s]*a[\\s]*.*>(.+?)<[\\s]*/a[\\s]*>.*";
		Pattern hrefPattern = Pattern.compile(hrefReg, Pattern.CASE_INSENSITIVE);
		Matcher hrefMatcher = hrefPattern.matcher(href);
		if (hrefMatcher.matches()) {
			return hrefMatcher.group(1);
		}
		return href;
	}

	/**
	 * @param source
	 * @return
	 */
	public static String htmlEscapeCharsToString(String source) {
		return StringsUtil.isEmpty(source) ? source : source.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
				.replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
	}

	/**
	 * @param s
	 * @return
	 */
	public static String fullWidthToHalfWidth(String s) {
		if (isEmpty(s)) {
			return s;
		}

		char[] source = s.toCharArray();
		for (int i = 0; i < source.length; i++) {
			if (source[i] == 12288) {
				source[i] = ' ';
				// } else if (source[i] == 12290) {
				// source[i] = '.';
			} else if (source[i] >= 65281 && source[i] <= 65374) {
				source[i] = (char)(source[i] - 65248);
			} else {
				source[i] = source[i];
			}
		}
		return new String(source);
	}

	/**
	 * @param s
	 * @return
	 */
	public static String halfWidthToFullWidth(String s) {
		if (isEmpty(s)) {
			return s;
		}
		char[] source = s.toCharArray();
		for (int i = 0; i < source.length; i++) {
			if (source[i] == ' ') {
				source[i] = (char)12288;
				// } else if (source[i] == '.') {
				// source[i] = (char)12290;
			} else if (source[i] >= 33 && source[i] <= 126) {
				source[i] = (char)(source[i] + 65248);
			} else {
				source[i] = source[i];
			}
		}
		return new String(source);
	}
}
