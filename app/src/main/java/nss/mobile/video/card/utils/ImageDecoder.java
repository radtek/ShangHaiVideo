package nss.mobile.video.card.utils;

/**
 * ID photo decoder
 */
public class ImageDecoder {

    static {
        System.loadLibrary("imagedecoder");
    }

    private ImageDecoder() {
        // Forbidden to initialize.
    }

    public native static void rotateNV21Degree90(byte[] src, byte[] dst, int width, int height);

    public native static void rotateNV21Degree180(byte[] src, byte[] dst, int width, int height);

    public native static void rotateNV21Degree270(byte[] src, byte[] dst, int width, int height);

    public native static void rotateNV21(byte[] input, byte[] output, int width, int height, final int rotation, boolean flipHorizontal);

    public native static void rotateNV21Ext(byte[] input, byte[] output, int width, int height, final int rotation);

    public native static void yuv420sp2argb8888(byte[] yuv420p, int[] rgb8888, int width, int height);

    public native static void yuv420sp2bgr(byte[] yuv420sp, byte[] bgr, int width, int height);

    public native static void bgr2rgb565(byte[] bgr, int[] rgb565, int width, int height);

    public native static void argb88882bgr(byte[] argb8888, byte[] bgr, int width, int height);

    public native static void rgb5652bgr(byte[] rgb565, byte[] bgr, int width, int height);

    public native static void rgb2bgr(byte[] rgb, byte[] bgr, int width, int height);
}