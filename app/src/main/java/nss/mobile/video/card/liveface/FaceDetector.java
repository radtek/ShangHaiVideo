package nss.mobile.video.card.liveface;

import android.graphics.Rect;


import java.util.Arrays;

import nss.mobile.video.card.biometric.ZKFaceDetectService;
import nss.mobile.video.card.utils.ImageDecoder;

/**
 * Created by liwei on 2018/8/14.
 */
public final class FaceDetector {

    private long mHandle;
    private byte[] mNv21Buffer = null;
    private byte[] mBgrBuffer = null;
    private static volatile FaceDetector sInstance = null;

    private FaceDetector() {
    }

    public static FaceDetector getInstance() {
        if (sInstance == null) {
            synchronized (FaceDetector.class) {
                if (sInstance == null) {
                    sInstance = new FaceDetector();
                }
            }
        }
        return sInstance;
    }

    public boolean initialize() {
        mHandle = ZKFaceDetectService.createContext();

        return mHandle != 0;
    }

    public LiveFace detectFace(byte[] nv21ImageBuf, int imageWidth, int imageHeight) {
        if (mHandle == 0) {
            return null;
        }
        if (nv21ImageBuf == null || imageWidth <= 0 || imageHeight <= 0) {
            return null;
        }
        if (mNv21Buffer == null) {
            try {
                mNv21Buffer = new byte[nv21ImageBuf.length];
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return null;
            }
        }
        System.arraycopy(nv21ImageBuf, 0, mNv21Buffer, 0, mNv21Buffer.length);

        if (mBgrBuffer == null) {
            try {
                mBgrBuffer = new byte[imageWidth * imageHeight * 3];
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return null;
            }
        } else {
            Arrays.fill(mBgrBuffer, (byte) 0x00);
        }
        final int[] detectFaceCount = new int[1];
        ImageDecoder.yuv420sp2bgr(mNv21Buffer, mBgrBuffer, imageWidth, imageHeight);
        final int ret = ZKFaceDetectService.detect(mHandle, mBgrBuffer, imageWidth, imageHeight, null, detectFaceCount);
        if (ret != 0) {
            return null;
        }
        if (detectFaceCount[0] > 0) {
            long faceContext = ZKFaceDetectService.getFaceContext(mHandle, 0);
            try {
                float[] quality = new float[1];
                if (0 == ZKFaceDetectService.getFaceQuality(faceContext, quality)) {
                    if (quality[0] < 0.6f) {
                        return null;
                    }
                }

                final int[] pts = new int[4];
                if (0 == ZKFaceDetectService.getFaceRect(faceContext, pts)) {
                    Rect rect =  new Rect(pts[0], pts[1], pts[0] + pts[2], pts[1] + pts[3]);
                    LiveFace liveFace = new LiveFace();
                    liveFace.setBounds(rect);
                }
            } finally {
                ZKFaceDetectService.closeFaceContext(faceContext);
            }
        }
        return null;
    }

    public void cleanup() {
        mNv21Buffer = null;
        mBgrBuffer = null;
        if (mHandle != 0) {
            ZKFaceDetectService.closeContext(mHandle);
        }
    }
}
