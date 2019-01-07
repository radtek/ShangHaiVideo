package nss.mobile.video.card.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Class for handling usb device permission.
 */
public class UsbDevicePermission {

    private static final String TAG = UsbDevicePermission.class.getSimpleName();
    private static final String ACTION_USB_PERMISSION_PREFIX = "com.zkteco.android.device.action.USB_PERMISSION_";
    private Context mContext;
    private String mUsbDeviceName;
    private PendingIntent mPermissionIntent = null;
    private CountDownLatch mWaitingLatch;
    private UsbReceiver mUsbReceiver;
    private String mUsbPermissionAction;

    public UsbDevicePermission(Context context, String deviceName) {
        this.mContext = context;
        this.mUsbDeviceName = deviceName;
    }

    private void registerUsbReceiver() {
        if (mUsbReceiver == null) {
            mUsbPermissionAction = ACTION_USB_PERMISSION_PREFIX + mUsbDeviceName;
            mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(mUsbPermissionAction), 0);
            final IntentFilter filter = new IntentFilter(mUsbPermissionAction);
            mUsbReceiver = new UsbReceiver();
            mContext.registerReceiver(mUsbReceiver, filter);
        }
    }

    private void unregisterUsbReceiver() {
        if (mWaitingLatch != null) {
            mWaitingLatch.countDown();
            mWaitingLatch = null;
        }

        if (mUsbReceiver != null) {
            mPermissionIntent = null;
            mUsbPermissionAction = null;
            if (mContext != null) {
                mContext.unregisterReceiver(mUsbReceiver);
            }
            mUsbReceiver = null;
        }
    }

    public boolean request(int vendorId, int productId) {
        UsbManager usbManager = (UsbManager) this.mContext.getSystemService(Context.USB_SERVICE);
        UsbDevice device = UsbDeviceManager.findUsbDevice(usbManager, vendorId, productId);
        if (device == null) {
            return false;
        }
        if (usbManager.hasPermission(device)) {
            return true;
        }
        registerUsbReceiver();
        try {
            usbManager.requestPermission(device, mPermissionIntent);
            mWaitingLatch = new CountDownLatch(1);
            try {
                mWaitingLatch.await(10000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return usbManager.hasPermission(device);
        } finally {
            unregisterUsbReceiver();
        }
    }

    private final class UsbReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mUsbPermissionAction.equals(intent.getAction())) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (mWaitingLatch != null) {
                        mWaitingLatch.countDown();
                    }
                }
            }
        }
    }
}
