/*
 *  Copyright 2016 Jeroen Mols
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package nss.mobile.video.video;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore.Video.Thumbnails;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import nss.mobile.video.MyApp;
import nss.mobile.video.R;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.bean.MemoryBean;
import nss.mobile.video.event.FileMemoryEvent;
import nss.mobile.video.ui.SnActivity;
import nss.mobile.video.ui.video.AliFileActivity;
import nss.mobile.video.ui.video.AllFunctionActivity;
import nss.mobile.video.ui.wifi.WifiManagerActivity;
import nss.mobile.video.utils.UnitHelper;
import nss.mobile.video.utils.preferences.QualityPreferences;
import nss.mobile.video.utils.preferences.SettingPreferences;
import nss.mobile.video.video.camera.CameraWrapper;
import nss.mobile.video.video.camera.NativeCamera;
import nss.mobile.video.video.configuration.CaptureConfiguration;
import nss.mobile.video.video.recorder.AlreadyUsedException;
import nss.mobile.video.video.recorder.VideoRecorder;
import nss.mobile.video.video.recorder.VideoRecorderInterface;
import nss.mobile.video.video.view.RecordingButtonInterface;
import nss.mobile.video.video.view.VideoCaptureView;

@BindLayout(layoutRes = R.layout.activity_videocapture, bindTopBar = false)
public class VideoCaptureActivity extends BaseActivity implements RecordingButtonInterface, VideoRecorderInterface {

    public static final int RESULT_ERROR = 753245;
    private static final int REQUESTCODE_SWITCHCAMERA = 222;

    public static final String EXTRA_OUTPUT_FILENAME = "com.jmolsmobile.extraoutputfilename";
    public static final String EXTRA_CAPTURE_CONFIGURATION = "com.jmolsmobile.extracaptureconfiguration";
    public static final String EXTRA_ERROR_MESSAGE = "com.jmolsmobile.extraerrormessage";

    private static final String EXTRA_FRONTFACINGCAMERASELECTED = "com.jmolsmobile.extracamerafacing";
    private static final String SAVED_RECORDED_BOOLEAN = "com.jmolsmobile.savedrecordedboolean";
    protected static final String SAVED_OUTPUT_FILENAME = "com.jmolsmobile.savedoutputfilename";

    @BindView(R.id.videocapture_videocaptureview_vcv)
    VideoCaptureView mVideoCaptureView;

    private boolean mVideoRecorded = false;
    VideoFile mVideoFile = null;
    private CaptureConfiguration mCaptureConfiguration;


    private VideoRecorder mVideoRecorder;
    private boolean isFrontFacingCameraSelected = true;
    private boolean isVideoPause = false;
    private boolean isVideoing = false;
    private CameraWrapper cameraWrapper;

    private boolean isRestart;

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        CLog.toggleLogging(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initializeCaptureConfiguration(savedInstanceState);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        initializeRecordingUI();
        FileMemoryEvent.getInstance().register(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            try {
                mVideoRecorder.toggleRecording();
            } catch (AlreadyUsedException e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    /**
     * 更改摄像头配置
     *
     * @param i
     */
    @Subscribe()
    public void setVideoInfo(int i) {
        if (i == 1) {
            mVideoCaptureView.showTimer(mCaptureConfiguration.getShowTimer());
            long autoPlayLong = SettingPreferences.getAutoPlayLong();
            mVideoCaptureView.setAutoVideoTime(autoPlayLong);
        }
    }

    private void initializeCaptureConfiguration(final Bundle savedInstanceState) {
        mCaptureConfiguration = generateCaptureConfiguration();
        mVideoRecorded = generateVideoRecorded(savedInstanceState);
        mVideoFile = generateOutputFile(savedInstanceState);
        isFrontFacingCameraSelected = generateIsFrontFacingCameraSelected();
    }

    private void initializeRecordingUI() {
        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        cameraWrapper = new CameraWrapper(new NativeCamera(), display.getRotation());
        mVideoRecorder = new VideoRecorder(this,
                mCaptureConfiguration,
                mVideoFile,
                cameraWrapper,
                mVideoCaptureView.getPreviewSurfaceHolder(),
                isFrontFacingCameraSelected);
        mVideoCaptureView.setRecordingButtonInterface(this);
        mVideoCaptureView.setCameraSwitchingEnabled(mCaptureConfiguration.getAllowFrontFacingCamera());
        mVideoCaptureView.setCameraFacing(isFrontFacingCameraSelected);
        mVideoCaptureView.setIsShowFileSize(true);
        if (mVideoRecorded) {
            mVideoCaptureView.updateUIRecordingFinished(getVideoThumbnail());
        } else {
            mVideoCaptureView.updateUINotRecording();
        }
        setVideoInfo(1);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isRestart) {
            Intent intent = new Intent(VideoCaptureActivity.this, VideoCaptureActivity.class);
            intent.putExtras(getIntent().getExtras());      //Pass all the current intent parameters
            intent.putExtra(EXTRA_FRONTFACINGCAMERASELECTED, isFrontFacingCameraSelected);
            startActivityForResult(intent, REQUESTCODE_SWITCHCAMERA);
            overridePendingTransition(R.anim.from_middle, R.anim.to_middle);
            return;
        }
        if (SettingPreferences.isAuto()) {
            mVideoCaptureView.startAutoVideo();
        }

    }

    @Override
    protected void onPause() {
        if (mVideoRecorder != null) {
            mVideoRecorder.stopRecording(null);
        }
        isRestart = true;
        releaseAllResources();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        finishCancelled();
    }

    @Override
    public void onRecordButtonClicked() {
        try {
            //开始录制
            mVideoRecorder.toggleRecording();
        } catch (AlreadyUsedException e) {
            CLog.d(CLog.ACTIVITY, "Cannot toggle recording after cleaning up all resources");
        }
    }

    @Override
    public void onRecordingStarted() {
        MyApp.getInstance().startCaseFileMemoryThread();
        mVideoCaptureView.updateUIRecordingOngoing();
        isVideoing = true;
        MyApp.isVideo = true;
    }

    @Override
    public void onSwitchCamera(boolean isFrontFacingSelected) {
        releaseAllResources();
        Intent intent = new Intent(VideoCaptureActivity.this, VideoCaptureActivity.class);
        intent.putExtras(getIntent().getExtras());      //Pass all the current intent parameters
        intent.putExtra(EXTRA_FRONTFACINGCAMERASELECTED, isFrontFacingSelected);
        startActivityForResult(intent, REQUESTCODE_SWITCHCAMERA);
        overridePendingTransition(R.anim.from_middle, R.anim.to_middle);
    }

    @Override
    public void onRecordButtonPauseOrAgain() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isVideoPause) {
                mVideoCaptureView.updateUIRecordingAgain();
                mVideoRecorder.again();
            } else {
                mVideoCaptureView.updateUIRecordingPause();
                mVideoRecorder.pause();

            }
            isVideoPause = !isVideoPause;
        }
    }

    @Override
    public void onAllMenuButtonClick() {
        Intent intent = new Intent(this, AllFunctionActivity.class);
        startActivity(intent);
    }

    @Override
    public void onScanningWifiButtonClick() {
        startActivity(WifiManagerActivity.class);
    }

    @Override
    public void onQualityButtonClick() {

        startActivity(AliFileActivity.class);

    }

    @Override
    public void onSnButtonClick() {
        startActivity(SnActivity.class);
    }


    @Override
    public void onRecordingStopped(String message) {
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        MyApp.getInstance().stopCaseFileMemoryThread();
        mVideoCaptureView.updateUIRecordingFinished(getVideoThumbnail());
        mVideoRecorder.setVideoFile(new VideoFile(null));
        mVideoRecorder.releaseRecorderResources();
        MyApp.isVideo = false;
    }

    @Override
    public void onRecordingSuccess() {
        mVideoRecorded = true;
        isVideoing = false;
    }

    @Override
    public void onRecordingFailed(String message) {
        finishError(message);
    }


    private void finishCancelled() {
        this.setResult(RESULT_CANCELED);
        finish();
    }

    private void finishError(final String message) {
        Toast.makeText(getApplicationContext(), "Can't capture video: " + message, Toast.LENGTH_LONG).show();

        final Intent result = new Intent();
        result.putExtra(EXTRA_ERROR_MESSAGE, message);
        this.setResult(RESULT_ERROR, result);
        finish();
    }

    private void releaseAllResources() {
        if (mVideoRecorder != null) {
            mVideoRecorder.releaseAllResources();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(SAVED_RECORDED_BOOLEAN, mVideoRecorded);
        savedInstanceState.putString(SAVED_OUTPUT_FILENAME, mVideoFile.getFullPath());
        super.onSaveInstanceState(savedInstanceState);
    }

    protected CaptureConfiguration generateCaptureConfiguration() {
        CaptureConfiguration returnConfiguration = QualityPreferences.getConfiguration();
        return returnConfiguration;
    }

    private boolean generateVideoRecorded(final Bundle savedInstanceState) {
        if (savedInstanceState == null) return false;
        return savedInstanceState.getBoolean(SAVED_RECORDED_BOOLEAN, false);
    }

    protected VideoFile generateOutputFile(Bundle savedInstanceState) {
        VideoFile returnFile;
        if (savedInstanceState != null) {
            returnFile = new VideoFile(savedInstanceState.getString(SAVED_OUTPUT_FILENAME));
        } else {
            returnFile = new VideoFile(this.getIntent().getStringExtra(EXTRA_OUTPUT_FILENAME));
        }
        // TODO: add checks to see if outputfile is writeable
        return returnFile;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private boolean generateIsFrontFacingCameraSelected() {
        return getIntent().getBooleanExtra(EXTRA_FRONTFACINGCAMERASELECTED, false);
    }

    public Bitmap getVideoThumbnail() {
        final Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(mVideoFile.getFullPath(),
                Thumbnails.FULL_SCREEN_KIND);
        if (thumbnail == null) {
            CLog.d(CLog.ACTIVITY, "Failed to generate video preview");
        }
        return thumbnail;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.setResult(resultCode, data);
        finish();
    }

    /**
     * 内存变化
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void fileMemoryChannge(MemoryBean allMemoery) {
        String fileSize = null;
        File nowSaveFile = mVideoRecorder.getNowSaveFile();
        if (nowSaveFile != null) {
            long length = nowSaveFile.length();
            fileSize = UnitHelper.formatterFileSize(length);
        } else {
            fileSize = "0";
        }
        String allSize = UnitHelper.formatterFileSize(allMemoery.getAvailableInternalMemorySize());
        mVideoCaptureView.setFileAndAllSize(fileSize, allSize);
        float s = allMemoery.getAvailableInternalMemorySize() *1.0f / allMemoery.getTotalInternalMemorySize();
        if (isVideoing) {
            if (s < 0.1f) {
                mVideoCaptureView.showMemoryError();
            } else if (s < 0.03f) {
                onRecordButtonClicked();
            }
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApp.isVideo = false;
        FileMemoryEvent.getInstance().unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MyApp.isVideo = false;
        if (SettingPreferences.isAuto()) {
            mVideoCaptureView.closeAutoVideo();
        }
    }
}
