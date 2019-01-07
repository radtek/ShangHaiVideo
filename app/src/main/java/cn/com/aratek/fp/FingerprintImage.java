package cn.com.aratek.fp;


import nss.mobile.video.card.WSQdemo;

/**
 * 时间：2018/10/10
 * 描述：指纹图片模板
 */
public class FingerprintImage {
    public final byte[] raw;
    public final int width;
    public final int height;
    public final int dpi;

    static {
        System.loadLibrary("AraBMApiFpAlgorithm");
        System.loadLibrary("AraBione");
        System.loadLibrary("AraBMApiDev");
        System.loadLibrary("WSQ_library_android");
        System.loadLibrary("AraBMApiFp");
        initialize();
    }

    /**
     * 构造函数（Constructor）
     * @param raw
     * @param width
     * @param height
     */
    FingerprintImage(byte[] raw, int width, int height) {
        this(raw, width, height, 500);
    }

    /**
     * 构造函数（Constructor）
     * @param raw
     * @param width
     * @param height
     * @param dpi
     */
    FingerprintImage(byte[] raw, int width, int height, int dpi) {
        this.raw = raw;
        this.width = width;
        this.height = height;
        this.dpi = dpi;
    }

    /**
     * 转换为bmp（Convert to Bitmap）
     * @return
     */
    public byte[] convert2Bmp() {
        return raw2Bmp(this.raw, this.width, this.height, this.dpi);
    }

    /**
     * 转为Wsq（Convert to Wsq）
     * @return
     */
    public byte[] convert2Wsq() {
        return WSQdemo.WSQ_encode_stream(this.raw, this.width, this.height, 0.75D, this.dpi, "WSQ");
    }

    private static native byte[] raw2Bmp(byte[] var0, int var1, int var2, int var3);

    private static native byte[] raw2Wsq(byte[] var0, int var1, int var2, double var3, int var5, String var6);

    private static native int initialize();
}
