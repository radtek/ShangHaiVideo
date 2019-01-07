package cn.com.aratek.fp;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import cn.com.aratek.util.OnUsbPermissionGrantedListener;
import cn.com.aratek.util.Result;

/**
 * 时间：2018/10/10
 * 描述：指纹扫描器（Finger Scanner）
 */
public class FingerprintScanner {

    public static final int RESULT_OK = 0;
    public static final int RESULT_FAIL = -1000;
    public static final int WRONG_CONNECTION = -1001;
    public static final int DEVICE_BUSY = -1002;
    public static final int DEVICE_NOT_OPEN = -1003;
    public static final int TIMEOUT = -1004;
    public static final int NO_PERMISSION = -1005;
    public static final int WRONG_PARAMETER = -1006;
    public static final int DECODE_ERROR = -1007;
    public static final int INIT_FAIL = -1008;
    public static final int UNKNOWN_ERROR = -1009;
    public static final int NOT_SUPPORT = -1010;
    public static final int NOT_ENOUGH_MEMORY = -1011;
    public static final int DEVICE_NOT_FOUND = -1012;
    public static final int DEVICE_REOPEN = -1013;
    public static final int NO_FINGER = -2005;
    private static final String TAG = "FingerprintScanner";
    private static final String ACTION_USB_PERMISSION = "cn.com.aratek.cn.com.aratek.fp.USB_PERMISSION";
    private Context mContext;
    private OnUsbPermissionGrantedListener mOnUsbPermissionGrantedListener;
    private UsbManager mUsbManager;
    private UsbDeviceConnection mUsbDeviceConnection;
    private UsbDevice mUsbDevice;
    private UsbInterface mUsbInterface;
    private UsbEndpoint mUsbEndpointBulkIn;
    private UsbEndpoint mUsbEndpointBulkOut;
    private long mDeviceInfo;
    private boolean mReceiverRegistered = false;


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if("cn.com.aratek.cn.com.aratek.fp.USB_PERMISSION".equals(action)) {
                synchronized(this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra("device");
                    if(!intent.getBooleanExtra("permission", false)) {
                        if(FingerprintScanner.this.mOnUsbPermissionGrantedListener != null) {
                            FingerprintScanner.this.mOnUsbPermissionGrantedListener.onUsbPermissionGranted(false);
                        }

                        Log.e("FingerprintScanner", "permission denied for device " + device);
                    } else {
                        Log.i("FingerprintScanner", "permission accepted for device " + device);
                        if(device != null && FingerprintScanner.this.mUsbDevice == null) {
                            FingerprintScanner.this.mUsbDevice = device;
                            if(FingerprintScanner.this.mUsbDevice.getInterfaceCount() > 0) {
                                FingerprintScanner.this.mUsbInterface = FingerprintScanner.this.mUsbDevice.getInterface(0);
                            }

                            if(FingerprintScanner.this.mUsbInterface == null) {
                                Log.e("FingerprintScanner", "Can not get USB interface!");
                                return;
                            }

                            UsbDeviceConnection connection = FingerprintScanner.this.mUsbManager.openDevice(FingerprintScanner.this.mUsbDevice);
                            if(connection != null) {
                                if(connection.claimInterface(FingerprintScanner.this.mUsbInterface, true)) {
                                    FingerprintScanner.this.mUsbDeviceConnection = connection;

                                    for(int j = 0; j < FingerprintScanner.this.mUsbInterface.getEndpointCount(); ++j) {
                                        UsbEndpoint endpoint = FingerprintScanner.this.mUsbInterface.getEndpoint(j);
                                        if(endpoint.getType() == 2) {
                                            if(endpoint.getDirection() == 128) {
                                                FingerprintScanner.this.mUsbEndpointBulkIn = endpoint;
                                            } else if(endpoint.getDirection() == 0) {
                                                FingerprintScanner.this.mUsbEndpointBulkOut = endpoint;
                                            }
                                        }

                                        if(FingerprintScanner.this.mUsbEndpointBulkIn != null && FingerprintScanner.this.mUsbEndpointBulkOut != null) {
                                            break;
                                        }
                                    }
                                } else {
                                    connection.close();
                                }
                            }
                        }

                        FingerprintScanner.this.onUsbPermissionGrantedNative();
                        if(FingerprintScanner.this.mOnUsbPermissionGrantedListener != null) {
                            FingerprintScanner.this.mOnUsbPermissionGrantedListener.onUsbPermissionGranted(true);
                        }
                    }
                }
            } else {
                "android.hardware.usb.action.USB_DEVICE_DETACHED".equals(action);
            }

        }
    };


    static {
        System.loadLibrary("AraBMApiFpAlgorithm");
        System.loadLibrary("AraBione");
        System.loadLibrary("AraBMApiDev");
        System.loadLibrary("WSQ_library_android");
        System.loadLibrary("AraBMApiFp");
        init();
    }

    /**
     * 构造函数（Constructor）
     * @param context
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     */
    public FingerprintScanner(Context context) throws IllegalArgumentException, IllegalStateException {
        if(context == null) {
            throw new IllegalArgumentException("Context cannot null!");
        } else {
            this.mContext = context;
            PackageManager pm = context.getPackageManager();
            if(!pm.hasSystemFeature("android.hardware.usb.host")) {
                throw new IllegalStateException("Feature android.hardware.usb.host is not support!");
            } else {
                this.mUsbManager = (UsbManager)context.getSystemService("usb");
            }
        }
        initDevice();
    }

    /**
     * Usb权限监听（Usb Permission Listener）
     * @param listener
     */
    public void setOnUsbPermissionGrantedListener(OnUsbPermissionGrantedListener listener) {
        this.mOnUsbPermissionGrantedListener = listener;
    }

    private native HashMap<String, UsbDevice> getSupportDevices();

    /**
     * 打开指纹扫描仪（Open Finger Scanner）
     * @return
     */
    public int open() {
        HashMap<String, UsbDevice> devices = this.getSupportDevices();
        if(devices != null && !devices.isEmpty()) {

            if(!this.mReceiverRegistered) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("cn.com.aratek.cn.com.aratek.fp.USB_PERMISSION");
                filter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
                filter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
                this.mContext.registerReceiver(this.mReceiver, filter);
                this.mReceiverRegistered = true;
            }

            PendingIntent pi = PendingIntent.getBroadcast(this.mContext, 0, new Intent("cn.com.aratek.cn.com.aratek.fp.USB_PERMISSION"), 0);
            this.mUsbManager.requestPermission((UsbDevice)((Map.Entry)devices.entrySet().iterator().next()).getValue(), pi);
            int i = this.openNative();


            return i;
        } else {
            return -1012;
        }
    }

    /**
     * 初始化设备（init device）
     * @return
     */
    public int initDevice() {
        HashMap<String, UsbDevice> devices = this.getSupportDevices();
        if(devices != null && !devices.isEmpty()) {
            if(!this.mReceiverRegistered) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("cn.com.aratek.cn.com.aratek.fp.USB_PERMISSION");
                filter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
                filter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
                this.mContext.registerReceiver(this.mReceiver, filter);
                this.mReceiverRegistered = true;
            }
            return this.openNative();
        } else {
            return -1012;
        }
    }

    private native int openNative();

    /**
     * 关闭设备（close device）
     * @return
     */
    public int close() {
        if(this.mReceiverRegistered) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mReceiverRegistered = false;
        }

        if(this.mUsbDeviceConnection != null) {
            if(this.mUsbInterface != null) {
                this.mUsbDeviceConnection.releaseInterface(this.mUsbInterface);
                this.mUsbInterface = null;
                this.mUsbEndpointBulkIn = null;
                this.mUsbEndpointBulkOut = null;
            }

            this.mUsbDeviceConnection.close();
            this.mUsbDeviceConnection = null;
        }
        this.mUsbDevice = null;
        return this.closeNative();
    }

    private native int closeNative();

    private native int onUsbPermissionGrantedNative();

    public native Result getDriverVersion();

    public native Result getFirmwareVersion();

    public native Result getSN();

    public native Result getSensorName();

    public native Result capture();

    public native int prepare();

    public native int finish();

    /** @deprecated */
    public native Result hasFinger();

    private static native void init();
}
