package nss.mobile.video.card.android.device.idreader;

/**
 * ID photo decoder
 */
public class IdPhotoDecoder {

    static {
        System.loadLibrary("idphotodecoder");
    }

    private IdPhotoDecoder() {
        // Forbidden to initialize.
    }

    public native static void bgr2rgb565(byte[] bgr, int[] rgb565, int width, int height);

    public native static void argb88882bgr(byte[] argb8888, byte[] bgr, int width, int height);

    public native static void rgb5652bgr(byte[] rgb565, byte[] bgr, int width, int height);

    public native static void rgb2bgr(byte[] rgb, byte[] bgr, int width, int height);
}