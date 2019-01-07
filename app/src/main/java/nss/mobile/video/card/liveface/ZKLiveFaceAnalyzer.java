package nss.mobile.video.card.liveface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;

import com.zkteco.android.biometric.liveface56.ZKLiveFaceService;

import java.io.File;
import java.io.FileInputStream;

import nss.mobile.video.card.utils.LogUtil;

/**
 * @author liwei
 * @create 2018/8/15
 * @Describe
 */
public class ZKLiveFaceAnalyzer {
    public final int INIT_SUCCESS = 0;
    public final int INIT_UNFIND_FILE = 1;
    public final int INIT_SETPARAM_ERROR = 2;
    public final int INIT_ERROR = 3;
    public static ZKLiveFaceAnalyzer zkLiveFaceService = null;
    private Context mContext;
    public long handle;
    public static int verifyScore = 72;
    public static int livenessScore = 70;
    private Rect rect = null;
    private boolean mIsInit = false;
    private int mAddDip=0;

    public boolean isInit() {
        return mIsInit;
    }

    public void setInit(boolean init) {
        LogUtil.i("liwei","isInit2:"+init);
        this.mIsInit = init;
    }

    public static ZKLiveFaceAnalyzer getInstance(Context context) {
        if (zkLiveFaceService == null) {
            zkLiveFaceService = new ZKLiveFaceAnalyzer(context);
        }
        return zkLiveFaceService;
    }

    private ZKLiveFaceAnalyzer(Context context) {
        mContext = context;
    }

    public String getHardWareCode() {
        byte[] hwid = new byte[256];
        int[] size = new int[1];
        size[0] = 256;
        String hwidStr = null;
        if (0 == ZKLiveFaceService.getHardwareId(hwid, size)) {
            hwidStr = new String(hwid, 0, size[0]);
        } else {
            hwidStr = getLastError(0);
        }
        return hwidStr;
    }

    public String getDeviceFingerprint() {
        byte[] hwid = new byte[32 * 1024];
        int[] size = new int[1];
        size[0] = 32 * 1024;
        if (0 == ZKLiveFaceService.getDeviceFingerprint(hwid, size)) {
            String hwidStr = new String(hwid, 0, size[0]);
            return hwidStr;
        } else {
            return null;
        }
    }

    public String getLastError(long context) {
        byte[] lasterror = new byte[256];
        int[] size = new int[1];
        size[0] = 256;
        if (0 == ZKLiveFaceService.getLastError(context, lasterror, size)) {
            String errStr = new String(lasterror, 0, size[0]);
            return errStr;
        } else {
            return "ERROR";
        }
    }

    public int init() {
        long[] retContext = new long[1];
        int ret = ZKLiveFaceService.init(retContext);
        LogUtil.i("liwei","ret:"+ret);

        if (ret != 0) {
            setInit(false);
            return INIT_ERROR;
        }
        handle = retContext[0];
        setInit(true);
        return INIT_SUCCESS;
    }

    public int initAuth() {
        Log.i("liwei","initAuth");
        File file = new File(Environment.getExternalStorageDirectory(), "zkliveface56.lic");
        Log.i("liwei","file:"+file);

        if (!file.exists()) {
            return INIT_UNFIND_FILE;
        }
        byte[] buffer = new byte[8192];
        int len = 0;
        try {
            FileInputStream inputStream = new FileInputStream(file);
            buffer = new byte[inputStream.available()];
            len = inputStream.available();
            inputStream.read(buffer);
            inputStream.close();
        } catch (Exception e) {
            Log.i("liwei","Exception:"+e);
        }
        int retCode = 0;
        retCode = ZKLiveFaceService.setParameter(0, 1012, buffer, len);
        Log.i("liwei","retCode:"+retCode);

        if (retCode != 0) {
            return INIT_SETPARAM_ERROR;
        }
        long[] retContext = new long[1];
        int ret = ZKLiveFaceService.init(retContext);
        Log.i("liwei","ret:"+ret);

        if (ret != 0) {
            setInit(false);
            Log.i("liwei","setInit(false)");
            return INIT_ERROR;
        }
        handle = retContext[0];
        setInit(true);
        Log.i("liwei","setInit(true)");
        return INIT_SUCCESS;
    }

    public byte[] detectFace(Bitmap bitmap) {
        int[] detectedFaces = new int[1];
        int ret = ZKLiveFaceService.detectFacesFromBitmap(handle, bitmap, detectedFaces);
        if (ret != 0 || detectedFaces[0] <= 0) {
            return null;
        }
        long[] faceContext = new long[1];
        ret = ZKLiveFaceService.getFaceContext(handle, 0, faceContext);
        if (ret == 0) {
            try {
                byte[] template = new byte[8 * 1024];
                int[] size = new int[1];
                int[] resverd = new int[1];
                size[0] = 8 * 1024;
                ret = ZKLiveFaceService.extractTemplate(faceContext[0], template, size, resverd);
                if (ret == 0) {
                    return template;
                }
            } finally {
                ZKLiveFaceService.closeFaceContext(faceContext[0]);
            }
        }
        return null;
    }

    public long detectFace(byte[] nv21, int width, int heigh) {
        int[] detectedFaces = new int[1];
        int ret = ZKLiveFaceService.detectFacesFromNV21(handle, nv21, width, heigh, detectedFaces);
        if (ret == 0 && detectedFaces[0] > 0) {
            long[] faceContext = new long[1];
            ret = ZKLiveFaceService.getFaceContext(handle, 0, faceContext);
            if (ret == 0) {
                int[] points = new int[8];
                if (ZKLiveFaceService.getFaceRect(faceContext[0], points, 8) == 0) {
                    setRect(points);
                } else {
                    setRect(null);
                }
                return faceContext[0];
            }
        }
        return -1;
    }

    public void setRect(int[] points) {
        if (points == null) {
            rect = null;
        } else {
            rect = new Rect();
            rect.left = points[0];
            rect.top = points[1]-mAddDip;
            rect.right = points[2];
            rect.bottom = points[5]-mAddDip;
            LogUtil.i("rect.left:" + rect.left);
            LogUtil.i("rect.top:" + rect.top);
            LogUtil.i("rect.right:" + rect.right);
            LogUtil.i("rect.bottom:" + rect.bottom);
        }
    }

    public Rect getRect() {
        if (rect == null) {
            return null;
        }
        if (rect.left<0){
            return null;
        }
        if (rect.top<0){
            return null;
        }
        return rect;
    }

    public byte[] extracTemplate(long faceContext) {
        try {
            byte[] template = new byte[8 * 1024];
            int[] size = new int[1];
            int[] resverd = new int[1];
            size[0] = 8 * 1024;
            int ret = ZKLiveFaceService.extractTemplate(faceContext, template, size, resverd);
            if (ret == 0) {
                return template;
            }
        } finally {
            ZKLiveFaceService.closeFaceContext(faceContext);
        }
        return null;
    }

    public boolean verify(byte[] card, byte[] current) {
        int[] score = new int[1];
        int ret = ZKLiveFaceService.verify(handle, card, current, score);
        if (ret == 0) {
            if (score[0] > verifyScore) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isLiveness(long faceContext) {
        int[] score = new int[1];
        int ret = ZKLiveFaceService.getLiveness(faceContext, score);
        if (ret == 0) {
            if (score[0] > livenessScore) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
