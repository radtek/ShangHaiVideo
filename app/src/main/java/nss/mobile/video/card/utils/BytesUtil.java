package nss.mobile.video.card.utils;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;

/**
 * @description 字节处理工具类
 * @author liwei
 * @date 2016年10月11日
 * @version icon_1.0
 */
public class BytesUtil {

	/**
	 * 合并byte数组
	 * @param head
	 * @param body
	 * @return
	 */
	public static byte[] byteContact(byte[] head, byte[] body) {
		byte[] result = new byte[head.length + body.length];
		System.arraycopy(head, 0, result, 0, head.length);
		System.arraycopy(body, 0, result, head.length, body.length);
		return result;
	}

	/**
	 * 字节数组 转换成 16 进制 字符串 
	 * @param bytes
	 * @return
	 */
	public static String bytesToHexString(byte[] bytes){
		StringBuilder stringBuilder = new StringBuilder("");
		if (bytes == null || bytes.length <= 0) {
			return null;
		}
		for (int i = 0; i < bytes.length; i++) {
			int v = bytes[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}
	
	public static String bytesToHexString(int[] src, String interval) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
			stringBuilder.append(interval);
		}
		return stringBuilder.toString();
	}
	
	/**
	 * 16进制串转换成字节数组
	 * @param hexString
	 * @return
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if ((hexString == null) || (hexString.equals(""))) {
			return null;
		}
		hexString = hexString.toUpperCase().replaceAll(" ", "");
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; ++i) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[(pos + 1)]));
		}
		return d;
	}

	public static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
	
	/**
	 * 不进位校验和
	 * @return
	 */
	public static byte sum(byte[] data) {
		return sum(data, false);
	}
	
	/**
	 * 不进位校验和
	 * @return
	 */
	public static byte sum(byte[] data, boolean self) {
		byte sum = 0;
		int length = self ? data.length - 1 : data.length;
		for (int i = 0; i < length; i++) {
			sum += data[i];
		}
		return sum;
	}
	
	/**
	 * 不进位校验和
	 * @return
	 */
	public static int sum(int[] data, boolean self) {
		int sum = 0;
		int length = self ? data.length - 1 : data.length;
		for (int i = 0; i < length; i++) {
			sum += data[i];
		}
		return (int) (sum & 0xFF);
	}
	
	/**
	 * 检查校验和  (不进位校验和)
	 * @return
	 */
	public static boolean checkSum(byte[] data) {
		return sum(data, true) == data[data.length - 1];
	}
	
	public static short getShort(byte[] bytes) {
		return (short) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
	}
	
	public static char getChar(byte[] bytes) {
		return (char) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
	}
	/**
	 * 十六进制 转 ASCII码
	 * @param hex
	 * @return
	 */
	public static String convertHexToString(String hex) {
		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();
		// 49204c6f7665204a617661 split into two characters 49, 20, 4c...
		for (int i = 0; i < hex.length() - 1; i += 2) {
			// grab the hex in pairs
			String output = hex.substring(i, (i + 2));
			// convert hex to decimal
			int decimal = Integer.parseInt(output, 16);
			// convert the decimal to character
			sb.append((char) decimal);
			temp.append(decimal);
		}
		return sb.toString();
	}

	/**
	 * 对象转化成字节数组
	 * @param obj
	 * @return
	 */
	public static byte[] ObjectToByte(Object obj) {
		byte[] bytes;
		try {
			//object to bytearray
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(bo);
			oo.writeObject(obj);
			bytes = bo.toByteArray();
			bo.close();
			oo.close();
			return (bytes);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 字节转换成相应大小的MB,KB
	 * @param bytes
	 * @return
	 */
	public static String bytes2Convert(long bytes) {
		BigDecimal filesize = new BigDecimal(bytes);
		BigDecimal gbyte = new BigDecimal(1024 * 1024 * 1024);
		float returnValue = filesize.divide(gbyte, 2, BigDecimal.ROUND_UP).floatValue();
		if (returnValue >= 1) {
			return (returnValue + "GB");
		}
		BigDecimal megabyte = new BigDecimal(1024 * 1024);
		returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP).floatValue();
		if (returnValue >= 1) {
			return (returnValue + "MB");
		}
		BigDecimal kilobyte = new BigDecimal(1024);
		returnValue = filesize.divide(kilobyte).intValue();
		return (returnValue + "KB");
	}

    public static byte[] bitmapToByte(Bitmap bitmap){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] datas = baos.toByteArray();
		return datas;
	}
}
