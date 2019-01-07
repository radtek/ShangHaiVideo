package cn.com.aratek.fp;

import android.content.Context;
import android.util.Log;

import java.util.Iterator;
import java.util.Map;

import cn.com.aratek.util.Result;

/**
 * 时间：2018/10/10
 * 描述：Bione Class
 */
public class Bione {

    public static final int RESULT_OK = 0;
    public static final int WRONG_PARAMETER = -1006;
    public static final int INITIALIZE_ERROR = -2000;
    public static final int INVALID_FEATURE_DATA = -2001;
    public static final int BAD_IMAGE = -2002;
    public static final int NOT_MATCH = -2003;
    public static final int LOW_POINT = -2004;
    public static final int NO_FINGER = -2005;
    public static final int NO_RESULT = -2006;
    public static final int OUT_OF_BOUND = -2007;
    public static final int DATABASE_FULL = -2008;
    public static final int LIBRARY_MISSING = -2010;
    public static final int UNINITIALIZE = -2011;
    public static final int REINITIALIZE = -2012;
    public static final int REPEATED_ENROLL = -2013;
    public static final int NOT_ENROLLED = -2014;
    public static final int SECURITY_LOW = 1;
    public static final int SECURITY_MEDIUM = 2;
    public static final int SECURITY_HIGH = 3;
    public static final int SECURITY_DEFAULT = 2;
    public static final int TYPE_VERIFY = 0;
    public static final int TYPE_IDENTIFY = 1;
    public static final int TYPE_IDCARD_VERIFY = 2;
    public static final int TYPE_IDCARD_IDENTIFY = 3;
    private static final String TAG = "Bione";
    private static int security = 2;
    private static float verifyThreshold;
    private static float identifyThreshold;
    private static float idcardVerifyThreshold;
    private static float idcardIdentifyThreshold;

    static {
        System.loadLibrary("AraBMApiFpAlgorithm");
        System.loadLibrary("AraBione");
        System.loadLibrary("AraBMApiDev");
        System.loadLibrary("WSQ_library_android");
        System.loadLibrary("AraBMApiFp");
    }

    /**
     * Bione Constructor
     */
    public Bione() {
    }

    /**
     * 初始化（initialize）
     * @param context
     * @param dbPath
     * @return
     */
    public static int initialize(Context context, String dbPath) {
        int processors = Runtime.getRuntime().availableProcessors();
        Log.i("Bione", "Initialize algorithm using " + processors + " thread(s).");
        int ret = initialize(dbPath, processors);
        if(ret == 0) {
            setSecurityLevel(2);
        }

        return ret;
    }

    private static native int initialize(String var0, int var1);

    /**
     * 退出（exit）
     * @return
     */
    public static native int exit();

    /**
     * 获取版本（get Version）
     * @return
     */
    public static native int getVersion();

    /**
     * 获取指纹相似度
     * @param image
     * @return
     */
    public static int getFingerprintQuality(FingerprintImage image) {
        return getFingerprintQuality(image.raw, image.width, image.height, image.dpi);
    }


    private static native int getFingerprintQuality(byte[] var0, int var1, int var2, int var3);

    public static Result extractFeature(FingerprintImage image) {
        return extractFeature(image.raw, image.width, image.height, image.dpi);
    }

    private static native Result extractFeature(byte[] var0, int var1, int var2, int var3);

    public static native Result makeTemplate(byte[] var0, byte[] var1, byte[] var2);

    public static native boolean isFreeID(int var0);

    public static native int getFreeID();

    public static native Result getFeature(int var0);

    public static native Result getAllFeatures();

    public static native int getEnrolledCount();

    public static native int enroll(int var0, byte[] var1);

    public static native int delete(int var0);

    public static native int clear();

    public static native Result verify(byte[] var0, byte[] var1);

    public static native Result verify(int var0, byte[] var1);

    public static native int identify(byte[] var0);

    public static native Result idcardVerify(byte[] var0, byte[] var1);

    public static Result idcardIdentify(Map<String, byte[]> idcardFeatureMap, byte[] feature) {
        if(idcardFeatureMap != null && !idcardFeatureMap.isEmpty()) {
            Iterator var4 = idcardFeatureMap.entrySet().iterator();

            Result res;
            Map.Entry entry;
            do {
                if(!var4.hasNext()) {
                    return new Result(-2006, (Object)null);
                }

                entry = (Map.Entry)var4.next();
                res = idcardVerify((byte[])entry.getValue(), feature);
            } while(res.error != 0 || !((Boolean)res.data).booleanValue());

            return new Result(0, entry.getKey());
        } else {
            return new Result(-1006, (Object)null);
        }
    }

    public static Result extractIsoFeature(FingerprintImage image) {
        return extractIsoFeature(image.raw, image.width, image.height, image.dpi);
    }

    private static native Result extractIsoFeature(byte[] var0, int var1, int var2, int var3);

    public static native Result verifyIsoFeature(byte[] var0, byte[] var1);

    public static void setSecurityLevel(int level) {
        float vt = 0.0F;
        float it = 0.0F;
        float idvt = 0.0F;
        float idit = 0.0F;
        security = level >= 1 && level <= 3?level:2;
        switch(security) {
        case 1:
            vt = 36.0F;
            it = 50.0F;
            idit = 0.5F;
            idvt = 0.5F;
            break;
        case 2:
            vt = 48.0F;
            it = 60.0F;
            idit = 0.68F;
            idvt = 0.68F;
            break;
        case 3:
            vt = 60.0F;
            it = 70.0F;
            idit = 0.75F;
            idvt = 0.75F;
        }

        setThreshold(0, vt);
        setThreshold(1, it);
        setThreshold(2, idvt);
        setThreshold(3, idit);
    }

    public static void setThreshold(int type, float threshold) {
        if(threshold >= 0.0F) {
            switch(type) {
            case 0:
                verifyThreshold = threshold;
                break;
            case 1:
                identifyThreshold = threshold;
                break;
            case 2:
            case 3:
                idcardVerifyThreshold = threshold;
                idcardIdentifyThreshold = threshold;
            }
        }
    }
}
