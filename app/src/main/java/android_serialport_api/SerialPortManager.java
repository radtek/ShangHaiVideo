package android_serialport_api;

import android.os.SystemClock;
import android.util.Log;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import nss.mobile.video.card.provider.CoreWise;
import nss.mobile.video.card.utils.DataUtils;
import nss.mobile.video.card.utils.LocalLog;

/**
 * SerialPort Manager
 */
public class SerialPortManager {

    private static final String TAG = "SerialPortManager";

    /**
     * 串口波特率(Serial Baudrate)
     */
    private static int BAUDRATE = 460800;

    public static boolean switchRFID = false;

    final byte[] UP = {'1'};
    final byte[] DOWN = {'0'};

    final byte[] FBIUP = {'2'};
    final byte[] FBIDOWN = {'3'};

    private static String PATH = "/dev/ttyHSL0";

    private static String DEFAULT_PATH = "/dev/ttyHSL1";
    private static int DEFAULT_BAUDRATE = 115200;

    private static String SFZ_PATH = "/dev/ttyHSL1";
    private static int BAUDRATE_SFZ = 115200;

    private static int BAUDRATE_UHF = 115200;
    private static String UHF_PATH = "/dev/ttyHSL1";


    private static String GPIO_DEV = "/sys/class/pwv_gpios/as602-en/enable";
    private static String GPIO_DEV_STM32 = "/sys/class/gpio_power/stm32power/enable";

    private static String GPIO_DEV_SFZ = "/sys/class/stm32_gpios/stm32-en/enable";


    private static SerialPortManager mSerialPortManager = new SerialPortManager();

    private static final byte[] SWITCH_COMMAND = "D&C00040104".getBytes();

    private SerialPort mSerialPort = null;

    private boolean isOpen;

    private boolean firstOpen = false;

    private OutputStream mOutputStream;

    private InputStream mInputStream;

    private byte[] mBuffer = new byte[50 * 1024];

    private volatile int mCurrentSize = 0;  //添加volatile，防止死锁

    private LooperBuffer looperBuffer;
/**/

    private ReadThread mReadThread;
    private ReadUHFThread mReadUHFThread;
    private ReadSFZThread mReadSFZThread;

    /**
     * 获取该类的实例对象，为单例（get single instance）
     * @return
     */
    public static SerialPortManager getInstance() {
        return mSerialPortManager;
    }


    public void setBaudrate(int baudrate) {
        BAUDRATE = baudrate;
    }

    /**
     * 判断串口是否打开(Serial Port is Open?)
     *
     * @return true：打开 false：未打开
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * 切换成读取RFID(swift Read RFID)
     *
     * @return
     */
    public void switchStatus() {
        if (!isOpen) {
            return;
        }
        write(SWITCH_COMMAND);
        Log.i("whw", "SWITCH_COMMAND hex=" + new String(SWITCH_COMMAND));
        SystemClock.sleep(200);
        if (!isOpen) {
            return;
        }
        switchRFID = true;
        Log.i("whw", "SWITCH_COMMAND end");
    }


    public boolean openSerialPort() {
        return openSerialPort(CoreWise.type.DEFAULT);
    }

    /**
     * 打开串口(open SerialPort)
     *
     * @param type
     * @return
     */
    public boolean openSerialPort(int type) {
        if (mSerialPort == null) {
            // 上电
            try {
                switch (CoreWise.getModel()) {

                    case CoreWise.device.A370://0: A370
                        setUpGpio();
                        if (isFBIDevice())
                            setDownGpioFbi();
                        Log.i("whw", "setUpGpio status=" + getGpioStatus());
                        mSerialPort = new SerialPort(new File(PATH), BAUDRATE, 0);

                        break;
                    case CoreWise.device.CFON640://1:CFON640
                        setUpGpio();
                        if (isFBIDevice())
                            setDownGpioFbi();
                        Log.i("whw", "setUpGpio status=" + getGpioStatus());
                        mSerialPort = new SerialPort(new File(PATH), BAUDRATE, 0);

                        break;
                    case CoreWise.device.other: //其他机型

                        break;
                    case CoreWise.device.U3_640:  //U3_640
                        setUpGpioSTM32();
                        mSerialPort = new SerialPort(new File(DEFAULT_PATH), DEFAULT_BAUDRATE, 0);
                        break;

                    case CoreWise.device.U3_A370:  //U3_A370
                        setGpioSTM32(GPIO_DEV_SFZ, UP);
                        mSerialPort = new SerialPort(new File(DEFAULT_PATH), DEFAULT_BAUDRATE, 0);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            switch (type) {
                case CoreWise.type.sfz: //身份证
                    Log.i(TAG, "开启了身份证串口");

                    mReadSFZThread = new ReadSFZThread();
                    mReadSFZThread.start();
                    break;

                case CoreWise.type.uhf: //超高频
                    Log.i(TAG, "开启了超高频串口");
                    mReadUHFThread = new ReadUHFThread();
                    mReadUHFThread.start();
                    break;
                case CoreWise.type.DEFAULT:
                    Log.i(TAG, "开启了普通串口");
                    mReadThread = new ReadThread();
                    mReadThread.start();
                    break;
            }


            isOpen = true;
            firstOpen = true;
            return true;
        }
        return false;
    }


    /**
     * 关闭串口(close SerialPort)
     */
    public void closeSerialPort() {
        if (mReadThread != null)
            mReadThread.interrupt();
        mReadThread = null;

        if (mReadSFZThread != null)
            mReadSFZThread.interrupt();
        mReadSFZThread = null;

        if (mReadUHFThread != null)
            mReadUHFThread.interrupt();
        mReadUHFThread = null;

        try {
            switch (CoreWise.getModel()) {

                case CoreWise.device.A370://0: A370
                    // 断电
                    setDownGpio();
                    setDownGpioSTM32();
                    break;
                case CoreWise.device.CFON640://1:CFON640

                    // 断电
                    setDownGpio();
                    setDownGpioSTM32();
                    break;
                case CoreWise.device.other: //其他机型

                    break;
                case CoreWise.device.U3_640:  //U3_640
                    // 断电
                    setDownGpio();
                    setDownGpioSTM32();
                    break;

                case CoreWise.device.U3_A370:  //U3_A370
                    setGpioSTM32(GPIO_DEV_SFZ, DOWN);
                    break;
            }


            Log.i("whw", "setDownGpio status=" + getGpioStatus());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (mSerialPort != null) {
            try {
                mOutputStream.close();
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSerialPort.close();
            mSerialPort = null;
        }
        isOpen = false;
        firstOpen = false;
        mCurrentSize = 0;
        switchRFID = false;
        if (looperBuffer != null) {
            looperBuffer = null;
        }
    }

    /**
     * U3
     * 打开关闭身份证串口
     *
     * @throws SecurityException
     * @throws IOException
     * @throws InvalidParameterException
     */
    @Deprecated
    private boolean openSerialPortSFZ() {
        if (mSerialPort == null) {
            try {
                setUpGpioSTM32();
                mSerialPort = new SerialPort(new File(SFZ_PATH), BAUDRATE_SFZ, 0);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            mReadSFZThread = new ReadSFZThread();
            mReadSFZThread.start();
            isOpen = true;
            return true;
        }
        return false;
    }

    @Deprecated
    private void closeSerialPortSFZ() {
        if (mReadSFZThread != null)
            mReadSFZThread.interrupt();
        mReadSFZThread = null;
        try {
            setDownGpioSTM32();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (mSerialPort != null) {
            try {
                mOutputStream.close();
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSerialPort.close();
            mSerialPort = null;
        }
        isOpen = false;
        mCurrentSize = 0;
        if (looperBuffer != null) {
            looperBuffer = null;
        }
    }

    /**
     * U3
     * 打开关闭超高频
     *
     * @throws SecurityException
     * @throws IOException
     * @throws InvalidParameterException
     */
    @Deprecated
    private boolean openSerialPortUHF() {
        if (mSerialPort == null) {
            try {
                setUpGpioSTM32();
                mSerialPort = new SerialPort(new File(UHF_PATH), BAUDRATE_UHF, 0);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            mReadUHFThread = new ReadUHFThread();
            mReadUHFThread.start();
            isOpen = true;
            return true;
        }
        return false;
    }

    @Deprecated
    private void closeSerialPortUHF() {
        if (mReadUHFThread != null)
            mReadUHFThread.interrupt();
        mReadUHFThread = null;
        try {
            setDownGpioSTM32();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (mSerialPort != null) {
            try {
                mOutputStream.close();
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSerialPort.close();
            mSerialPort = null;
        }
        isOpen = false;
        mCurrentSize = 0;
        if (looperBuffer != null) {
            looperBuffer = null;
        }
    }

    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    ////////  读写串口操作    ////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    /**
     * 读串口操作(Read SerialPort)
     *
     * @param buffer
     * @param waittime
     * @param interval
     * @return
     */
    public synchronized int read(byte buffer[], int waittime, int interval) {
        if (!isOpen) {
            return 0;
        }
        int sleepTime = 5;
        int length = waittime / sleepTime;
        boolean shutDown = false;
        for (int i = 0; i < length; i++) {
            if (mCurrentSize == 0) {
                SystemClock.sleep(sleepTime);
                continue;
            } else {
                break;
            }
        }

        if (mCurrentSize > 0) {
            long lastTime = System.currentTimeMillis();
            long currentTime = 0;
            int lastRecSize = 0;
            int currentRecSize = 0;

            while (!shutDown && isOpen) {
                currentTime = System.currentTimeMillis();
                currentRecSize = mCurrentSize;
                if (currentRecSize > lastRecSize) {
                    lastTime = currentTime;
                    lastRecSize = currentRecSize;
                } else if (currentRecSize == lastRecSize && currentTime - lastTime >= interval) {
                    shutDown = true;
                }
            }

            if (mCurrentSize <= buffer.length) {
                System.arraycopy(mBuffer, 0, buffer, 0, mCurrentSize);
            }

        } else {
            // closeSerialPort2();
            SystemClock.sleep(100);
            // openSerialPort2();
        }
        LocalLog.setLogPath("/sdcard/SFZ/");
        LocalLog.setFileName("SFZ");
        LocalLog.setDefalutTag("SFZ");
        LocalLog.i("测试数据不全---" + mCurrentSize + "--内容--" + DataUtils.bytesToHexString(buffer));
        return mCurrentSize;
    }


    /**
     * @param buffer
     * @param waittime
     * @param requestLength
     * @return
     */
    public synchronized int readFixedLength(byte buffer[], int waittime, int requestLength) {
        return readFixedLength(buffer, waittime, requestLength, 15);
    }

    /**
     * @param buffer
     * @param waittime
     * @param requestLength
     * @param interval
     * @return
     */
    public synchronized int readFixedLength(byte buffer[], int waittime, int requestLength, int interval) {
        if (!isOpen) {
            return 0;
        }
        int sleepTime = 5;
        int length = waittime / sleepTime;
        boolean shutDown = false;
        for (int i = 0; i < length; i++) {
            if (mCurrentSize == 0) {
                SystemClock.sleep(sleepTime);
                continue;
            } else {
                break;
            }
        }

        if (mCurrentSize > 0) {
            long lastTime = System.currentTimeMillis();
            long currentTime = 0;
            int lastRecSize = 0;
            int currentRecSize = 0;
            while (!shutDown && isOpen) {
                if (mCurrentSize == requestLength) {
                    shutDown = true;
                } else {
                    currentTime = System.currentTimeMillis();
                    currentRecSize = mCurrentSize;
                    if (currentRecSize > lastRecSize) {
                        lastTime = currentTime;
                        lastRecSize = currentRecSize;
                    } else if (currentRecSize == lastRecSize && currentTime - lastTime >= interval) {
                        shutDown = true;
                    }
                }
            }

            if (mCurrentSize <= buffer.length) {
                System.arraycopy(mBuffer, 0, buffer, 0, mCurrentSize);
            }
        } else {
            closeSerialPort2();
            SystemClock.sleep(100);
            openSerialPort2();
        }
        return mCurrentSize;
    }


    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    ////////  上电操作    ////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////


    /**
     * 通用上电(set GPIO Pin up)
     *
     * @param GPIO
     * @param cmd
     * @throws IOException
     */
    private void setGpioSTM32(String GPIO, byte[] cmd) throws IOException {
        FileOutputStream fw = new FileOutputStream(GPIO);
        fw.write(cmd);
        fw.close();
    }

    @Deprecated
    private void setUpGpioSTM32() throws IOException {
        FileOutputStream fw = new FileOutputStream(GPIO_DEV_STM32);
        fw.write(UP);
        fw.close();
    }

    @Deprecated
    private void setDownGpioSTM32() throws IOException {
        FileOutputStream fw = new FileOutputStream(GPIO_DEV_STM32);
        fw.write(DOWN);
        fw.close();
    }

    @Deprecated
    private boolean openSerialPort2() {
        if (mSerialPort == null) {
            try {
                mSerialPort = new SerialPort(new File(PATH), BAUDRATE, 0);
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i("whw", "mSerialPort=" + mSerialPort);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            mReadThread = new ReadThread();
            mReadThread.start();
            isOpen = true;
            firstOpen = true;
            return true;
        }
        return false;
    }

    @Deprecated
    private void closeSerialPort2() {
        if (mReadThread != null)
            mReadThread.interrupt();
        mReadThread = null;
        if (mSerialPort != null) {
            try {
                mOutputStream.close();
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSerialPort.close();
            mSerialPort = null;
        }
        isOpen = false;
        firstOpen = false;
        mCurrentSize = 0;
        switchRFID = false;
        if (looperBuffer != null) {
            looperBuffer = null;
        }
    }

    public void clearBuffer() {
        mBuffer = null;
        mBuffer = new byte[50 * 1024];
        mCurrentSize = 0;
    }

    public void setLoopBuffer(LooperBuffer looperBuffer) {
        this.looperBuffer = looperBuffer;
    }

    private void writeCommand(byte[] data) {
        if (!isOpen) {
            return;
        }
        if (firstOpen) {
            SystemClock.sleep(2000);
            firstOpen = false;
        }
        mCurrentSize = 0;
        try {
            mOutputStream.write(data);
        } catch (IOException e) {

        }
    }


    public synchronized void write(byte[] data) {
        Log.i("whw", "send commnad=" + DataUtils.toHexString(data));
        writeCommand(data);
    }

    private void setUpGpio() throws IOException {
        FileOutputStream fw = new FileOutputStream(GPIO_DEV);
        fw.write(UP);
        fw.close();
    }

    private void setDownGpio() throws IOException {
        FileOutputStream fw = new FileOutputStream(GPIO_DEV);
        fw.write(DOWN);
        fw.close();
    }

    private void setDownGpioFbi() throws IOException {
        FileOutputStream fw = new FileOutputStream("/sys/class/fbicode_gpios/fbicoe_state/control");
        fw.write(FBIDOWN);
        fw.close();
    }

    private boolean isFBIDevice() {
        String path = "/sys/class/fbicode_gpios/fbicoe_state/control";
        File file = new File(path);
        if (file.exists())
            return true;
        else
            return false;
    }

    public String getGpioStatus() throws IOException {
        String value;
        BufferedReader br = null;
        FileInputStream inStream = new FileInputStream(GPIO_DEV);
        br = new BufferedReader(new InputStreamReader(inStream));
        value = br.readLine();
        inStream.close();
        return value;

    }


    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    ////////     各种读取串口的线程    ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////


    /**
     * 通用读取串口线程(General Read Serial Thread)
     */
    private class ReadThread extends Thread {
        @Override
        public void run() {
            byte[] buffer = new byte[2325];
            while (!isInterrupted()) {
                int length = 0;
                try {
                    if (mInputStream == null) return;
                    length = mInputStream.read(buffer);
                    if (length > 0) {
                        if (looperBuffer != null) {
                            byte[] buf = new byte[length];
                            System.arraycopy(buffer, 0, buf, 0, length);
                            Log.i("xuws", "recv buf=" + DataUtils.toHexString(buf));
                            looperBuffer.add(buf);
                        }
                        System.arraycopy(buffer, 0, mBuffer, mCurrentSize, length);
                        mCurrentSize += length;
                        Log.i("whw", "mCurrentSize=" + mCurrentSize + "  length=" + length);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    /**
     * 身份证读取串口线程(Read IDCard Serial Thread)
     */
    private class ReadSFZThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                int length = 0;
                byte[] buffer = new byte[2325];

                try {
                    if (mInputStream == null) return;
                    if (mInputStream.available() > 0 == false) {
                        continue;
                    } else {
                        switch (CoreWise.getModel()) {

                            case CoreWise.device.A370://0: A370
                                Thread.sleep(200);   //此处延时确保一次性获取数据，最低190ms

                                break;
                            case CoreWise.device.CFON640://1:CFON640

                                Thread.sleep(200);   //此处延时确保一次性获取数据，最低190ms

                                break;
                            case CoreWise.device.other: //其他机型
                                Thread.sleep(200);   //此处延时确保一次性获取数据，最低190ms

                                break;
                            case CoreWise.device.U3_640:  //U3_640
                                Thread.sleep(200);   //此处延时确保一次性获取数据，最低190ms
                                break;

                            case CoreWise.device.U3_A370:  //U3_A370
                                Thread.sleep(300);   //此处延时确保一次性获取数据，最低190ms

                                break;
                        }
                    }

                    length = mInputStream.read(buffer);
                    if (length > 0) {
                        /*if (looperBuffer != null) {
                            byte[] buf = new byte[length];
                            System.arraycopy(buffer, 0, buf, 0, length);
                            Log.i("xuws", "recv buf=" + DataUtils.toHexString(buf));
                            looperBuffer.add(buf);
                        }*/
                        Log.i("whw22", "--length--" + length + "--buffer--" + DataUtils.bytesToHexString(buffer));

                        System.arraycopy(buffer, 0, mBuffer, mCurrentSize, length);
                        //mCurrentSize += length;
                        mCurrentSize = length;
                        Log.i("whw", "mCurrentSize=" + mCurrentSize + "  length=" + length);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取超高频串口线程(Read UHF Serial Thread)
     */
    private class ReadUHFThread extends Thread {
        @Override
        public void run() {
            byte[] buffer = new byte[2325];
            while (!isInterrupted()) {
                int length = 0;
                try {
                    if (mInputStream == null) return;
                    if (mInputStream.available() > 0 == false) {
                        continue;
                    } else {
                        Thread.sleep(10);
                    }
                    length = mInputStream.read(buffer);
                    if (length > 0) {
                        if (looperBuffer != null) {
                            byte[] buf = new byte[length];
                            System.arraycopy(buffer, 0, buf, 0, length);
                            Log.i("xuws", "recv buf=" + DataUtils.toHexString(buf));
                            looperBuffer.add(buf);
                        }
                        System.arraycopy(buffer, 0, mBuffer, mCurrentSize, length);
                        mCurrentSize += length;
                        Log.i("whw", "mCurrentSize=" + mCurrentSize + "  length=" + length);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
