package nss.mobile.video.card.Cognaxon;

/**
 * 作者：李阳
 * 时间：2018/10/10
 * 描述：
 */
public class WSQdemo {
    static {
        System.loadLibrary("WSQ_library_android");
    }

    public WSQdemo() {
    }

    public static native byte[] WSQ_decode_stream(byte[] var0, int[] var1);

    public static native byte[] WSQ_encode_stream(byte[] var0, int var1, int var2, double var3, int var5, String var6);
}
