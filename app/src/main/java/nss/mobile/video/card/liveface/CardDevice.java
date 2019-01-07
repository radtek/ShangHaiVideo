package nss.mobile.video.card.liveface;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.util.Log;

import nss.mobile.video.card.provider.CardReceiverListener;
import nss.mobile.video.card.receiver.IDCardReceiver;


public class CardDevice {
    public static IDCardReceiver initCardReceiver(Context context, CardReceiverListener listener) {
        Log.i("liwei","initCardReceiver");
        IDCardReceiver receiver = new IDCardReceiver(listener);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(receiver, filter);
        return receiver;
    }
}
