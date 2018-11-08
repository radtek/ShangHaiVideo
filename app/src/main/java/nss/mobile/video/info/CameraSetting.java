package nss.mobile.video.info;

import android.hardware.Camera;

import com.qiniu.pili.droid.streaming.CameraStreamingSetting;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/9
 *
 * @author ql
 */
public class CameraSetting {


    public void s(){
        CameraStreamingSetting mCameraStreamingSetting = new CameraStreamingSetting();
        mCameraStreamingSetting.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT); // 前置摄像头
        mCameraStreamingSetting.setContinuousFocusModeEnabled(false);
        mCameraStreamingSetting.setRecordingHint(false);
        mCameraStreamingSetting.setBuiltInFaceBeautyEnabled(false);

    }
}
