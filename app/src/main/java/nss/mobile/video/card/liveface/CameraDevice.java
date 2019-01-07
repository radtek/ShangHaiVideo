package nss.mobile.video.card.liveface;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.TextureView;


import java.io.IOException;
import java.util.List;

import nss.mobile.video.card.provider.Config;
import nss.mobile.video.card.utils.LogUtil;

public class CameraDevice {

    public static Camera initCamera(TextureView textureView, SurfaceTexture surfaceTexture) {
        Camera camera=null;
        if (surfaceTexture != null) {
            //0代表后置摄像头
            camera = Camera.open(Config.CameraDirection);
        }
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        Camera.Size optionSize = CameraDevice.getOptimalPreviewSize(sizeList, textureView.getWidth(), textureView.getHeight());
        LogUtil.i("liwei","optionSizeWidth:"+optionSize.width);
        LogUtil.i("liwei","optionSizeHeight:"+optionSize.height);
        Config.CameraWidth=optionSize.width;
        Config.CameraHight=optionSize.height;

        LogUtil.i("liwei","optionSizeWidth22:"+Config.CameraWidth);
        LogUtil.i("liwei","optionSizeHeight22:"+Config.CameraHight);
        parameters.setPreviewSize(Config.CameraWidth, Config.CameraHight);
        parameters.setPreviewFormat(ImageFormat.NV21);
        camera.setParameters(parameters);
        try {
            camera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.setDisplayOrientation(270);
        camera.setPreviewCallback(new CameraPreview());
        camera.startPreview();
        return camera;
    }

    public static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}
