package nss.mobile.video.card.authentication.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 通用工具类
 */
public class ComonUtil {
	/**
	 * 默认的格式化时间时的模式字符串
	 */
	private static String fmtPattern = "yyyy-MM-dd HH:mm:ss";
	/**
	 * 格式化时间的工具
	 */
	private static SimpleDateFormat sdf = new SimpleDateFormat(fmtPattern,Locale.CHINA);
	/**
	 * 被格式化的时间对象
	 */
	private static Date date = new Date();
	/**
	 * 获取格式化后的时间的字符串
	 * @param timeMillis 以毫秒为单位的时间
	 * @return 根据模式字符串格式化后的时间
	 */
	public static String getFormatted(long timeMills){
		return getFormatted("mm:ss", timeMills);
	}
	/**
	 * 获取格式化后的时间的字符串
	 * @param pattern 格式化时使用的模式字符串，例如mm:ss
	 * @param timeMillis 以毫秒为单位的时间
	 * @return 根据模式字符串格式化后的时间
	 */
	public static String getFormatted(String format,long timeMills){
		// 应用参数中指定的模式字符串
		if(format != null && !"".equals(format)) {
			sdf.applyPattern(format);
		}
		// 设置被格式化的时间
		date.setTime(timeMills);
		// 执行格式化，并返回
		return sdf.format(date);
	}
}
