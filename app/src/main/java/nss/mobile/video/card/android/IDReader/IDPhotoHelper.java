package nss.mobile.video.card.android.IDReader;

import android.graphics.Bitmap;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

import nss.mobile.video.card.android.device.idreader.IdPhotoDecoder;


/**
 * Created by scarx on 2015/12/3.
 */
public class IDPhotoHelper {
    /*public static Bitmap Bgr2Bitmap(byte[] bgrbuf)
    {
        int width = WLTService.imgWidth;
        int height = WLTService.imgHeight;
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int row = 0, col = width-1;
        for (int i = bgrbuf.length-1; i >= 3; i -= 3) {
            int color = bgrbuf[i] & 0xFF;
            color += (bgrbuf[i-1] << 8) & 0xFF00;
            color += ((bgrbuf[i-2]) << 16) & 0xFF0000;
            bmp.setPixel(col--, row, color);
            if (col < 0) {
                col = width-1;
                row++;
            }
        }
        return bmp;
    }*/

   public static Bitmap Bgr2Bitmap(byte[] bgrbuf) {
        int width = WLTService.imgWidth;
        int height = WLTService.imgHeight;
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int[] pixels = new int[width * height];
        IdPhotoDecoder.bgr2rgb565(bgrbuf, pixels, width, height);
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmp;
    }

    public static void verticalImage(byte[] src, byte[] dst, int width, int height, int depth) {
        byte[] tmp = new byte[src.length];
        IdPhotoDecoder.rgb2bgr(src, tmp, width, height);

        for (int h = 0; h < height; h++) {
            System.arraycopy(tmp, h * width * depth, dst, (height - h - 1) * width * depth, width * depth);
        }
    }


    protected Bitmap decodePhoto(byte[] buffer) {
        if (buffer == null) {
            return null;
        }

        Bitmap bitmap = null;

        try {
            bitmap = IDPhotoHelper.Bgr2Bitmap(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
        try {
            return out.toByteArray();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
