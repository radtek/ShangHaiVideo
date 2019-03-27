package nss.mobile.video.ui.video;

import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiangdg.usbcamera.FileUtils;
import com.jiangdg.usbcamera.USBCameraManager;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.encoder.RecordParams;
import com.serenegiant.usb.widget.CameraViewInterface;
import com.serenegiant.usb.widget.UVCCameraTextureView;

import java.io.File;

import nss.mobile.video.C;
import nss.mobile.video.R;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.card.authentication.aratek.SwitchUtil;
import nss.mobile.video.utils.DataUtils;
import nss.mobile.video.utils.DateUtils;
import nss.mobile.video.video.VideoFile;

@BindLayout(layoutRes = R.layout.activity_usb_camera, title = "USB摄像头", bindTopBar = false)
public class UsbCameraActivity extends BaseActivity {

    @BindView(R.id.textureView)
    UVCCameraTextureView textureView;
    @BindView(R.id.usb_camera_tv_hint)
    TextView mHintTv;
    @BindView(R.id.usb_camera_action_iv)
    ImageView mActionIv;


    USBCameraManager mUSBManager;
    private boolean isPreview;
    private boolean isRequest;

    private boolean isCamera;//是否开始录像;
    private UVCCameraTextureView mUVCCameraView;

    @Override
    public void init(Bundle savedInstanceState) {
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.init(savedInstanceState);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        init();

        mActionIv.setOnClickListener(this);

    }


    private void init() {
        mUSBManager = USBCameraManager.getInstance();
        USBCameraManager.OnMyDevConnectListener listener = new USBCameraManager.OnMyDevConnectListener() {
            // 插入USB设备
            @Override
            public void onAttachDev(UsbDevice device) {
                int deviceId = device.getDeviceId();
                int productId = device.getProductId();
                int deviceClass = device.getDeviceClass();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mUSBManager == null || mUSBManager.getUsbDeviceCount() == 0) {
                            displayMessageDialog("未检测到USB摄像头设备 ,deviceId =" + deviceId + "deviceClass = " + deviceClass + ",productId=" + productId + "," + mUSBManager.getUsbDeviceCount());
                            mHintTv.setText("未检测到USB摄像头设备");
                            return;
                        }

//                      // 请求打开摄像头
                        if (!isRequest) {
                            isRequest = true;
                            if (mUSBManager != null) {
                                mUSBManager.requestPermission(0);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mHintTv.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }
                    }
                });


            }


            // 拔出USB设备
            @Override
            public void onDettachDev(UsbDevice device) {
                if (isRequest) {
                    // 关闭摄像头
                    isRequest = false;


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mUSBManager.closeCamera();
                            displayMessageDialog(device.getDeviceName() + "已拨出");
                            mHintTv.setText("设备已经拔出");
                            mHintTv.setVisibility(View.VISIBLE);
                        }
                    });

                }
            }


            // 连接USB设备成功
            @Override
            public void onConnectDev(UsbDevice device, boolean isConnected) {
                if (!isConnected) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            displayMessageDialog("连接失败，请检查分辨率参数是否正确");

                        }
                    });
                    isPreview = false;
                } else {
                    isPreview = true;
                }
            }


            // 与USB设备断开连接
            @Override
            public void onDisConnectDev(UsbDevice device) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isPreview = false;
                        displayMessageDialog("连接失败");
                        mHintTv.setText("连接失败");
                        mHintTv.setVisibility(View.VISIBLE);
                    }
                });

            }

        };
        mUVCCameraView = textureView;
        mUVCCameraView.setCallback(new CameraViewInterface.Callback() {
            @Override
            public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
                if (!isPreview && mUSBManager.isCameraOpened()) {
                    mUSBManager.startPreview(mUVCCameraView, new AbstractUVCCameraHandler.OnPreViewResultListener() {
                        @Override
                        public void onPreviewResult(boolean result) {


                        }
                    });
                    isPreview = true;
                }
            }

            @Override
            public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {


            }


            @Override
            public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
                if (isPreview && mUSBManager.isCameraOpened()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mUSBManager.stopPreview();
                        }
                    });

                    isPreview = false;
                }
            }
        });
        // 初始化引擎
        mUSBManager = USBCameraManager.getInstance();
        mUSBManager.initUSBMonitor(this, listener);
        mUSBManager.createUVCCamera(mUVCCameraView);


// 注册USB事件广播监听器
        if (mUSBManager != null) {
            mUSBManager.registerUSB();
        }

    }

    @Override
    public void forbidClick(View v) {
        super.forbidClick(v);
        if (v.getId() == mActionIv.getId()) {
            if (isCamera) {
                //通知 关闭
                stopVideo(v);
            } else {
                //通知开启录像
                startVideo(v);
            }
        }
    }

    private void open() {
        if (mUSBManager == null || !mUSBManager.isCameraOpened()) {
            displayMessageDialog("录制异常，摄像头未开启");
            return;
        }
        if (!mUSBManager.isRecording()) {
//            String videoPath = USBCameraManager.ROOT_PATH + System.currentTimeMillis();
//            FileUtils.createfile(FileUtils.ROOT_PATH + "test666.h264");
            VideoFile videoFile = new VideoFile(null);
            int l = videoFile.getFullPath().length() - ".mp4".length();
            String substring = videoFile.getFullPath().substring(0, l);
            RecordParams params = new RecordParams();
            params.setRecordPath(substring);
            params.setRecordDuration(0);    // 设置为0，不分割保存
            params.setVoiceClose(false);    // 不屏蔽声音
            isCamera = true;
            mActionIv.setSelected(true);
            mUSBManager.startRecording(params, new AbstractUVCCameraHandler.OnEncodeResultListener() {
                @Override
                public void onEncodeResult(byte[] data, int offset, int length, long timestamp, int type) {
                    // type = 0,aac格式音频流
                    // type = 1,h264格式视频流

                    if (type == 1) {
//                        FileUtils.putFileStream(data, offset, length);
                    }
                }


                @Override
                public void onRecordResult(String videoPath) {
                    displayMessageDialog(videoPath);
                }
            });
        }
    }

    private void startVideo(View view) {
        open();

    }

    private void stopVideo(View view) {
        mUSBManager.stopRecording();
        isCamera = false;
        mActionIv.setSelected(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUSBManager.unregisterUSB();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUSBManager.registerUSB();
        // 恢复Camera预览
        if (mUVCCameraView != null) {
            mUVCCameraView.onResume();
        }
    }
}
