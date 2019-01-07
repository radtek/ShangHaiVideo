package nss.mobile.video.card.liveface;

import android.hardware.Camera;


import java.util.Arrays;

import nss.mobile.video.card.provider.Config;

public class CameraPreview implements Camera.PreviewCallback {

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (Config.VerifyFrameBuf == null) {
            Config.VerifyFrameBuf = Arrays.copyOf(data, data.length);
        } else {
            System.arraycopy(data, 0, Config.VerifyFrameBuf, 0, data.length);
        }
    }
}
