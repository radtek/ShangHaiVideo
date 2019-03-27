package nss.mobile.video.ui.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bio.fingerprint.ID_Fpr;
import com.bio.fingerprint.ID_FprCap;

import org.greenrobot.eventbus.EventBus;

import nss.mobile.video.card.authentication.api.sfz.ParseSFZAPI;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2019/2/25
 *
 * @author ql
 */
public class FingerPrintBHMHelper implements IFingerPrint {
    private static final String TAG = "FingerPrintBHMHelper";
    private static final int ERRORINFOS = 0;

    private static final int W_CHECK_SUCCESS = 100;//1：1检测指纹成功
    private static final int W_CHECK_FAILED = 101;//检测指纹失败
    private static final int W_IMG = 201;//发送指纹图片

    //阀值
    private double Threshold = 0.30; //0.4

    private ID_FprCap mLiveScan = null;
    private ID_Fpr mID_Fpr;

    private char[] cErrInfo = new char[ID_FprCap.LIVESCAN_ERRINFO_LENTH];
    private int[] imageWidth = new int[1];
    private int[] imageHeight = new int[1];

    private long startTime, captureTime = -1, extractTime = -1, verifyTime = -1;

    private byte[] m_byFeature = new byte[ID_Fpr.ID_Fpr_LENTH];

    //length shall refer to the state standard GA1011/1012
    private byte[] m_byFeatures = new byte[ID_Fpr.ID_Fpr_LENTH * 1000];
    private int nbyFeature = 0;
    IBHMFingerListener fingerListener;

    /**
     * 指纹图像分值
     */
    private byte[] bScore = new byte[1];

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case W_CHECK_SUCCESS://比对成功
                    fingerListener.checkSuccess();
                    break;
                case W_IMG://获得指纹图片
                    Bitmap image = (Bitmap) msg.obj;
                    fingerListener.printFinger(image);
                    break;
                case W_CHECK_FAILED://比对失败
                case ERRORINFOS://特征没有获取到
                    fingerListener.checkFailed((String) msg.obj);
                    break;

            }
        }
    };

    public FingerPrintBHMHelper(Context context) {
        init(context);
    }

    public void init(Context context) {
        mLiveScan = new ID_FprCap(context, mHandler);
        mID_Fpr = new ID_Fpr();
    }


    /**
     * 初始化指纹
     */
    private void initFingerDevice() {
        mLiveScan.LIVESCAN_Init();
        mLiveScan.LIVESCAN_GetChannelCount();
    }

    /**
     * 反初始化
     */
    private void unInitFingerDevice() {
        mLiveScan.LIVESCAN_Close();
    }


    /**
     * 打开指纹设备
     */
    public boolean openFingerDevice() {

        int iRet = mLiveScan.LIVESCAN_Init();
        mLiveScan.LIVESCAN_GetErrorInfo(iRet, cErrInfo);
        String Msg = String.format("LIVESCAN_Init:%d_%s\r\n", iRet, String.valueOf(cErrInfo));

        iRet = mLiveScan.LIVESCAN_GetChannelCount();

        Log.i(TAG, "---" + iRet);
        if (iRet < ID_FprCap.LIVESCAN_SUCCESS) {
            //指纹模块准备不成功
            mLiveScan.LIVESCAN_GetErrorInfo(iRet, cErrInfo);
            Msg += String.format("LIVESCAN_GetChannelCount:%d_%s\r\n", iRet, String.valueOf(cErrInfo));
            EventBus.getDefault().post(Msg);
            return false;
        } else {
            //指纹模块装备成功

            mLiveScan.LIVESCAN_GetMaxImageSize(0, imageWidth, imageHeight);

            btnFingerState(true);

            iRet = mID_Fpr.FP_Begin();
            Msg += String.format("FP_Begin:%d", iRet);
            Log.e(TAG, Msg);
            return true;

        }
    }

    /**
     * 关闭指纹设备
     */
    public boolean closeFingerDevice() {

        //LIVESCAN_Init
        int iRet = mLiveScan.LIVESCAN_Close();
        mLiveScan.LIVESCAN_GetErrorInfo(iRet, cErrInfo);
        String Msg = String.format("LIVESCAN_Close:%d_%s\r\n", iRet, String.valueOf(cErrInfo));
        //FP_begin
        iRet = mID_Fpr.FP_End();
        Msg += String.format("FP_End:%d", iRet);

        if (iRet == 0) {
            btnFingerState(false);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 打开模式、关闭模式
     *
     * @param state
     */
    private void btnFingerState(boolean state) {

    }


    /**
     * 录入指纹 获得指纹信息 指纹  指纹图片
     */
    public void enroll() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                collectFinger();
            }
        }).start();
    }

    private void check(ParseSFZAPI.People people) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                searchFingerprint(people);
            }
        }).start();
    }

    /**
     * 1、采集指纹图片
     * 2、获取指纹特征
     * 阻塞
     */
    private void collectFinger() {
        String Msg = null;
        int iRet = -1;
        do {
            //1.先采取指纹图片
            startTime = System.currentTimeMillis();
            iRet = mLiveScan.LIVESCAN_BeginCapture(0);


            if (iRet < ID_FprCap.LIVESCAN_SUCCESS) {
                mLiveScan.LIVESCAN_GetErrorInfo(iRet, cErrInfo);
                Msg = String.format("LIVESCAN_BeginCapture:%d_%s\r\n", iRet, String.valueOf(cErrInfo));
                Log.e(TAG, Msg);
                sendCheckFailed(Msg, ERRORINFOS);
                return;
            }

            //LIVESCAN_GetFPBmpData
            if (imageWidth[0] == 0 || imageHeight[0] == 0) {
                Log.e(TAG, "please invoking LIVESCAN_GetMaxImageSize ");
                sendCheckFailed("please invoking LIVESCAN_GetMaxImageSize ", ERRORINFOS);
                return;
            }

            byte[] fpBmp = new byte[imageWidth[0] * imageHeight[0] + ID_FprCap.LIVESCAN_IMAGE_HEADER];

            iRet = mLiveScan.LIVESCAN_GetFPBmpData(0, fpBmp);

            if (iRet != ID_FprCap.LIVESCAN_SUCCESS) {
                mLiveScan.LIVESCAN_GetErrorInfo(iRet, cErrInfo);
                Msg = String.format("LIVESCAN_GetFPBmpData:%d_%s\r\n", iRet, String.valueOf(cErrInfo));
                Log.e(TAG, Msg);
                sendCheckFailed(Msg, ERRORINFOS);

            } else {
                captureTime = System.currentTimeMillis() - startTime;
                //取图时间
                SendBmp(BitmapFactory.decodeByteArray(fpBmp, 0, fpBmp.length));
            }


            //2.获取特征值

            startTime = System.currentTimeMillis();

            //LIVESCAN_GetFPBmpData
            if (imageWidth[0] == 0 || imageHeight[0] == 0) {
                Log.e(TAG, "please invoking LIVESCAN_GetMaxImageSize ");
                sendCheckFailed("please invoking LIVESCAN_GetMaxImageSize", ERRORINFOS);

                return;
            }
            byte[] fpRaw = new byte[imageWidth[0] * imageHeight[0]];

            iRet = mLiveScan.LIVESCAN_GetFPRawData(0, fpRaw);

            if (iRet != ID_FprCap.LIVESCAN_SUCCESS) {
                mLiveScan.LIVESCAN_GetErrorInfo(iRet, cErrInfo);
                Msg = String.format("GetFPRawData:%d_%s", iRet, String.valueOf(cErrInfo));
                Log.e(TAG, Msg);
                sendCheckFailed(Msg, ERRORINFOS);

                return;
            } else {
                extractTime = System.currentTimeMillis() - startTime;
                SendBmp(mLiveScan.LIVESCAN_RawToBmp(fpRaw, imageWidth[0], imageHeight[0]));
            }
            iRet = mID_Fpr.FP_GetQualityScore(fpRaw, bScore);
            Log.e(TAG, "-----" + iRet + "-----" + (bScore[0] & 0xff));

            iRet = mID_Fpr.FP_FeatureExtract((byte) 0x17, (byte) 11, fpRaw, m_byFeature);

            System.arraycopy(m_byFeature, 0, m_byFeatures, nbyFeature * ID_Fpr.ID_Fpr_LENTH, ID_Fpr.ID_Fpr_LENTH);


            Msg = String.format("FeatureExtract: %d", iRet);

            Log.e(TAG, Msg);


        } while ((bScore[0] & 0xff) < ID_Fpr.ID_IMAGE_SCORE_TH);


        if (iRet == 0) {
            //指纹采集成功
//            sendInfos(getString(R.string.finger_collect_success) + "  id: " + nbyFeature++, SUCCESSINFOS);
//            sendCheckSuccess();
            Log.i(TAG, "------------------------------" + nbyFeature);
        } else {
            //指纹采集失败
//            sendInfos(getString(R.string.finger_collect_failure), ERRORINFOS);
            sendCheckFailed("采集失败", ERRORINFOS);

        }

        //LIVESCAN_EndCapture
        iRet = mLiveScan.LIVESCAN_EndCapture(0);
        if (iRet < ID_FprCap.LIVESCAN_SUCCESS) {
            mLiveScan.LIVESCAN_GetErrorInfo(iRet, cErrInfo);
            Msg = String.format("EndCapture:%d_%s", iRet, String.valueOf(cErrInfo));
            Log.e(TAG, Msg);
        }

    }

    /**
     * 1:1 匹配指纹
     * 阻塞
     */
    private void matchFingerprint(ParseSFZAPI.People people) {
        //LIVESCAN_BeginCapture
        String Msg = "";
        int iRet = mLiveScan.LIVESCAN_BeginCapture(0);
        if (iRet < ID_FprCap.LIVESCAN_SUCCESS) {
            mLiveScan.LIVESCAN_GetErrorInfo(iRet, cErrInfo);
            Msg = String.format("LIVESCAN_BeginCapture:%d_%s\r\n", iRet, String.valueOf(cErrInfo));
            sendCheckFailed(Msg, W_CHECK_FAILED);
            return;
        }


        //LIVESCAN_GetFPBmpData
        if (imageWidth[0] == 0 || imageHeight[0] == 0) {
            return;
        }

        //SendMesg("Place Your Finger");
        byte[] bScore = new byte[1];
        int i = 0;
        byte[] fpRaw = new byte[imageWidth[0] * imageHeight[0]];
        byte[] fpFtp = new byte[512];//shall refer to the state standard GA1011/1012


        while (!Thread.interrupted()) {

            try {


                iRet = mLiveScan.LIVESCAN_GetFPRawData(0, fpRaw);

                if (iRet != ID_FprCap.LIVESCAN_SUCCESS) {
                    mLiveScan.LIVESCAN_GetErrorInfo(iRet, cErrInfo);
                    Msg = String.format("GetFPRawData:%d_%s", iRet, String.valueOf(cErrInfo));
                    sendCheckFailed(Msg, W_CHECK_FAILED);
                    //SendMesg(Msg);
                    break;
                } else {
                    SendBmp(mLiveScan.LIVESCAN_RawToBmp(fpRaw, imageWidth[0], imageHeight[0]));
                }

                iRet = mID_Fpr.FP_GetQualityScore(fpRaw, bScore);
                Msg = String.format("GetQualityScore:%d _%d", iRet, bScore[0] & 0xff);
                Log.e(TAG, Msg);
                //SendMesg(Msg);

                if ((bScore[0] & 0xff) >= ID_Fpr.ID_IMAGE_SCORE_TH) {
                    //byte cScannerType=0x17
                    //byte cFingerCode = 11~20 97~99 shall refer to the state standard GB5974.1-86
                    startTime = System.currentTimeMillis();
                    /**/
                    iRet = mID_Fpr.FP_FeatureExtract((byte) 0x17, (byte) 11, fpRaw, fpFtp);
                    Msg = String.format("FeatureExtract:%d", iRet);
                    // SendMesg(Msg);
                    if (iRet == ID_Fpr.ID_Fpr_SUCCESS) {
                        float[] fs1 = new float[1];
                        float[] fs2 = new float[1];
                        byte[] one = new byte[512];
                        byte[] two = new byte[512];

                        byte[] model = people.getModel();
                        System.arraycopy(model, 0, one, 0, 512);
                        System.arraycopy(model, 512, two, 0, 512);

                        iRet = mID_Fpr.FP_FeatureMatch(one, fpFtp, fs1);
                        iRet = mID_Fpr.FP_FeatureMatch(two, fpFtp, fs2);

                        String s1 = (fs1[0] >= Threshold) ? "y" : "n";
                        String s2 = (fs2[0] >= Threshold) ? "y" : "n";
                        EventBus.getDefault().post("第一个指纹:" + fs1[0] + ",第二个指纹:" + fs2[1]);
                        //身份证上的第一个指纹比对
                        if ("y".equals(s1) || "y".equals(s2)) {
                            sendCheckSuccess();
                        } else {
                            sendCheckFailed("不匹配", W_CHECK_FAILED);
                        }

                    } else {
                        sendCheckFailed("提取特征失败!", ERRORINFOS);
                    }
                    break;
                } else {
                    i++;
                    Thread.sleep(200);
                    if (i % 2 == 0) {
//                         sendCheckFailed("Lift and Place Your Finger.",ERRORINFOS);
                    }
                    if (i % 2 == 1) {
//                        sendCheckFailed("Lift and Place Your Finger...",ERRORINFOS);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                sendCheckFailed("shibai", ERRORINFOS);
            }
        }
        //LIVESCAN_EndCapture
        iRet = mLiveScan.LIVESCAN_EndCapture(0);
        if (iRet < ID_FprCap.LIVESCAN_SUCCESS) {
            mLiveScan.LIVESCAN_GetErrorInfo(iRet, cErrInfo);
            Msg = String.format("EndCapture:%d_%s", iRet, String.valueOf(cErrInfo));
            //SendMesg(Msg);
        }

    }

    /**
     * 搜索指纹
     * 1:N
     */
    public void searchFingerprint(ParseSFZAPI.People people) {
        byte[] m_byFeatures = new byte[ID_Fpr.ID_Fpr_LENTH * 2];
        byte[] one = new byte[512];
        byte[] two = new byte[512];

        byte[] model = people.getModel();
        System.arraycopy(model, 0, one, 0, 512);
        System.arraycopy(model, 512, two, 0, 512);
        System.arraycopy(one, 0, m_byFeatures, 0 * ID_Fpr.ID_Fpr_LENTH, ID_Fpr.ID_Fpr_LENTH);
        System.arraycopy(two, 0, m_byFeatures, 1 * ID_Fpr.ID_Fpr_LENTH, ID_Fpr.ID_Fpr_LENTH);
        nbyFeature = 2;
        String Msg = "";
        int iRet;
        byte[] fpFtp = new byte[512];
        byte[] fpRaw = new byte[imageWidth[0] * imageHeight[0]];

        do {
            //LIVESCAN_BeginCapture
            iRet = mLiveScan.LIVESCAN_BeginCapture(0);
            if (iRet < ID_FprCap.LIVESCAN_SUCCESS) {
                mLiveScan.LIVESCAN_GetErrorInfo(iRet, cErrInfo);
                Msg = String.format("LIVESCAN_BeginCapture:%d_%s\r\n", iRet, String.valueOf(cErrInfo));
                Log.e(TAG, Msg);
                continue;
            }
            //LIVESCAN_GetFPBmpData
            if (imageWidth[0] == 0 || imageHeight[0] == 0) {
                Log.i(TAG, "imageWidth or imageHeight length is zero !");
                return;
            }

            //shall refer to the state standard GA1011/1012

            startTime = System.currentTimeMillis();

            iRet = mLiveScan.LIVESCAN_GetFPRawData(0, fpRaw);
            captureTime = System.currentTimeMillis() - startTime;

            if (iRet != ID_FprCap.LIVESCAN_SUCCESS) {
                mLiveScan.LIVESCAN_GetErrorInfo(iRet, cErrInfo);
                Msg = String.format("GetFPRawData:%d_%s", iRet, String.valueOf(cErrInfo));
                Log.e(TAG, Msg);
            } else {
                SendBmp(mLiveScan.LIVESCAN_RawToBmp(fpRaw, imageWidth[0], imageHeight[0]));
            }

            iRet = mID_Fpr.FP_GetQualityScore(fpRaw, bScore);
            Msg = String.format("GetQualityScore:%d _%d", iRet, bScore[0] & 0xff);
            Log.e(TAG, "-----" + Msg);

            if ((bScore[0] & 0xff) < ID_Fpr.ID_IMAGE_SCORE_TH) {
//                sendCheckFailed("指纹质量分小于指定阀值!", ERRORINFOS);
            } else {
//                sendCheckFailed("指纹质量分: " + (bScore[0] & 0xff), ERRORINFOS);
            }

        } while ((bScore[0] & 0xff) < ID_Fpr.ID_IMAGE_SCORE_TH);


        //byte cScannerType=0x17
        //byte cFingerCode = 11~20 97~99 shall refer to the state standard GB5974.1-86
        startTime = System.currentTimeMillis();
        iRet = mID_Fpr.FP_FeatureExtract((byte) 0x17, (byte) 11, fpRaw, fpFtp);
        extractTime = System.currentTimeMillis() - startTime;

        Msg = String.format("FeatureExtract:%d", iRet);
        Log.e(TAG, Msg);
        //SendMesg(Msg);
        if (iRet == ID_Fpr.ID_Fpr_SUCCESS) {
            float[] fs = new float[1];
            int[] id = new int[1];
            startTime = System.currentTimeMillis();

            iRet = mID_Fpr.FP_FeatureSearch(fpFtp, m_byFeatures, nbyFeature, id, fs);

            Msg = String.format("FP_FeatureSearch:id:%d %f :%s", id[0], fs[0], (fs[0] >= ID_Fpr.ID_MATCH_TH) ? "y" : "n");
            Log.e(TAG, Msg);

            if (fs[0] >= ID_Fpr.ID_MATCH_TH) {
                verifyTime = System.currentTimeMillis() - startTime;
                sendCheckSuccess();
            } else {
                sendCheckFailed("未匹配", W_CHECK_FAILED);

            }


        } else {
            sendCheckFailed("提取特征失败!", ERRORINFOS);
        }

        //LIVESCAN_EndCapture
        iRet = mLiveScan.LIVESCAN_EndCapture(0);
        if (iRet < ID_FprCap.LIVESCAN_SUCCESS) {
            mLiveScan.LIVESCAN_GetErrorInfo(iRet, cErrInfo);
            Msg = String.format("EndCapture:%d_%s", iRet, String.valueOf(cErrInfo));
//            SendMesg(Msg);
        }

//        dismissProgressDialog();


    }

    private void sendCheckFailed(String failed, int errorinfos) {
        Message msg = mHandler.obtainMessage();
        msg.what = errorinfos;
        msg.obj = failed;
        mHandler.sendMessage(msg);
    }

    private void sendCheckSuccess() {
        Message msg = mHandler.obtainMessage();
        msg.what = W_CHECK_SUCCESS;
        mHandler.sendMessage(msg);
    }

    /**
     * 发送指纹图片
     *
     * @param bmp
     */
    private void SendBmp(Bitmap bmp) {
        Message msg = mHandler.obtainMessage();
        msg.arg1 = 2;
        msg.what = W_IMG;
        msg.obj = bmp;
        mHandler.sendMessage(msg);
    }

    @Override
    public boolean openFingerDev() {
        return openFingerDevice();
    }

    @Override
    public boolean closeFingerDev() {
        return closeFingerDevice();
    }

    @Override
    public void checkFinger(ParseSFZAPI.People people) {
        check(people);
    }

    @Override
    public void setFingerListener(IBHMFingerListener listener) {
        fingerListener = listener;
    }

    @Override
    public void onPause() {
        unInitFingerDevice();
    }

    @Override
    public void onResume() {
        initFingerDevice();
    }


}
