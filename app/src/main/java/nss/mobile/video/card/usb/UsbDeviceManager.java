package nss.mobile.video.card.usb;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class UsbDeviceManager {

    private UsbDeviceManager() {
    }

    public static UsbDevice findUsbDevice(UsbManager usbManager, int vendorId, int productId) {
        UsbDeviceFilter filter = UsbDeviceFilter.create(vendorId, productId);
        final HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        final List<UsbDevice> result = new ArrayList<UsbDevice>();
        if (deviceList != null) {
            final Iterator<UsbDevice> iterator = deviceList.values().iterator();
            UsbDevice device;
            while (iterator.hasNext()) {
                device = iterator.next();
                if (filter.matches(device)) {
                    result.add(device);
                }
            }
        }
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public static UsbDevice findUsbDevice(Context context, int vendorId, int productId) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        return findUsbDevice(usbManager, vendorId, productId);
    }

    /**
     * returns the filtered device, returns empty if no device matched
     * @param filters
     * @return
     * @throws IllegalStateException
     */
    public static UsbDevice findUsbDevice(final Context context, final List<UsbDeviceFilter> filters) throws IllegalStateException {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        final HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        final List<UsbDevice> result = new ArrayList<UsbDevice>();

        if ((filters == null) || filters.isEmpty()) {
            return null;
        }
        if (deviceList != null) {
            for (final UsbDevice device : deviceList.values()) {
                for (final UsbDeviceFilter filter : filters) {
                    if ((filter != null) && filter.matches(device)) {
                        result.add(device);
                    }
                }
            }
        }
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * returns the filtered devices, returns empty if no device matched
     * @param filters
     * @return
     * @throws IllegalStateException
     */
    public static List<UsbDevice> findUsbDevices(final Context context, final List<UsbDeviceFilter> filters) throws IllegalStateException {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        final HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        final List<UsbDevice> result = new ArrayList<UsbDevice>();
        if ((filters == null) || filters.isEmpty()) {
            return null;
        }
        if (deviceList != null) {
            for (final UsbDevice device : deviceList.values()) {
                for (final UsbDeviceFilter filter : filters) {
                    if ((filter != null) && filter.matches(device)) {
                        result.add(device);
                    }
                }
            }
        }
        return result;
    }

    /**
     * returns the filtered device, returns empty if no device matched
     * @param devices
     * @param filters
     * @return
     * @throws IllegalStateException
     */
    public static UsbDevice findUsbDevice(final List<UsbDevice> devices, final List<UsbDeviceFilter> filters) throws IllegalStateException {
        final List<UsbDevice> result = new ArrayList<UsbDevice>();

        if ((filters == null) || filters.isEmpty()) {
            return null;
        }
        if (devices != null) {
            for (final UsbDevice device : devices) {
                for (final UsbDeviceFilter filter : filters) {
                    if ((filter != null) && filter.matches(device)) {
                        result.add(device);
                    }
                }
            }
        }
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * returns the filtered device, returns empty if no device matched
     * @param filter
     * @return
     * @throws IllegalStateException
     */
    public static List<UsbDevice> findUsbDevice(final Context context, final UsbDeviceFilter filter) throws IllegalStateException {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        final HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        final List<UsbDevice> result = new ArrayList<UsbDevice>();
        if (filter == null) {
            return result;
        }
        if (deviceList != null) {
            for (final UsbDevice device : deviceList.values()) {
                if (filter.matches(device)) {
                    result.add(device);
                }
            }
        }
        return result;
    }
}
