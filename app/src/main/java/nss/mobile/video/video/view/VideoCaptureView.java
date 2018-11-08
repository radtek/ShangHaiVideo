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

package nss.mobile.video.video.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import nss.mobile.video.R;
import nss.mobile.video.utils.LogUtils;
import nss.mobile.video.video.preview.CapturePreview;

public class VideoCaptureView extends FrameLayout implements OnClickListener {

    private ImageView mRecordBtnIv;
    private ImageView mChangeCameraIv;

    private ImageView mPauseBtnIv;

    private TextView mAutoTimeTv;

    private ViewGroup mSizeGroup;
    private TextView mFileSizeTv;
    private TextView mAllSizeTv;

    private ImageView mAllMenuIv;
    private ImageView mWifiIv;
    private ImageView mQualityIv;//图片质量选择

    private SurfaceView mSurfaceView;
    private TextView mTimerTv;
    private Handler customHandler = new Handler();
    private long startTime = 0L;

    private RecordingButtonInterface mRecordingInterface;
    private boolean mShowTimer;
    private boolean isFrontCameraEnabled;
    private boolean isCameraSwitchingEnabled;
    private boolean isOpenAutoVideo = false;
    private long autoVideoTime = 10;//自动播放时间
    private long autoMax = 10;
    private Runnable mAutoVideoRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isOpenAutoVideo) {
                customHandler.removeCallbacks(this);
                return;
            }
            LogUtils.i(getClass().getName(), "自动播放还在运行");
            mAutoTimeTv.setText(String.format("%d s", autoVideoTime));
            if (autoVideoTime <= 0) {
                //开启自动播放
                mRecordingInterface.onRecordButtonClicked();
                customHandler.removeCallbacks(this);
                return;
            }
            customHandler.postDelayed(this, 1_000);
            autoVideoTime--;
        }
    };
    private boolean isShowFileSize;

    public VideoCaptureView(Context context) {
        super(context);
        initialize(context);
    }

    public VideoCaptureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public VideoCaptureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        final View videoCapture = View.inflate(context, R.layout.view_videocapture, this);

        mRecordBtnIv = (ImageView) videoCapture.findViewById(R.id.videocapture_recordbtn_iv);
        mChangeCameraIv = (ImageView) videoCapture.findViewById(R.id.change_camera_iv);
        mPauseBtnIv = videoCapture.findViewById(R.id.videocapture_recordbtn_pause_iv);
        mQualityIv = videoCapture.findViewById(R.id.videocapture_quality_iv);
        mAllMenuIv = videoCapture.findViewById(R.id.videocapture_allMenu_iv);
        mWifiIv = videoCapture.findViewById(R.id.videocapture_wifi_iv);

        mQualityIv.setOnClickListener(this);
        mAllMenuIv.setOnClickListener(this);
        mWifiIv.setOnClickListener(this);
        mPauseBtnIv.setOnClickListener(this);
        mRecordBtnIv.setOnClickListener(this);
        mChangeCameraIv.setOnClickListener(this);

        mSizeGroup = videoCapture.findViewById(R.id.videocapture_size_group);
        mAllSizeTv = videoCapture.findViewById(R.id.videocapture_allSize_tv);
        mFileSizeTv = videoCapture.findViewById(R.id.videocapture_fileSize_tv);

        mAutoTimeTv = videoCapture.findViewById(R.id.videocapture_auto_time_tv);

        mSurfaceView = (SurfaceView) videoCapture.findViewById(R.id.videocapture_preview_sv);

        mTimerTv = (TextView) videoCapture.findViewById(R.id.videocapture_timer_tv);
    }

    public void setRecordingButtonInterface(RecordingButtonInterface mBtnInterface) {
        this.mRecordingInterface = mBtnInterface;
    }

    public void setCameraSwitchingEnabled(boolean isCameraSwitchingEnabled) {
        this.isCameraSwitchingEnabled = isCameraSwitchingEnabled;
        mChangeCameraIv.setVisibility(isCameraSwitchingEnabled ? View.VISIBLE : View.INVISIBLE);
    }

    public void setCameraFacing(boolean isFrontFacing) {
        if (!isCameraSwitchingEnabled) return;
        isFrontCameraEnabled = isFrontFacing;
        mChangeCameraIv.setImageResource(isFrontCameraEnabled ?
                R.drawable.ic_change_camera_back :
                R.drawable.ic_change_camera_front);
    }

    public SurfaceHolder getPreviewSurfaceHolder() {
        return mSurfaceView.getHolder();
    }

    public void updateUINotRecording() {
        startTime = 0l;
        mPauseBtnIv.setVisibility(GONE);
        mQualityIv.setVisibility(VISIBLE);
        mTimerTv.setVisibility(GONE);
        mRecordBtnIv.setSelected(false);
        mChangeCameraIv.setVisibility(allowCameraSwitching() ? VISIBLE : INVISIBLE);
        mRecordBtnIv.setVisibility(View.VISIBLE);
        mSurfaceView.setVisibility(View.VISIBLE);
        if (isOpenAutoVideo) {
            autoVideoTime = autoMax;
            mAutoTimeTv.setVisibility(VISIBLE);
            customHandler.post(mAutoVideoRunnable);
        } else {
            mAutoTimeTv.setVisibility(View.GONE);
        }
        mWifiIv.setVisibility(View.VISIBLE);
        mSizeGroup.setVisibility(View.GONE);
        mAllMenuIv.setVisibility(View.VISIBLE);

    }

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            startTime++;
            long minutes = startTime / 60;
            long seconds = startTime % 60;
            updateRecordingTime(seconds, minutes);
            customHandler.postDelayed(this, 1000);
        }
    };

    public void updateUIRecordingOngoing() {
        mRecordBtnIv.setSelected(true);
        mAllMenuIv.setVisibility(View.GONE);

        mPauseBtnIv.setVisibility(View.VISIBLE);
        mRecordBtnIv.setVisibility(View.VISIBLE);
        mChangeCameraIv.setVisibility(View.INVISIBLE);
        mSurfaceView.setVisibility(View.VISIBLE);

        mQualityIv.setVisibility(View.GONE);
        mWifiIv.setVisibility(View.GONE);

        if (mShowTimer) {
            mTimerTv.setVisibility(View.VISIBLE);
            updateRecordingTime(0, 0);
            customHandler.postDelayed(updateTimerThread, 1000);
        }
        if (isOpenAutoVideo) {
            mAutoTimeTv.setVisibility(View.GONE);
            customHandler.removeCallbacks(mAutoVideoRunnable);
        }

        if (isShowFileSize) {
            mSizeGroup.setVisibility(View.VISIBLE);
        }
    }

    public void updateUIRecordingFinished(Bitmap videoThumbnail) {
        updateUINotRecording();

        customHandler.removeCallbacks(updateTimerThread);
    }

    @Override
    public void onClick(View v) {
        if (mRecordingInterface == null) return;

        if (v.getId() == mRecordBtnIv.getId()) {
            mRecordingInterface.onRecordButtonClicked();
        } else if (v.getId() == mChangeCameraIv.getId()) {
            isFrontCameraEnabled = !isFrontCameraEnabled;
            mChangeCameraIv.setImageResource(isFrontCameraEnabled ?
                    R.drawable.ic_change_camera_front : R.drawable.ic_change_camera_back);
            mRecordingInterface.onSwitchCamera(isFrontCameraEnabled);
        } else if (v.getId() == mPauseBtnIv.getId()) {
            //暂停
            mRecordingInterface.onRecordButtonPauseOrAgain();
        } else if (v.getId() == mAllMenuIv.getId()) {
            mRecordingInterface.onAllMenuButtonClick();
        } else if (v.getId() == mWifiIv.getId()) {
            mRecordingInterface.onScanningWifiButtonClick();
        } else if (v.getId() == mQualityIv.getId()) {
            mRecordingInterface.onQualityButtonClick();
        }

    }

    public void showTimer(boolean showTimer) {
        this.mShowTimer = showTimer;
    }

    private void updateRecordingTime(long seconds, long minutes) {
        mTimerTv.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
    }

    private boolean allowCameraSwitching() {
        return CapturePreview.isFrontCameraAvailable() && isCameraSwitchingEnabled;
    }

    public void setAutoVideoTime(long autoVideoTime) {
        this.autoMax = autoVideoTime;
        this.autoVideoTime = autoMax;

    }


    public void startAutoVideo() {
        isOpenAutoVideo = true;
        customHandler.post(mAutoVideoRunnable);
        mAutoTimeTv.setVisibility(View.VISIBLE);
    }

    public void closeAutoVideo() {
        isOpenAutoVideo = false;
        customHandler.removeCallbacks(mAutoVideoRunnable);
        mAutoTimeTv.setVisibility(View.GONE);

    }

    public void setIsShowFileSize(boolean isShow) {
        this.isShowFileSize = isShow;
    }

    public void setFileAndAllSize(CharSequence fileSize, CharSequence allSize) {
        mAllSizeTv.setText(allSize);
        mFileSizeTv.setText(fileSize);
    }

    /**
     * 暂停状态显示
     */
    public void updateUIRecordingPause() {
        mPauseBtnIv.setSelected(true);
        customHandler.removeCallbacks(updateTimerThread);
    }

    /**
     * 继续录制
     */
    public void updateUIRecordingAgain() {
        mPauseBtnIv.setSelected(false);
        customHandler.postDelayed(updateTimerThread, 1_000);
    }
}
