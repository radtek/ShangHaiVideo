/**   
* @Title: Constant.java
* @Package com.CoreWise.util
* @Description: 通用常量类
* @author Weishun.Xu   
* @date 2014-6-26 下午2:52:02
* @version V1.0   
*/
package nss.mobile.video.card.authentication.utils;

/**
 * @author Administrator
 *
 */
public class Constant {

	// 操作工具类型
	public static final int CONTACT_IC = 0x0;// 接触式IC卡
	public static final int CONTACTLESS_IC = 0x01;// RFID
	public static final int PRINTER = 0x02;// 打印机
	public static final int UHF_TAG = 0x03;// UHF
	public static final int IDCARD = 0x04;// INSIDE

	// 打印机常量
	public static byte[] print_command = { 0x0A };// 打印命令
	public static byte[] initPrinter_command = { 0x1B, 0x40 };// 初始化打印机
	public static byte[] setBold_command = { 0x1B, 0x45, 0x01 };// 设置加粗
	public static byte[] quitBold_command = { 0x1B, 0x45, 0x00 };// 取消加粗
	public static byte[] setTimesHeight_command = { 0x1B, 0x21, 0x01 };// 设置倍高
	public static byte[] setTimesWeight_command = { 0x1B, 0x21, 0x10 };// 设置倍宽
	public static byte[] setTimesHWeight_command = { 0x1B, 0x21, 0x11 };// 设置倍高倍宽
	public static byte[] quitTimesHWeight_command = { 0x1B, 0x21, 0x00 };// 取消倍高倍宽
	public static byte[] setUnderLine_command = { 0x1B, 0x2D, 0x00 };// 无下划线
	public static byte[] setAlignType_command = { 0x1B, 0x61, 0x00 };// 设置左对齐
	public static byte[] printFlashPic_command = { 0x1C, 0x2D, 0x00 };// 打印flash图片
	public static byte[] printQrcode_command = { 0x1D, 0x5A, 0x00 };// 默认Qr码

	public static final String READ_TIME_INFO = "read_time_info";
	public static final String BASIC_INFO = "basic_info";
	public static final String TECH_INFO = "tech_info";

	public static final String NFC_NDEF = "android.nfc.tech.Ndef";
	public static final String NFC_NDEFFORMATABLE = "android.nfc.tech.NdefFormatable";
	public static final String NFC_NFCA = "android.nfc.tech.NfcA";
	public static final String NFC_MIFARECLASSIC = "android.nfc.tech.MifareClassic";
	public static final String NFC_MIFAREULTRALIGHT = "android.nfc.tech.MifareUltralight";
	public static final String NFC_NFCB = "android.nfc.tech.NfcB";
	public static final String NFC_ISODEP = "android.nfc.tech.IsoDep";
	public static final String NFC_NFCF = "android.nfc.tech.NfcF";
	public static final String NFC_NFCV = "android.nfc.tech.NfcV";
	public static final String NFC_ICODESLI = "android.nfc.tech.IcodeSli";
	public static final String NFC_NFCBARCODE = "android.nfc.tech.NfcBarcode";
}
