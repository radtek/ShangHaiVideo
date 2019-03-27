package nss.mobile.video.ui.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import cn.com.aratek.fp.Bione;
import cn.com.aratek.fp.FingerprintImage;
import cn.com.aratek.fp.FingerprintScanner;
import cn.com.aratek.util.Result;
import nss.mobile.video.MyApp;
import nss.mobile.video.card.authentication.api.sfz.ParseSFZAPI;
import nss.mobile.video.card.authentication.aratek.SwitchUtil;
import nss.mobile.video.R;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2019/1/24
 *
 * @author ql
 */
public class FingerPrintHelper {
    private static final String FP_DB_PATH = "/sdcard/fp.db";

    private static final String TAG = "FingerPrintHelper";
    private FingerprintScanner mScanner;
    private boolean mDeviceOpened;

    private static final int MSG_SHOW_ERROR = 0;
    private static final int MSG_SHOW_INFO = 1;
    private static final int MSG_UPDATE_IMAGE = 2;
    private static final int MSG_UPDATE_TEXT = 3;
    private static final int MSG_UPDATE_BUTTON = 4;
    private static final int MSG_UPDATE_SN = 5;
    private static final int MSG_UPDATE_FW_VERSION = 6;
    private static final int MSG_SHOW_PROGRESS_DIALOG = 7;
    private static final int MSG_DISMISS_PROGRESS_DIALOG = 8;

    private int mId;
    private ParseSFZAPI.People mPeopleCheck;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW_ERROR: {
                    String information = ((Bundle) msg.obj).getString("information");
                    String details = ((Bundle) msg.obj).getString("details");
                    mOnFingerCheckListener.onFingerCheckFailed(information, details);

                    break;
                }
                case MSG_SHOW_INFO: {
                    String information = ((Bundle) msg.obj).getString("information");
                    String details = ((Bundle) msg.obj).getString("details");
                    mOnFingerCheckListener.onFingerCheckSuccess(information, details);
                    break;
                }
                case MSG_UPDATE_IMAGE: {
                    Bitmap obj = (Bitmap) msg.obj;
                    break;
                }
                case MSG_UPDATE_TEXT: {
                    String[] texts = (String[]) msg.obj;
//                    mCaptureTime.setText(texts[0]);
//                    mExtractTime.setText(texts[1]);
//                    mGeneralizeTime.setText(texts[2]);
//                    mVerifyTime.setText(texts[3]);
                    break;
                }
                case MSG_UPDATE_BUTTON: {
                    Boolean enable = (Boolean) msg.obj;
                    if (mDeviceOpened) {
                        //当前指纹设备是启动状态
                        mOnFingerCheckListener.onFingerOpen();
                    } else {
                        //当前指纹设备是关闭状态
                        mOnFingerCheckListener.onFingerClose();

                    }
                    break;
                }

                case MSG_UPDATE_SN: {
                    String obj = (String) msg.obj;
                    break;
                }

                case MSG_UPDATE_FW_VERSION: {
                    String obj = (String) msg.obj;
                    break;
                }

                case MSG_SHOW_PROGRESS_DIALOG: {
                    String[] info = (String[]) msg.obj;
                    String title = info[0];
                    String message = info[1];
                    break;
                }

                case MSG_DISMISS_PROGRESS_DIALOG: {
                    break;

                }
            }
        }
    };
    private FingerprintTask mTask;
    private OnFingerCheckListener mOnFingerCheckListener;

    public interface OnFingerCheckListener {
        void onFingerCheckSuccess(String title, String msg);

        void onFingerCheckFailed(String information, String details);

        void onFingerOpen();

        void onFingerClose();
    }

    public OnFingerCheckListener getOnFingerCheckListener() {
        return mOnFingerCheckListener;
    }

    public void setOnFingerCheckListener(OnFingerCheckListener l) {
        this.mOnFingerCheckListener = l;
    }

    public void init(Context context) {
        mScanner = new FingerprintScanner(context);
        SwitchUtil.getInstance().openUSB();

    }


    public void openDevice(Context context) {
        synchronized (FingerPrintHelper.this) {
//                    showProgressDialog(getString(R.string.loading), getString(R.string.preparing_device));
            int error;
            if ((error = mScanner.open()) != FingerprintScanner.RESULT_OK) {
                Log.i(TAG, "------------" + error);
                //Toast.makeText(AratekFingerFragment.this, "------"+error, Toast.LENGTH_SHORT).show();
                showError(getString(R.string.fingerprint_device_open_failed), getFingerprintErrorString(error));

            } else {
                showInformation(getString(R.string.fingerprint_device_open_success), null);
                mDeviceOpened = true;
                enableControl(true);
            }
            if ((error = Bione.initialize(context, FP_DB_PATH)) != Bione.RESULT_OK) {
//                        showError(getString(R.string.algorithm_initialization_failed), getFingerprintErrorString(error));
            }
            Log.i(TAG, "Fingerprint algorithm version: " + Bione.getVersion());
            dismissProgressDialog();
        }
    }

    public void closeDevice() {

        synchronized (FingerPrintHelper.this) {
            showProgressDialog(getString(R.string.loading), getString(R.string.closing_device));
            enableControl(false);
            int error;
            if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED) {
                mTask.cancel(false);
                mTask.waitForDone();
            }
            if ((error = mScanner.close()) != FingerprintScanner.RESULT_OK) {
                showError(getString(R.string.fingerprint_device_close_failed), getFingerprintErrorString(error));
            } else {
                showInformation(getString(R.string.fingerprint_device_close_success), null);
            }
            if ((error = Bione.exit()) != Bione.RESULT_OK) {
                showError(getString(R.string.algorithm_cleanup_failed), getFingerprintErrorString(error));
            }
            mDeviceOpened = false;
            dismissProgressDialog();
        }

    }

    /**
     * 注册
     */
    public void enroll() {
        mTask = new FingerprintTask();
        mTask.execute("enroll");
    }


    /**
     * 比对
     */
    public void verify() {
        mTask = new FingerprintTask();
        mTask.execute("verify");
    }

    /**
     * 验证用户指纹信息
     */
    public void checkUserFinger(ParseSFZAPI.People people) {
        mPeopleCheck = people;
        mTask = new FingerprintTask();
        mTask.execute("checkFingerPeople");
    }

    /**
     * 鉴定
     */
    public void identify() {
        mTask = new FingerprintTask();
        mTask.execute("identify");
    }

    private void clearFingerprintDatabase() {
        int error = Bione.clear();
        if (error == Bione.RESULT_OK) {
            showInformation(getString(R.string.clear_fingerprint_database_success), null);
        } else {
            showError(getString(R.string.clear_fingerprint_database_failed), getFingerprintErrorString(error));
        }
    }


    private void showFingerprintImage() {
        mTask = new FingerprintTask();
        mTask.execute("show");
    }

    private void showProgressDialog(String title, String message) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_PROGRESS_DIALOG, new String[]{title, message}));
    }

    private void dismissProgressDialog() {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_DISMISS_PROGRESS_DIALOG));
    }

    private void showError(String info, String details) {
        Bundle bundle = new Bundle();
        bundle.putString("information", info);
        bundle.putString("details", details);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_ERROR, bundle));
    }

    private void showInformation(String info, String details) {
        Bundle bundle = new Bundle();
        bundle.putString("information", info);
        bundle.putString("details", details);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_INFO, bundle));
    }

    private String getFingerprintErrorString(int error) {
        int strid;
        switch (error) {
            case FingerprintScanner.RESULT_OK:
                strid = R.string.operation_successful;
                break;
            case FingerprintScanner.RESULT_FAIL:
                strid = R.string.error_operation_failed;
                break;
            case FingerprintScanner.WRONG_CONNECTION:
                strid = R.string.error_wrong_connection;
                break;
            case FingerprintScanner.DEVICE_BUSY:
                strid = R.string.error_device_busy;
                break;
            case FingerprintScanner.DEVICE_NOT_OPEN:
                strid = R.string.error_device_not_open;
                break;
            case FingerprintScanner.TIMEOUT:
                strid = R.string.error_timeout;
                break;
            case FingerprintScanner.NO_PERMISSION:
                strid = R.string.error_no_permission;
                break;
            case FingerprintScanner.WRONG_PARAMETER:
                strid = R.string.error_wrong_parameter;
                break;
            case FingerprintScanner.DECODE_ERROR:
                strid = R.string.error_decode;
                break;
            case FingerprintScanner.INIT_FAIL:
                strid = R.string.error_initialization_failed;
                break;
            case FingerprintScanner.UNKNOWN_ERROR:
                strid = R.string.error_unknown;
                break;
            case FingerprintScanner.NOT_SUPPORT:
                strid = R.string.error_not_support;
                break;
            case FingerprintScanner.NOT_ENOUGH_MEMORY:
                strid = R.string.error_not_enough_memory;
                break;
            case FingerprintScanner.DEVICE_NOT_FOUND:
                strid = R.string.error_device_not_found;
                break;
            case FingerprintScanner.DEVICE_REOPEN:
                strid = R.string.error_device_reopen;
                break;
            case FingerprintScanner.NO_FINGER:
                strid = R.string.error_no_finger;
                break;
            case Bione.INITIALIZE_ERROR:
                strid = R.string.error_algorithm_initialization_failed;
                break;
            case Bione.INVALID_FEATURE_DATA:
                strid = R.string.error_invalid_feature_data;
                break;
            case Bione.BAD_IMAGE:
                strid = R.string.error_bad_image;
                break;
            case Bione.NOT_MATCH:
                strid = R.string.error_not_match;
                break;
            case Bione.LOW_POINT:
                strid = R.string.error_low_point;
                break;
            case Bione.NO_RESULT:
                strid = R.string.error_no_result;
                break;
            case Bione.OUT_OF_BOUND:
                strid = R.string.error_out_of_bound;
                break;
            case Bione.DATABASE_FULL:
                strid = R.string.error_database_full;
                break;
            case Bione.LIBRARY_MISSING:
                strid = R.string.error_library_missing;
                break;
            case Bione.UNINITIALIZE:
                strid = R.string.error_algorithm_uninitialize;
                break;
            case Bione.REINITIALIZE:
                strid = R.string.error_algorithm_reinitialize;
                break;
            case Bione.REPEATED_ENROLL:
                strid = R.string.error_repeated_enroll;
                break;
            case Bione.NOT_ENROLLED:
                strid = R.string.error_not_enrolled;
                break;
            default:
                strid = R.string.error_other;
                break;
        }
        return getString(strid);
    }

    /**
     * 注册指纹
     *
     * @param fpFeat 指纹信息
     */
    public Integer regFinger(byte[] fpFeat) {
        Result res = Bione.makeTemplate(fpFeat, fpFeat, fpFeat);
        if (res.error != Bione.RESULT_OK) {
            showError(getString(R.string.enroll_failed_because_of_make_template), getFingerprintErrorString(res.error));
            return null;
        }
        byte[] fpTemp = (byte[]) res.data;

        int id = Bione.getFreeID();
        if (id < 0) {
            showError(getString(R.string.enroll_failed_because_of_get_id), getFingerprintErrorString(id));
            return null;
        }
        int ret = Bione.enroll(id, fpTemp);
        if (ret != Bione.RESULT_OK) {
            showError(getString(R.string.enroll_failed_because_of_error), getFingerprintErrorString(ret));
            return null;
        }
        mId = id;
        return id;
    }

    private void updateSingerTestText(long captureTime, long extractTime, long generalizeTime, long verifyTime) {
        String[] texts = new String[4];
        if (captureTime < 0) {
            texts[0] = getString(R.string.not_done);
        } else if (captureTime < 1) {
            texts[0] = "< 1ms";
        } else {
            texts[0] = captureTime + "ms";
        }

        if (extractTime < 0) {
            texts[1] = getString(R.string.not_done);
        } else if (extractTime < 1) {
            texts[1] = "< 1ms";
        } else {
            texts[1] = extractTime + "ms";
        }

        if (generalizeTime < 0) {
            texts[2] = getString(R.string.not_done);
        } else if (generalizeTime < 1) {
            texts[2] = "< 1ms";
        } else {
            texts[2] = generalizeTime + "ms";
        }

        if (verifyTime < 0) {
            texts[3] = getString(R.string.not_done);
        } else if (verifyTime < 1) {
            texts[3] = "< 1ms";
        } else {
            texts[3] = verifyTime + "ms";
        }

        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_TEXT, texts));
    }

    private void updateFingerprintImage(FingerprintImage fi) {
        byte[] fpBmp = null;
        Bitmap bitmap;
        if (fi == null || (fpBmp = fi.convert2Bmp()) == null || (bitmap = BitmapFactory.decodeByteArray(fpBmp, 0, fpBmp.length)) == null) {
            bitmap = BitmapFactory.decodeResource(MyApp.getInstance().getResources(), R.drawable.finger_gray);
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_IMAGE, bitmap));
    }

    private void enableControl(boolean enable) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_BUTTON, enable));
    }


    private class FingerprintTask extends AsyncTask<String, Integer, Void> {
        private boolean mIsDone = false;

        /**
         * 这个方法是在执行异步任务之前的时候执行，并且是在UI Thread当中执行的，通常我们在这个方法里做一些UI控件的初始化的操作，例如弹出要给ProgressDialog
         */
        @Override
        protected void onPreExecute() {
            enableControl(false);
        }

        /**
         * 处理异步任务的方法
         *
         * @param params
         * @return
         */
        @Override
        protected Void doInBackground(String... params) {
            long startTime, captureTime = -1, extractTime = -1, generalizeTime = -1, verifyTime = -1;
            FingerprintImage fi = null;
            byte[] fpFeat = null, fpTemp = null;
            Result res;

            do {
                if (params[0].equals("show") || params[0].equals("enroll") || params[0].equals("verify") || params[0].equals("identify")) {

                    showProgressDialog(getString(R.string.loading), getString(R.string.press_finger));
                    int capRetry = 0;

                    mScanner.prepare();
                    do {
                        startTime = System.currentTimeMillis();

                        res = mScanner.capture();

                        captureTime = System.currentTimeMillis() - startTime;

                        fi = (FingerprintImage) res.data;

                        int quality;

                        if (fi != null) {

                            quality = Bione.getFingerprintQuality(fi);
                            Log.i(TAG, "Fingerprint image quality is " + quality);
                            if (quality < 50 && capRetry < 3 && !isCancelled()) {
                                capRetry++;
                                continue;
                            }
                        }

                        if (res.error != FingerprintScanner.NO_FINGER || isCancelled()) {
                            break;
                        }

                    } while (true);
                    mScanner.finish();

                    if (isCancelled()) {
                        break;
                    }

                    if (res.error != FingerprintScanner.RESULT_OK) {
                        showError(getString(R.string.capture_image_failed), getFingerprintErrorString(res.error));
                        break;
                    }
                    updateFingerprintImage(fi);
                }

                if (params[0].equals("show")) {
                    showInformation(getString(R.string.capture_image_success), null);
                } else if (params[0].equals("enroll")) {
                    showProgressDialog(getString(R.string.loading), getString(R.string.enrolling));
                } else if (params[0].equals("verify")) {
                    showProgressDialog(getString(R.string.loading), getString(R.string.verifying));
                } else if (params[0].equals("identify")) {
                    showProgressDialog(getString(R.string.loading), getString(R.string.identifying));
                }

                if (params[0].equals("enroll") || params[0].equals("verify") || params[0].equals("identify")) {
                    startTime = System.currentTimeMillis();
                    res = Bione.extractFeature(fi);
                    extractTime = System.currentTimeMillis() - startTime;
                    if (res.error != Bione.RESULT_OK) {
                        showError(getString(R.string.enroll_failed_because_of_extract_feature), getFingerprintErrorString(res.error));
                        break;
                    }
                    fpFeat = (byte[]) res.data;
                }

                if (params[0].equals("enroll")) {//注册
                    startTime = System.currentTimeMillis();
                    res = Bione.makeTemplate(fpFeat, fpFeat, fpFeat);
                    generalizeTime = System.currentTimeMillis() - startTime;
                    if (res.error != Bione.RESULT_OK) {
                        showError(getString(R.string.enroll_failed_because_of_make_template), getFingerprintErrorString(res.error));
                        break;
                    }
                    fpTemp = (byte[]) res.data;

                    int id = Bione.getFreeID();
                    if (id < 0) {
                        showError(getString(R.string.enroll_failed_because_of_get_id), getFingerprintErrorString(id));
                        break;
                    }
                    int ret = Bione.enroll(id, fpTemp);
                    if (ret != Bione.RESULT_OK) {
                        showError(getString(R.string.enroll_failed_because_of_error), getFingerprintErrorString(ret));
                        break;
                    }
                    mId = id;
                    showInformation(getString(R.string.enroll_success), getString(R.string.enrolled_id, id));
                } else if (params[0].equals("verify")) {//比对
                    startTime = System.currentTimeMillis();
                    res = Bione.verify(mId, fpFeat);
                    verifyTime = System.currentTimeMillis() - startTime;
                    if (res.error != Bione.RESULT_OK) {
                        showError(getString(R.string.verify_failed_because_of_error), getFingerprintErrorString(res.error));
                        break;
                    }
                    if ((Boolean) res.data) {
                        showInformation(getString(R.string.fingerprint_match), getString(R.string.fingerprint_similarity, res.arg1));
                    } else {
                        showError(getString(R.string.fingerprint_not_match), getString(R.string.fingerprint_similarity, res.arg1));
                    }
                } else if (params[0].equals("identify")) {
                    startTime = System.currentTimeMillis();
                    int id = Bione.identify(fpFeat);
                    verifyTime = System.currentTimeMillis() - startTime;
                    if (id < 0) {
                        showError(getString(R.string.identify_failed_because_of_error), getFingerprintErrorString(id));
                        break;
                    }
                    showInformation(getString(R.string.identify_match), getString(R.string.matched_id, id));
                } else if (params[0].equals("checkFingerPeople")) {
                    startTime = System.currentTimeMillis();
                    byte[] one = new byte[512];
                    byte[] two = new byte[512];

                    System.arraycopy(mPeopleCheck.getModel(), 0, one, 0, 512);
                    System.arraycopy(mPeopleCheck.getModel(), 512, two, 0, 512);
                    Result resultO = Bione.idcardVerify(one, fpFeat);
                    Result resultT = Bione.idcardVerify(two, fpFeat);

                    verifyTime = System.currentTimeMillis() - startTime;
                    if (resultO.error == Bione.RESULT_OK) {
                        Boolean okO = (Boolean) resultO.data;
                        if (okO) {
                            showInformation(getString(R.string.fingerprint_match), getString(R.string.fingerprint_similarity, resultO.arg1));
                        } else {
                            showError(getString(R.string.fingerprint_not_match), getString(R.string.fingerprint_similarity, resultO.arg1));
                        }
                    } else if (resultT.error == Bione.RESULT_OK) {
                        Boolean okT = (Boolean) resultT.data;
                        if (okT) {
                            showInformation(getString(R.string.fingerprint_match), getString(R.string.fingerprint_similarity, resultT.arg1));
                        } else {
                            showError(getString(R.string.fingerprint_not_match), getString(R.string.fingerprint_similarity, resultT.arg1));
                        }
                    } else {
                        showError(getString(R.string.verify_failed_because_of_error), getFingerprintErrorString(resultT.error));

                    }
                }
            } while (false);

            updateSingerTestText(captureTime, extractTime, generalizeTime, verifyTime);
            enableControl(true);
            dismissProgressDialog();
            mIsDone = true;
            return null;
        }

        /**
         * 当我们的异步任务执行完之后，就会将结果返回给这个方法，这个方法也是在UI Thread当中调用的，我们可以将返回的结果显示在UI控件上
         *
         * @param result
         */
        @Override
        protected void onPostExecute(Void result) {
        }

        /**
         * 这个方法也是在UI Thread当中执行的，我们在异步任务执行的时候，有时候需要将执行的进度返回给我们的UI界面
         *
         * @param values
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
        }

        @Override
        protected void onCancelled() {
        }

        public void waitForDone() {
            while (!mIsDone) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private String getString(int fingerprint_similarity, int arg1) {
        return MyApp.getInstance().getString(fingerprint_similarity, arg1);
    }

    private String getString(int s) {
        return MyApp.getInstance().getString(s);
    }

}
