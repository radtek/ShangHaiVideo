package nss.mobile.video.card.authentication.api.barcode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.util.Log;

/**
* 时间：2018/10/24
* 描述：肯麦思扫码机api，包括一维码、二维码api接口（CoreWise Scanner Api，including One-dimensional code，QR Code）
*/
public class SoftDecodingAPI {

    private static final String TAG = "SoftDecodingAPI";

    private static final String SCANNER_POWER_ON = "SCANNER_POWER_ON";
    private static final String SCANNER_OUTPUT_MODE = "SCANNER_OUTPUT_MODE";
    private static final String SCANNER_TERMINAL_CHAR = "SCANNER_TERMINAL_CHAR";
    private static final String SCANNER_PREFIX = "SCANNER_PREFIX";
    private static final String SCANNER_SUFFIX = "SCANNER_SUFFIX";
    private static final String SCANNER_VOLUME = "SCANNER_VOLUME";
    private static final String SCANNER_PLAYTONE_MODE = "SCANNER_PLAYTONE_MODE";

    private static final int ENABLE = 1;
    private static final int DISENABLE = 0;

    private ScannerThread scannerThread;


    private IBarCodeData inter;

    private Context mcontext;
    private String Prefix = "";
    private String Suffix = "";
    private String barcode = "";

    int all = 0, success = 0;

    /**
     * 构造函数（Constructor）
     * @param context 上下文
     * @param inter 扫码接口
     */
    public SoftDecodingAPI(Context context, IBarCodeData inter) {
        this.mcontext = context;
        this.inter = inter;
    }


    /**
     * 获取扫码机系统设置（Get Scanner System Settings）
     */
    public void getSettings() {
        int mPowerOnOff = Settings.System.getInt(mcontext.getContentResolver(), SCANNER_POWER_ON, 1);
        int mOutputMode = Settings.System.getInt(mcontext.getContentResolver(), SCANNER_OUTPUT_MODE, -1);
        int mTerminalChar = Settings.System.getInt(mcontext.getContentResolver(), SCANNER_TERMINAL_CHAR, -1);
        String mPrefix = Settings.System.getString(mcontext.getContentResolver(), SCANNER_PREFIX);
        Prefix = mPrefix;
        String mSuffix = Settings.System.getString(mcontext.getContentResolver(), SCANNER_SUFFIX);
        Suffix = mSuffix;
        int mVolume = Settings.System.getInt(mcontext.getContentResolver(), SCANNER_VOLUME, 0);
        int mPlayoneMode = Settings.System.getInt(mcontext.getContentResolver(), SCANNER_PLAYTONE_MODE, 0);
        inter.getSettings(mPowerOnOff, mOutputMode, mTerminalChar, mPrefix, mSuffix, mVolume, mPlayoneMode);
    }

    /**
     * 设置扫码机参数（Set Scanner Params）
     * @param PowerOnOff
     * @param OutputMode 输出模式
     * @param TerminalChar
     * @param Prefix 前缀
     * @param Suffix 后缀
     * @param Volume 音量
     * @param PlayoneMode 震动音效效果
     */

    public void setSettings(int PowerOnOff, int OutputMode, int TerminalChar, String Prefix, String Suffix, int Volume, int PlayoneMode) {

        Settings.System.putInt(mcontext.getContentResolver(), SCANNER_POWER_ON, PowerOnOff);
        Settings.System.putInt(mcontext.getContentResolver(), SCANNER_OUTPUT_MODE, OutputMode);
        Settings.System.putInt(mcontext.getContentResolver(), SCANNER_TERMINAL_CHAR, TerminalChar);
        Settings.System.putString(mcontext.getContentResolver(), SCANNER_PREFIX, Prefix);
        Settings.System.putString(mcontext.getContentResolver(), SCANNER_SUFFIX, Suffix);
        Settings.System.putInt(mcontext.getContentResolver(), SCANNER_VOLUME, Volume);

        switch (PlayoneMode) {
            case 0:
                Settings.System.putInt(mcontext.getContentResolver(), "SCANNER_SOUND", 1);
                Settings.System.putInt(mcontext.getContentResolver(), "SCANNER_VIBRATION", 0);
                break;
            case 1:
                Settings.System.putInt(mcontext.getContentResolver(), "SCANNER_SOUND", 0);
                Settings.System.putInt(mcontext.getContentResolver(), "SCANNER_VIBRATION", 1);
                break;
            case 2:
                Settings.System.putInt(mcontext.getContentResolver(), "SCANNER_SOUND", 1);
                Settings.System.putInt(mcontext.getContentResolver(), "SCANNER_VIBRATION", 1);
                break;
            case 3:
                Settings.System.putInt(mcontext.getContentResolver(), "SCANNER_SOUND", 0);
                Settings.System.putInt(mcontext.getContentResolver(), "SCANNER_VIBRATION", 0);
                break;
            default:
                break;
        }

        Settings.System.putInt(mcontext.getContentResolver(), SCANNER_PLAYTONE_MODE, PlayoneMode);

        Intent intent = new Intent("com.android.server.scannerservice.settingchange", null);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        mcontext.sendOrderedBroadcast(intent, null);

        inter.setSettingsSuccess();
    }

    /**
     * Setting scanner switch
     *
     * @param flag true:open;false:close
     */
    @Deprecated
    public void setScannerStatus(boolean flag) {
        if (flag) {
            Intent intent = new Intent("com.android.server.scannerservice.onoff");
            intent.putExtra("scanneronoff", ENABLE);
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            mcontext.sendBroadcast(intent);
        } else {
            Intent intent = new Intent("com.android.server.scannerservice.onoff");
            intent.putExtra("scanneronoff", DISENABLE);
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            mcontext.sendBroadcast(intent);
        }
    }

    /**
     * 开始扫描（Start scanning）
     */
    public void scan() {
        inter.sendScan();
        Intent startIntent = new Intent("android.intent.action.SCANNER_BUTTON_DOWN", null);
        startIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        mcontext.sendOrderedBroadcast(startIntent, null);
        all++;
    }

    /**
     * 关闭扫描（Close scanning）
     */
    @Deprecated
    public void closeScan() {
        Intent endIntent = new Intent("android.intent.action.SCANNER_BUTTON_UP", null);
        mcontext.sendOrderedBroadcast(endIntent, null);
    }

    /**
     * 注册广播，必须加,api里涉及广播的一定要成双成对的出现，建议在onResume里添加（Register broadcast）
     */
    public void openBarCodeReceiver() {
        IntentFilter filter = new IntentFilter("com.android.server.scannerservice.broadcast");
        mcontext.registerReceiver(receiver, filter);
        Log.i(TAG, "--------扫码广播打开了-----");
    }

    /**
     * 取消广播，必须加,api里涉及广播的一定要成双成对的出现，建议在onPause里添加(UnRegister broadcast)
     */
    public void closeBarCodeReceiver() {
        if (receiver != null) {
            mcontext.unregisterReceiver(receiver);
        }
        Log.i(TAG, "--------扫码广播关闭了-----");
    }


    /**
     * 连续扫码（Continuous Scan）
     */
    public void ContinuousScanning() {
        if (scannerThread == null) {
            scannerThread = new ScannerThread();
            scannerThread.setLoop(true);
            scannerThread.start();
        }
    }


    /**
     * 获取连续扫码间隔（get Continuous interval）
     * @return
     */
    public long getTime() {
        return time;
    }

    /**
     * 设置连续扫码间隔（set continuous interval）
     * @param time
     */
    public void setTime(long time) {
        this.time = time;
    }

    private long time = 0;


    private class ScannerThread extends Thread {

        volatile boolean isLoop;

        public boolean isLoop() {
            return isLoop;
        }

        public void setLoop(boolean isLoop) {
            this.isLoop = isLoop;
        }

        @Override
        public void run() {
            while (isLoop) {
                long l = System.currentTimeMillis();
                scan();
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                closeScan();
            }
        }
    }


    /**
     * 关闭连续扫码（Close Continuous Scan）
     */
    public void CloseScanning() {
        if (scannerThread != null) {
            scannerThread.setLoop(false);
            scannerThread.interrupt();
            scannerThread = null;
        }
    }

    /**
     * Bar code value
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.android.server.scannerservice.broadcast")) {
                barcode = intent.getExtras().getString("scannerdata");
//				byte[] rawdata = intent.getExtras().getByteArray("rawdata");
                inter.onBarCodeData(Prefix + barcode + Suffix);
                success++;
            }
        }
    };


    /**
     * 扫码接口（Scan Interface）
     */
    public interface IBarCodeData {

        /**
         * 开始扫码()
         */
        void sendScan();

        /**
         * 获取扫码值（get barcode data）
         * @param data
         */
        void onBarCodeData(String data);

        /**
         * 获取扫码系统设置（get scanner system settings）
         * @param PowerOnOff
         * @param OutputMode
         * @param TerminalChar
         * @param Prefix
         * @param Suffix
         * @param Volume
         * @param PlayoneMode
         */
        void getSettings(int PowerOnOff, int OutputMode, int TerminalChar, String Prefix, String Suffix, int Volume,
                         int PlayoneMode);

        /**
         * 设置扫码系统设置（set scanner system settings）
         */
        void setSettingsSuccess();
    }

}