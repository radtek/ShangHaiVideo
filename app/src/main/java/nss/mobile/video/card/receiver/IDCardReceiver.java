package nss.mobile.video.card.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;

import nss.mobile.video.card.provider.CardReceiverListener;

//用来监听上层应用的广播,动态广播
public class IDCardReceiver extends BroadcastReceiver {
    private CardReceiverListener mListener;
    public IDCardReceiver(CardReceiverListener listener){
        this.mListener=listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        switch (action) {
            case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                mListener.actionUsbDeviceAttached();
                break;
            case UsbManager.ACTION_USB_DEVICE_DETACHED:
                mListener.actionUsbDeviceDetached();
                break;
            case Intent.ACTION_SCREEN_ON:
                break;
            case Intent.ACTION_SCREEN_OFF:
                break;
        }
    }
}

