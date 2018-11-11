package nss.mobile.video.ui;

import android.hardware.Camera;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.qiniu.android.dns.DnsManager;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.http.DnspodFree;
import com.qiniu.android.dns.local.AndroidDnsServer;
import com.qiniu.android.dns.local.Resolver;
import com.qiniu.pili.droid.streaming.AVCodecType;
import com.qiniu.pili.droid.streaming.CameraStreamingSetting;
import com.qiniu.pili.droid.streaming.MediaStreamingManager;
import com.qiniu.pili.droid.streaming.MicrophoneStreamingSetting;
import com.qiniu.pili.droid.streaming.StreamingProfile;
import com.qiniu.pili.droid.streaming.StreamingState;
import com.qiniu.pili.droid.streaming.StreamingStateChangedListener;
import com.qiniu.pili.droid.streaming.widget.AspectFrameLayout;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;

import nss.mobile.video.C;
import nss.mobile.video.R;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.ui.widget.CameraPreviewFrameView;

@BindLayout(layoutRes = R.layout.activity_sim_live_video, bindTopBar = false)
public class SimLiveVideoActivity extends BaseActivity implements CameraPreviewFrameView.Listener, StreamingStateChangedListener {


    private MediaStreamingManager streamingManager;
    private StreamingProfile streamingProfile;
    private MicrophoneStreamingSetting mMicrophoneStreamingSetting;
    @BindView(R.id.live_video_hint_tv)
    TextView mHintTv;
    @BindView(R.id.live_video_open_tv)
    TextView mOpenLiveTv;
    @BindView(R.id.live_video_camera_tv)
    TextView mCameraTv;

    private boolean mIsLiveVideo = false;
    private CameraStreamingSetting setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initWidget() {
        super.initWidget();

        mOpenLiveTv.setOnClickListener(this);
        mCameraTv.setOnClickListener(this);
        AspectFrameLayout afl = (AspectFrameLayout) findViewById(R.id.cameraPreview_afl);
        afl.setShowMode(AspectFrameLayout.SHOW_MODE.REAL);
        CameraPreviewFrameView cameraPreviewFrameView =
                (CameraPreviewFrameView) findViewById(R.id.cameraPreview_surfaceView);
        cameraPreviewFrameView.setListener(this);
        String publishurl = "rtmp://pili-publish.push.expresscourts.com/expresscourt/hello";
        streamingProfile = new StreamingProfile();

        try {
            streamingProfile.setVideoQuality(StreamingProfile.VIDEO_QUALITY_MEDIUM2)
                    .setAudioQuality(StreamingProfile.AUDIO_QUALITY_MEDIUM2)
//                .setPreferredVideoEncodingSize(960, 544)
                    .setEncodingSizeLevel(StreamingProfile.VIDEO_ENCODING_HEIGHT_480)
                    .setEncoderRCMode(StreamingProfile.EncoderRCModes.BITRATE_PRIORITY)
//                .setAVProfile(avProfile)
                    .setDnsManager(getMyDnsManager())
                    .setAdaptiveBitrateEnable(true)
                    .setFpsControllerEnable(true)
                    .setStreamStatusConfig(new StreamingProfile.StreamStatusConfig(3))
                    .setPublishUrl(publishurl)
//                .setEncodingOrientation(StreamingProfile.ENCODING_ORIENTATION.PORT)
                    .setSendingBufferProfile(new StreamingProfile.SendingBufferProfile(0.2f, 0.8f, 3.0f, 20 * 1000));
             setting = new CameraStreamingSetting();
            setting.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK)
                    .setContinuousFocusModeEnabled(true)
                    .setCameraPrvSizeLevel(CameraStreamingSetting.PREVIEW_SIZE_LEVEL.MEDIUM)
                    .setCameraPrvSizeRatio(CameraStreamingSetting.PREVIEW_SIZE_RATIO.RATIO_16_9);

            streamingManager = new MediaStreamingManager(this, afl, cameraPreviewFrameView,
                    AVCodecType.HW_VIDEO_WITH_HW_AUDIO_CODEC); // hw codec  // soft codec
            mMicrophoneStreamingSetting = new MicrophoneStreamingSetting();
            mMicrophoneStreamingSetting.setBluetoothSCOEnabled(false);
            streamingManager.prepare(setting, mMicrophoneStreamingSetting, null, streamingProfile);
            streamingManager.setStreamingStateListener(this);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private boolean isBack = true;

    @Override
    public void forbidClick(View v) {
        super.forbidClick(v);
        if (v.getId() == mOpenLiveTv.getId()) {
            if (mIsLiveVideo) {
                stopLiveVideo();
            } else {
                startLiveVideo();
            }
        }else if (v.getId() == mCameraTv.getId()){
            if (isBack) {
                streamingManager.switchCamera(CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT);
            }else {
                streamingManager.switchCamera(CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_BACK);
            }
            isBack =!isBack;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        streamingManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // You must invoke pause here.
        streamingManager.pause();
    }


    @Override
    public void onStateChanged(StreamingState streamingState, Object o) {
        switch (streamingState) {
            case PREPARING:
                //预备
                break;
            case READY:
                //准备
                // start streaming when READY
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (streamingManager != null) {
                            startLiveVideo();
                        }
                    }
                }).start();

                break;
            case CONNECTING:
                //连接
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mHintTv.setText("连接成功");
                    }
                });

                break;
            case STREAMING:
                // The av packet had been sent.
                liveVideoStatus(true);
                break;
            case SHUTDOWN:
                //停止推流
                // The streaming had been finished.
                liveVideoStatus(false);
                break;
            case IOERROR:
                // Network connect error.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mHintTv.setText("直播通道异常,尝试重新连接");
                    }
                });

                C.sHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startLiveVideo();
                    }
                }, 2_000);

                break;
            case SENDING_BUFFER_EMPTY:
                break;
            case SENDING_BUFFER_FULL:
                break;
            case AUDIO_RECORDING_FAIL:
                // Failed to record audio.
                break;
            case OPEN_CAMERA_FAIL:
                // Failed to open camera.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mHintTv.setText("打开相机失败");
                    }
                });

                break;
            case DISCONNECTED:
                // The socket is broken while streaming
                break;
            case INVALID_STREAMING_URL:
                break;
            case UNAUTHORIZED_STREAMING_URL:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mHintTv.setText("未知推流地址");
                    }
                });


                break;
        }
    }

    private void liveVideoStatus(boolean b) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIsLiveVideo = b;
                String s = b ? "关闭" : "开启";
                mOpenLiveTv.setText(s);
                if (b){
                    mHintTv.setText("直播中");
                }else {
                    mHintTv.setText("未开启直播");
                }
            }
        });

    }

    public void startLiveVideo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean b = streamingManager.startStreaming();
                final String s = b ? "开启" : "关闭";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mOpenLiveTv.setText(s);
                    }
                });
            }
        }).start();

    }

    public void stopLiveVideo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean b = streamingManager.stopStreaming();
                final String s = b ? "开启" : "关闭";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mOpenLiveTv.setText(s);
                    }
                });
                streamingManager.stopStreaming();

            }
        }).start();
    }

    private static DnsManager getMyDnsManager() {
        IResolver r0 = new DnspodFree();
        IResolver r1 = AndroidDnsServer.defaultResolver();
        IResolver r2 = null;
        try {
            r2 = new Resolver(InetAddress.getByName("119.29.29.29"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new DnsManager(NetworkInfo.normal, new IResolver[]{r0, r1, r2});
    }


    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onZoomValueChanged(float factor) {
        return false;
    }


}
