package nss.mobile.video.card.authentication.aratek;

import android.content.Context;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;


public class SwitchUtil {

    private static SwitchUtil mSwitchUtil = new SwitchUtil();

    private static String GPIO_USB = "/sys/class/finger/finger/poweron";

    private static String GPIO = "/sys/class/gpio_power/stm32power/enable";

    private final byte[] UP1 = {'1'};
    private final byte[] UP2 = {'2'};

    private final byte[] DOWN1 = {'0'};
    private final byte[] DOWN2 = {'3'};

    /**
     * 获取单例（get Instance）
     * @return SwitchUtil
     */
    public static SwitchUtil getInstance() {
        return mSwitchUtil;
    }

    /**
     * 打开USB(open usb)
     * @return boolean
     */
    public boolean openUSB() {
        try {

            FileOutputStream fw = new FileOutputStream(GPIO_USB);
            fw.write(UP1);
            fw.close();
            fw = new FileOutputStream(GPIO_USB);
            fw.write(UP2);
            fw.close();

            setUpGpio();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 关闭USB(close usb)
     * @return boolean
     */
    public boolean closeUSB() {
        try {
            FileOutputStream fw = new FileOutputStream(GPIO_USB);
            fw.write(DOWN1);
            fw.close();

            fw = new FileOutputStream(GPIO_USB);
            fw.write(DOWN2);
            fw.close();

            fw.close();

            setDownGpio();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean setUpGpio() throws IOException {
        try {
            FileOutputStream fw = new FileOutputStream(GPIO);
            fw.write(UP1);
            fw.close();
            fw = new FileOutputStream(GPIO);
            fw.write(UP2);
            fw.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean setDownGpio() throws IOException {
        try {
            FileOutputStream fw = new FileOutputStream(GPIO);
            fw.write(DOWN1);
            fw.close();
            fw = new FileOutputStream(GPIO);
            fw.write(DOWN2);
            fw.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}