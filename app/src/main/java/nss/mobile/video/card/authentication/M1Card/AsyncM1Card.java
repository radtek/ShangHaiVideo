package nss.mobile.video.card.authentication.M1Card;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


import nss.mobile.video.card.authentication.CoreWise;
import nss.mobile.video.card.authentication.api.m1card.M1CardAPI;
import nss.mobile.video.card.authentication.utils.DataUtils;


public class AsyncM1Card extends Handler {
    private static final int READ_CARD_NUM = 1;
    private static final int SEND_PWD = 2;
    private static final int VALID_PWD = 3;
    private static final int WRITE_AT_POSITION_DATA = 4;
    private static final int READ_AT_POSITION_DATA = 5;
    private static final int UPDATE_PWD = 6;
    private static final String BLOCK = "block";
    private static final String KEY_TYPE = "key_type";
    private static final String NUM = "num";
    private static final String KEY_A = "key_a";
    private static final String KEY_B = "key_b";
    private static final String WRITE = "data";
    private Handler mWorkerThreadHandler;
    private M1CardAPI reader;


    public AsyncM1Card(Looper looper) {
        mWorkerThreadHandler = createHandler(looper);
        reader = new M1CardAPI();
    }

    protected Handler createHandler(Looper looper) {
        return new WorkerHandler(looper);
    }

    protected class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case READ_CARD_NUM:
                    M1CardAPI.Result result = reader.readCardNum();

                    //Result result = reader.writeBlock((byte) 0x00, (byte) 0x00, "ffffffff", "12345678");
                    //M1CardAPI.M1CardAPI.Result result = reader.opBlock((byte) 0x02, (byte) 0x00, (byte) 0x30, (byte) 0x00, (byte) 0x01, "ffffffffffff");

                    AsyncM1Card.this.obtainMessage(READ_CARD_NUM, result).sendToTarget();
                    break;
                case SEND_PWD:
                    break;
                case VALID_PWD:
                    break;
                case WRITE_AT_POSITION_DATA:
                    M1CardAPI.Result writeAtPositionResult = writeBlock(msg);
                    AsyncM1Card.this.obtainMessage(WRITE_AT_POSITION_DATA,
                            writeAtPositionResult).sendToTarget();
                    break;
                case READ_AT_POSITION_DATA:
                    M1CardAPI.Result readAtPositionResult = readBlock(msg);
                    AsyncM1Card.this.obtainMessage(READ_AT_POSITION_DATA, readAtPositionResult).sendToTarget();
                    break;
                case UPDATE_PWD:
                    M1CardAPI.Result updatePwdResult = updateCtrlBlock(msg);
                    AsyncM1Card.this.obtainMessage(UPDATE_PWD,
                            updatePwdResult).sendToTarget();
                    break;
                default:
                    break;
            }
        }
    }

    private OnReadCardNumListener onReadCardNumListener;//读卡号监听器
    private OnSendPwdListener onSendPwdListener;//发送密码监听器
    private OnValidPwdListener onValidPwdListener;//认证密码监听器
    private OnReadAtPositionListener onReadAtPositionListener;//读指定块号监听器
    private OnWriteAtPositionListener onWriteAtPositionListener;//写指定块号监听器
    private OnUpdatePwdListener onUpdatePwdListener;//修改密码监听器

    public void setOnReadCardNumListener(
            OnReadCardNumListener onReadCardNumListener) {
        this.onReadCardNumListener = onReadCardNumListener;
    }

    public void setOnSendPwdListener(
            OnSendPwdListener onSendPwdListener) {
        this.onSendPwdListener = onSendPwdListener;
    }

    public void setOnValidPwdListener(
            OnValidPwdListener onValidPwdListener) {
        this.onValidPwdListener = onValidPwdListener;
    }

    public void setOnReadAtPositionListener(
            OnReadAtPositionListener onReadAtPositionListener) {
        this.onReadAtPositionListener = onReadAtPositionListener;
    }

    public void setOnWriteAtPositionListener(
            OnWriteAtPositionListener onWriteAtPositionListener) {
        this.onWriteAtPositionListener = onWriteAtPositionListener;
    }

    public void setOnUpdatePwdListener(
            OnUpdatePwdListener onUpdatePwdListener) {
        this.onUpdatePwdListener = onUpdatePwdListener;
    }

    /**
     * 确认码 1: 成功 2：寻卡失败 3：验证失败 4:写卡失败 5：超时 6：其它异常
     *
     * @param comfirmationCode
     */
    public interface OnReadCardNumListener {
        public void onReadCardNumSuccess(String num);

        public void onReadCardNumFail(int comfirmationCode);
    }

    public interface OnReadAtPositionListener {
        public void onReadAtPositionSuccess(String cardNum, String data);

        public void onReadAtPositionFail(int comfirmationCode);
    }

    public interface OnSendPwdListener {
        public void onSendPwdSuccess(String cardNum, byte[][] data);

        public void onSendPwdFail(int comfirmationCode);
    }

    public interface OnValidPwdListener {
        public void onValidPwdSuccess(String cardNum, byte[][] data);

        public void onValidPwdFail(int comfirmationCode);
    }

    public interface OnWriteAtPositionListener {
        public void onWriteAtPositionSuccess(String num);

        public void onWriteAtPositionFail(int comfirmationCode);
    }

    public interface OnUpdatePwdListener {
        public void onUpdatePwdSuccess(String num);

        public void onUpdatePwdFail(int comfirmationCode);
    }

    public void readCardNum() {
        mWorkerThreadHandler.obtainMessage(READ_CARD_NUM).sendToTarget();
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case READ_CARD_NUM:
                M1CardAPI.Result numResult = (M1CardAPI.Result) msg.obj;
                Log.i("UUUUUUU", numResult.toString() + "---" + msg.obj.toString());
                if (onReadCardNumListener != null) {
                    if (numResult != null
                            && numResult.confirmationCode == M1CardAPI.Result.SUCCESS) {
                        onReadCardNumListener.onReadCardNumSuccess(numResult.num);
                    } else {
                        onReadCardNumListener
                                .onReadCardNumFail(numResult.confirmationCode);
                    }
                }
                break;
            case SEND_PWD:
                break;
            case VALID_PWD:
                break;
            case WRITE_AT_POSITION_DATA:
                if (onWriteAtPositionListener != null) {
                    M1CardAPI.Result result = (M1CardAPI.Result) msg.obj;
                    if (result != null && result.confirmationCode == M1CardAPI.Result.SUCCESS) {
                        onWriteAtPositionListener
                                .onWriteAtPositionSuccess(result.num);
                    } else {
                        onWriteAtPositionListener
                                .onWriteAtPositionFail(result.confirmationCode);
                    }
                }
                break;
            case READ_AT_POSITION_DATA:
                M1CardAPI.Result readPositionResult = (M1CardAPI.Result) msg.obj;
                String readPositionData = (String) readPositionResult.resultInfo;
                if (onReadAtPositionListener != null) {
                    if (readPositionData != null && !readPositionData.equals("")) {
                        onReadAtPositionListener.onReadAtPositionSuccess(readPositionResult.num, readPositionData);
                    } else {
                        onReadAtPositionListener.onReadAtPositionFail(readPositionResult.confirmationCode);
                    }
                }
                break;
            case UPDATE_PWD:
                M1CardAPI.Result updatePwdResult = (M1CardAPI.Result) msg.obj;
                if (updatePwdResult != null && updatePwdResult.confirmationCode == M1CardAPI.Result.SUCCESS) {
                    onUpdatePwdListener.onUpdatePwdSuccess(updatePwdResult.num);
                } else {
                    onUpdatePwdListener.onUpdatePwdFail(updatePwdResult.confirmationCode);
                }
                break;
            default:
                break;
        }
    }

    public void write(int block, int keyType, int num, String keyA, String keyB, String data) {
        Message msg = mWorkerThreadHandler.obtainMessage(WRITE_AT_POSITION_DATA);
        Bundle bundle = new Bundle();
        bundle.putInt(BLOCK, block);
        bundle.putInt(NUM, num);
        bundle.putInt(KEY_TYPE, keyType);
        bundle.putString(KEY_A, keyA);
        bundle.putString(KEY_B, keyB);
        bundle.putString(WRITE, data);
        msg.setData(bundle);
        msg.sendToTarget();
    }


    /**
     * U3、老海派写块方法
     *
     * @param msg
     * @return
     */
    private M1CardAPI.Result writeBlock(Message msg) {

        Bundle writeBundle = msg.getData();
        //获取块号
        int block = writeBundle.getInt(BLOCK);
        //执行次数
        int num = writeBundle.getInt(NUM);
        //获取卡片类型
        int keyType = writeBundle.getInt(KEY_TYPE);
        //获取密码A
        String keyA = writeBundle.getString(KEY_A);
        //获取密码B
        String keyB = writeBundle.getString(KEY_B);
        //写入数据
        String data = writeBundle.getString(WRITE);
        M1CardAPI.Result result = null;
        switch (CoreWise.getAndroidVersion()) {

            case CoreWise.deviceSysVersion.O:
                return write(msg);

            case CoreWise.deviceSysVersion.U:
                //S50

                if (keyType == 1) {
                    result = reader.writeBlock((byte) 0x00, (byte) block, keyA, data);
                } else if (keyType == 2) {
                    result = reader.writeBlock((byte) 0x01, (byte) block, keyA, data);
                }


                //result.num=reader.readCardNum().num;

                return result;

        }
        return null;
    }


    /**
     * 老海派写块方法
     *
     * @param msg
     * @return
     */
    private M1CardAPI.Result write(Message msg) {
        Bundle writeBundle = msg.getData();
        int block = writeBundle.getInt(BLOCK);//获取块号
        int num = writeBundle.getInt(NUM);//执行次数
        int keyType = writeBundle.getInt(KEY_TYPE);//获取卡片类型
        String keyA = writeBundle.getString(KEY_A);//获取密码A
        String keyB = writeBundle.getString(KEY_B);//获取密码B
        String data = writeBundle.getString(WRITE);//写入数据
        M1CardAPI.Result result = null;
        int time = 0;
        // 寻卡三次或验证3次都不通过返回
        while (time < 3) {
            result = reader.readCardNum();
            if (result.confirmationCode == M1CardAPI.Result.FIND_FAIL) {
                time++;
                continue;
            } else if (result.confirmationCode == M1CardAPI.Result.TIME_OUT) {
                return result;
            }

            if (!reader.validatePassword(block, keyType, keyA, keyB)) {
                result.confirmationCode = M1CardAPI.Result.VALIDATE_FAIL;
                time++;
                continue;
            } else {
                break;
            }
        }
        if (result.confirmationCode == M1CardAPI.Result.FIND_FAIL
                || result.confirmationCode == M1CardAPI.Result.VALIDATE_FAIL) {
            return result;
        }
        boolean writeResult = reader.write(block, num, data);
        reader.turnOff();
        if (writeResult) {
            result.confirmationCode = M1CardAPI.Result.SUCCESS;
        } else {
            result.confirmationCode = M1CardAPI.Result.WRITE_FAIL;
        }
        return result;
    }

    public void read(int block, int keyType, int num, String keyA, String keyB) {
        Message msg = mWorkerThreadHandler.obtainMessage(READ_AT_POSITION_DATA);
        Bundle bundle = new Bundle();
        bundle.putInt(BLOCK, block);
        bundle.putInt(NUM, num);
        bundle.putInt(KEY_TYPE, keyType);
        bundle.putString(KEY_A, keyA);
        bundle.putString(KEY_B, keyB);
        msg.setData(bundle);
        msg.sendToTarget();
    }


    /**
     * U3、老海派读块方法
     *
     * @param msg
     * @return
     */
    private M1CardAPI.Result readBlock(Message msg) {
        Bundle readBundle = msg.getData();
        //获取块号
        int block = (byte) readBundle.getInt(BLOCK);
        //执行次数
        int num = readBundle.getInt(NUM);
        //获取卡片类型
        int keyType = readBundle.getInt(KEY_TYPE);
        //获取密码A
        String keyA = readBundle.getString(KEY_A);
        //获取密码B
        String keyB = readBundle.getString(KEY_B);
        M1CardAPI.Result result = new M1CardAPI.Result();

        switch (CoreWise.getAndroidVersion()) {

            case CoreWise.deviceSysVersion.O:
                return read(msg);

            case CoreWise.deviceSysVersion.U:


                //S50
                if (keyType == 1) {
                    result = reader.readBlock((byte) 0x00, (byte) block, keyA);

                } else if (keyType == 2) {
                    result = reader.readBlock((byte) 0x01, (byte) block, keyA);
                }


                //result.num=reader.readCardNum().num;

                return result;

        }


        return null;
    }


    /**
     * 老海派读块
     *
     * @param msg
     * @return
     */
    private M1CardAPI.Result read(Message msg) {

        Bundle readBundle = msg.getData();
        //获取块号
        int block = readBundle.getInt(BLOCK);
        //执行次数
        int num = readBundle.getInt(NUM);
        //获取卡片类型
        int keyType = readBundle.getInt(KEY_TYPE);
        //获取密码A
        String keyA = readBundle.getString(KEY_A);
        //获取密码B
        String keyB = readBundle.getString(KEY_B);
        int time = 0;
        // 寻卡三次或验证3次都不通过返回
        M1CardAPI.Result result = null;
        while (time < 3) {
            result = reader.readCardNum();
            if (result.confirmationCode == M1CardAPI.Result.FIND_FAIL) {
                // return result;
                time++;
                continue;
            } else if (result.confirmationCode == M1CardAPI.Result.TIME_OUT) {
                return result;
            }

            if (!reader.validatePassword(block, keyType, keyA, keyB)) {
                result.confirmationCode = M1CardAPI.Result.VALIDATE_FAIL;
                // return result;
                time++;
                continue;
            } else {
                break;
            }

        }
        if (result.confirmationCode == M1CardAPI.Result.FIND_FAIL
                || result.confirmationCode == M1CardAPI.Result.VALIDATE_FAIL) {
            return result;
        }
        byte[][] data = reader.read(block, num);

        reader.turnOff();
        if (!dataIsNull(data)) {
            result.confirmationCode = M1CardAPI.Result.SUCCESS;
            result.resultInfo = DataUtils.bytesToHexString(data[0]);

        } else {
            result.confirmationCode = M1CardAPI.Result.READ_FAIL;
            result.resultInfo = "error";
        }
        return result;
    }


    /**
     * 更新控制块
     * @param msg
     * @return
     */
    public M1CardAPI.Result updateCtrlBlock(Message msg) {

        Bundle writeBundle = msg.getData();
        //获取块号
        int block = writeBundle.getInt(BLOCK);
        //执行次数
        int num = writeBundle.getInt(NUM);
        //获取卡片类型
        int keyType = writeBundle.getInt(KEY_TYPE);
        //获取密码A
        String keyA = writeBundle.getString(KEY_A);
        //获取密码B
        String keyB = writeBundle.getString(KEY_B);
        //写入数据 新密码
        String data = writeBundle.getString(WRITE);
        M1CardAPI.Result result = null;
        int time = 0;

        switch (CoreWise.getAndroidVersion()) {

            case CoreWise.deviceSysVersion.O:
                return updatePwd(msg);

            case CoreWise.deviceSysVersion.U:

                //0x00:S50;0x01:S70
                if (keyType == 1) {
                    result = reader.writeBlock((byte) M1CardAPI.M1S50, (byte) block, keyA,data+"ff078069"+data);

                } else if (keyType == 2) {
                    result = reader.writeBlock((byte) M1CardAPI.M1S70, (byte) block, keyA,data+"ff078069"+data);
                }
                return result;

        }

        return null;
    }


    public void updatePwd(int block, int keyType, int num, String keyA, String keyB, String data) {
        Message msg = mWorkerThreadHandler.obtainMessage(UPDATE_PWD);
        Bundle bundle = new Bundle();
        bundle.putInt(BLOCK, block);
        bundle.putInt(NUM, num);
        bundle.putInt(KEY_TYPE, keyType);
        bundle.putString(KEY_A, keyA);
        bundle.putString(KEY_B, keyB);
        bundle.putString(WRITE, data);
        msg.setData(bundle);
        msg.sendToTarget();
    }

    private M1CardAPI.Result updatePwd(Message msg) {
        Bundle writeBundle = msg.getData();
        int block = writeBundle.getInt(BLOCK);//获取块号
        int num = writeBundle.getInt(NUM);//执行次数
        int keyType = writeBundle.getInt(KEY_TYPE);//获取卡片类型
        String keyA = writeBundle.getString(KEY_A);//获取密码A
        String keyB = writeBundle.getString(KEY_B);//获取密码B
        String data = writeBundle.getString(WRITE);//写入数据 新密码
        M1CardAPI.Result result = null;
        int time = 0;
        // 寻卡三次或验证3次都不通过返回
        while (time < 3) {
            result = reader.readCardNum();
            if (result.confirmationCode == M1CardAPI.Result.FIND_FAIL) {
                time++;
                continue;
            } else if (result.confirmationCode == M1CardAPI.Result.TIME_OUT) {
                return result;
            }

            if (!reader.validatePassword(block, keyType, keyA, keyB)) {
                result.confirmationCode = M1CardAPI.Result.VALIDATE_FAIL;
                time++;
                continue;
            } else {
                break;
            }
        }
        if (result.confirmationCode == M1CardAPI.Result.FIND_FAIL
                || result.confirmationCode == M1CardAPI.Result.VALIDATE_FAIL) {
            return result;
        }
        boolean writeResult = reader.updatePwd(block, num, data, keyType);
        reader.turnOff();
        if (writeResult) {
            result.confirmationCode = M1CardAPI.Result.SUCCESS;
        } else {
            result.confirmationCode = M1CardAPI.Result.WRITE_FAIL;
        }
        return result;
    }

    private boolean dataIsNull(byte[][] data) {
        if (data == null) {
            return true;
        }
        for (int i = 0; i < data.length; i++) {
            if (data[i] == null) {
                return true;
            }
        }

        return false;
    }
}
