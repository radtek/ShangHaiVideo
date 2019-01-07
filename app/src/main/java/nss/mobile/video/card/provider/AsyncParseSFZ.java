package nss.mobile.video.card.provider;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import cn.com.aratek.fp.Bione;
import cn.com.aratek.fp.FingerprintImage;
import cn.com.aratek.fp.FingerprintScanner;
import nss.mobile.video.MyApp;
import nss.mobile.video.R;

/**
 * 异步读取身份证类
 */
public class AsyncParseSFZ extends Handler {

    private static final String TAG = "AsyncParseSFZ";

    private static final int READ_CARD_ID = 999;
    private static final int READ_SFZ = 1000;
    private static final int READ_MODULE = 2000;
    private static final int FIND_CARD_SUCCESS = 1001;
    private static final int FIND_CARD_FAIL = 1002;
    private static final int FIND_MODULE_SUCCESS = 1003;
    private static final int FIND_MODULE_FAIL = 1004;
    private static final int DATA_SIZE = 1295;

    public static final int OPEN_FINGERSCANNER_FAIL = 1111;
    public static final int OPEN_FINGERSCANNER_SUCCESS = 1112;
    public static final int BIONE_INIT_SUCCESS = 1113;
    public static final int BIONE_INIT_FAIL = 1114;

    public static final int CLOSE_FINGERSCANNER_FAIL = 2333;
    public static final int CLOSE_FINGERSCANNER_SUCCESS = 2334;

    public static final int BIONE_EXIT_SUCCESS = 2336;
    public static final int BIONE_EXIT_FAIL = 2335;

    public static final int IDCARD_EXTRACTFEATURE_ERROR = 33331;
    public static final int IDCARD_MAKETEMPLATE_ERROR = 33332;
    public static final int IDCARD_IDCARDVERIFY_ERROR = 33333;
    public static final int IDCARD_IDCARDVERIFY_SUCCESS = 3333;


    private ParseSFZAPI parseAPI;

    private Handler mWorkerThreadHandler;

    private OnReadSFZListener onReadSFZListener;

    private OnReadModuleListener onReadModuleListener;

    private OnReadCardIDListener onReadCardIDListener;

    private Context mContext;

    /**
     * 构造方法
     *
     * @param looper
     * @param context 上下文
     */
    public AsyncParseSFZ(Looper looper, Context context) {
        mContext = context;
        parseAPI = new ParseSFZAPI(context);
        mWorkerThreadHandler = createHandler(looper);

    }


    /**
     * 该方法废弃
     *
     * @param onReadModuleListener
     */
    @Deprecated
    public void setOnReadModuleListener(OnReadModuleListener onReadModuleListener) {
        this.onReadModuleListener = onReadModuleListener;
    }

    /**
     * 读取身份证监听函数
     *
     * @param onReadSFZListener
     */
    public void setOnReadSFZListener(OnReadSFZListener onReadSFZListener) {
        this.onReadSFZListener = onReadSFZListener;
    }

    /**
     * 读取身份证id监听函数
     *
     * @param onReadCardIDListener
     */
    public void setOnReadCardIDListener(OnReadCardIDListener onReadCardIDListener) {
        this.onReadCardIDListener = onReadCardIDListener;
    }


    /**
     * 读取身份证模块接口
     */
    @Deprecated
    public interface OnReadModuleListener {
        void onReadSuccess(String module);

        void onReadFail(int confirmationCode);
    }

    /**
     * 读取身份证监听接口
     */
    public interface OnReadSFZListener {

        /**
         * 读取身份证成功
         *
         * @param people
         */
        void onReadSuccess(ParseSFZAPI.People people);

        /**
         * 读取身份证失败
         *
         * @param confirmationCode
         */
        void onReadFail(int confirmationCode);
    }

    /**
     * 读取身份证id接口
     */
    public interface OnReadCardIDListener {

        /**
         * 读取身份证id成功
         *
         * @param id
         */
        void onReadSuccess(String id);

        /**
         * 读取身份证id失败
         */
        void onReadFail();
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
                case READ_SFZ:
                    ParseSFZAPI.Result resultSFZ = null;

                    switch (CoreWise.getModel()) {
                        case CoreWise.device.CFON640:
                            Log.i(TAG, "---CFON640---");

                            resultSFZ = parseAPI.read(msg.arg1);
                            break;
                        case CoreWise.device.A370:
                            Log.i(TAG, "---A370---");

                            resultSFZ = parseAPI.read(msg.arg1);
                            break;
                        case CoreWise.device.U3_640:
                            Log.i(TAG, "---U3_640---");

                            resultSFZ = parseAPI.read();
                            break;

                        case CoreWise.device.U3_A370:
                            Log.i(TAG, "---U3_A370---");

                            resultSFZ = parseAPI.read();
                            break;
                    }


                    if (resultSFZ.data != null) {
                        //Toast.makeText(mContext, DataUtils.toHexString(resultSFZ.data), Toast.LENGTH_SHORT).show();
                    }
                    if (resultSFZ.confirmationCode == ParseSFZAPI.Result.SUCCESS) {
                        AsyncParseSFZ.this.obtainMessage(FIND_CARD_SUCCESS, resultSFZ.resultInfo).sendToTarget();
                    } else {
                        AsyncParseSFZ.this.obtainMessage(FIND_CARD_FAIL, resultSFZ.confirmationCode, -1).sendToTarget();
                    }
                    break;
                case READ_MODULE:
                    ParseSFZAPI.Result resultModule = parseAPI.readModule();
                    Log.i("whw", "module=" + resultModule.resultInfo);
                    if (resultModule.confirmationCode == ParseSFZAPI.Result.SUCCESS) {
                        AsyncParseSFZ.this.obtainMessage(FIND_MODULE_SUCCESS, resultModule.resultInfo).sendToTarget();
                    } else {
                        AsyncParseSFZ.this.obtainMessage(FIND_MODULE_FAIL, resultModule.confirmationCode, -1)
                                .sendToTarget();
                    }
                    break;
                case READ_CARD_ID:


                    switch (CoreWise.getModel()) {
                        case CoreWise.device.CFON640:
                            AsyncParseSFZ.this.obtainMessage(READ_CARD_ID, parseAPI.readCardID()).sendToTarget();
                            break;
                        case CoreWise.device.A370:
                            AsyncParseSFZ.this.obtainMessage(READ_CARD_ID, parseAPI.readCardID()).sendToTarget();
                            break;
                        case CoreWise.device.U3_640:
                            AsyncParseSFZ.this.obtainMessage(READ_CARD_ID, parseAPI.readCardID_U3()).sendToTarget();
                            break;
                        case CoreWise.device.U3_A370:
                            AsyncParseSFZ.this.obtainMessage(READ_CARD_ID, parseAPI.readCardID_U3()).sendToTarget();
                            break;
                    }

                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 根据类型读二三代证
     *
     * @param cardType
     */
    public void readSFZ(int cardType) {
        mWorkerThreadHandler.obtainMessage(READ_SFZ, cardType, -1).sendToTarget();
    }


    /**
     * 读取模块号
     */
    @Deprecated
    public void readModuleNum() {
        mWorkerThreadHandler.obtainMessage(READ_MODULE).sendToTarget();
    }

    /**
     * 读身份证id
     */
    public void readCardID() {
        mWorkerThreadHandler.obtainMessage(READ_CARD_ID).sendToTarget();
    }


    /**
     * Handle消息处理
     *
     * @param msg
     */
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        switch (msg.what) {
            case FIND_CARD_SUCCESS:
                if (onReadSFZListener != null) {
                    onReadSFZListener.onReadSuccess((ParseSFZAPI.People) msg.obj);
                }
                break;

            case FIND_CARD_FAIL:
                if (onReadSFZListener != null) {
                    onReadSFZListener.onReadFail(msg.arg1);
                }
                break;

            case FIND_MODULE_SUCCESS:
                if (onReadModuleListener != null) {
                    onReadModuleListener.onReadSuccess((String) msg.obj);
                }
                break;

            case FIND_MODULE_FAIL:
                if (onReadModuleListener != null) {
                    onReadModuleListener.onReadFail(msg.arg1);
                }
                break;

            case READ_CARD_ID:
                if (onReadCardIDListener != null) {
                    String id = (String) msg.obj;
                    if (!TextUtils.isEmpty(id)) {
                        onReadCardIDListener.onReadSuccess(id);
                    } else {
                        onReadCardIDListener.onReadFail();
                    }
                }
                break;

            default:
                break;
        }
    }


    private String FP_DB_PATH = "/sdcard/cn.com.aratek.fp.db";

    public void setFP_DB_PATH(String FP_DB_PATH) {
        this.FP_DB_PATH = FP_DB_PATH;
    }


    /**
     * 打开指纹设备
     * @param context 上下文
     * @param mScanner
     */
    public void openFingerDevice(final Context context, final FingerprintScanner mScanner) {

        new Thread() {
            @Override
            public void run() {
                synchronized (context) {
                    int error;
                    if ((error = mScanner.open()) != FingerprintScanner.RESULT_OK) {
                        Log.i(TAG, "------mScanner.open------" + error);
                        //mHandler.sendEmptyMessage(OPEN_FINGERSCANNER_FAIL);

                    } else {
                        //mHandler.sendEmptyMessage(OPEN_FINGERSCANNER_SUCCESS);

                    }
                    if ((error = Bione.initialize(context, FP_DB_PATH)) != Bione.RESULT_OK) {
                        Log.i(TAG, "------Bione.initialize------" + context.getString(R.string.algorithm_initialization_failed) + error);
                        //mHandler.sendEmptyMessage(BIONE_INIT_FAIL);
                    } else {
                        //mHandler.sendEmptyMessage(BIONE_INIT_SUCCESS);

                    }
                }
            }
        }.start();
    }

    /**
     * 关闭指纹设备
     * @param context 上下文
     * @param mScanner
     */
    public void closeFingerDevice(final Context context, final FingerprintScanner mScanner) {
        new Thread() {
            @Override
            public void run() {
                synchronized (context) {
                    int error;
                    if ((error = mScanner.close()) != FingerprintScanner.RESULT_OK) {
                        Log.i(TAG, context.getString(R.string.fingerprint_device_close_failed) + "------mScanner.close------" + error);
                        //mHandler.sendEmptyMessage(CLOSE_FINGERSCANNER_FAIL);

                    } else {
                        Log.i(TAG, context.getString(R.string.fingerprint_device_close_success));
                        //mHandler.sendEmptyMessage(CLOSE_FINGERSCANNER_SUCCESS);
                    }
                    if ((error = Bione.exit()) != Bione.RESULT_OK) {
                        Log.i(TAG, context.getString(R.string.algorithm_cleanup_failed) + "------Bione.exit------" + error);
                        //mHandler.sendEmptyMessage(BIONE_EXIT_FAIL);

                    } else {
                        Log.i(TAG, context.getString(R.string.algorithm_cleanup_success) + "------------");

                        //mHandler.sendEmptyMessage(BIONE_EXIT_SUCCESS);

                    }

                }
            }
        }.start();
    }


    /**
     * 比对指纹设备采集的指纹和身份证采集的指纹
     * @param mHandler
     * @param mScanner 指纹设备
     * @param people 读取身份证的信息
     */
    public void idCardVerify(final Handler mHandler, final FingerprintScanner mScanner, final ParseSFZAPI.People people) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                int capRetry = 0;
                Log.i("liwei","capRetry1:"+capRetry);

                FingerprintImage fi; //指纹图像

                mScanner.prepare();//准备采集指纹
                do {
                    cn.com.aratek.util.Result res = mScanner.capture();//采集一帧FingerprintImage类型指纹图像
                    Log.i("liwei","res:"+res);

                    fi = (FingerprintImage) res.data;
                     Log.i("liwei","fi:"+fi);
                    int quality;

                    if (fi != null) {
                        Log.i("liwei","fi:"+fi);
                        quality = Bione.getFingerprintQuality(fi);
                        Log.i(TAG, "指纹质量:" + quality);
                        if (quality < 50 && capRetry < 3) {
                            Log.i("liwei","capRetry2:"+capRetry);
                            capRetry++;
                            continue;
                        }
                    }

                    Log.i("liwei","res.error:"+res.error);
                    Log.i("liwei","FingerprintScanner.NO_FINGER:"+FingerprintScanner.NO_FINGER);
                    if (res.error!= FingerprintScanner.NO_FINGER&&null!=fi) {
                        Log.i("liwei","res.error2:"+res.error);
                        break;
                    }
                } while (true);
                Log.i("liwei","fi3:"+fi);
               MyApp.BitmapFinger=fi.convert2Bmp();

                mScanner.finish();//采集指纹结束
                byte[] convert2Bmp = fi.convert2Bmp();
                cn.com.aratek.util.Result res = Bione.extractFeature(fi);//从FingerprintImage指纹图像提取特征
                Log.i("liwei","res2:"+res);

                if (res.error != Bione.RESULT_OK) {
                    //showError(getString(R.string.enroll_failed_because_of_extract_feature), getFingerprintErrorString(res.error));
                    //break;
                    Message msg = Message.obtain();
                    msg.obj = res.error;
                    msg.what = IDCARD_EXTRACTFEATURE_ERROR;
                    mHandler.sendMessage(msg);


                }
                byte[] fpFeat = (byte[]) res.data;

                res = Bione.makeTemplate(fpFeat, fpFeat, fpFeat);//从同一手指的三个指纹特征合成模板数据

                if (res.error != Bione.RESULT_OK) {
                    Message msg = Message.obtain();
                    msg.obj = res.error;
                    msg.what = IDCARD_MAKETEMPLATE_ERROR;
                    mHandler.sendMessage(msg);
                }

                byte[] fpTemp = (byte[]) res.data;

                byte[] one = new byte[512];
                byte[] two = new byte[512];

                System.arraycopy(people.getModel(), 0, one, 0, 512);
                System.arraycopy(people.getModel(), 512, two, 0, 512);

                cn.com.aratek.util.Result oneResult = Bione.idcardVerify(one, fpTemp);//比对身份证指纹和设备取到的指纹

                cn.com.aratek.util.Result twoResult = Bione.idcardVerify(two, fpTemp);//比对身份证指纹和设备取到的指纹


                if (oneResult.error == Bione.INITIALIZE_ERROR || twoResult.error == Bione.INITIALIZE_ERROR) {
                    Message msg = Message.obtain();
                    msg.obj = oneResult.error;
                    msg.arg1 = twoResult.error;

                    msg.what = IDCARD_IDCARDVERIFY_ERROR;
                    mHandler.sendMessage(msg);
                    Log.i(TAG,"---one---"+oneResult.error+"\n---two---"+twoResult.error);
                } else {

                    Message msg = Message.obtain();

                    boolean isOne = (boolean) oneResult.data;
                    boolean isTwo = (boolean) twoResult.data;

                    msg.obj = isOne || isTwo;
                    msg.what = IDCARD_IDCARDVERIFY_SUCCESS;
                    mHandler.sendMessage(msg);
                }

            }
        }).start();

    }

}