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

package nss.mobile.video.video.recorder;

import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.SurfaceHolder;

import nss.mobile.video.video.CLog;
import nss.mobile.video.video.VideoFile;
import nss.mobile.video.video.camera.CameraWrapper;
import nss.mobile.video.video.camera.OpenCameraException;
import nss.mobile.video.video.camera.PrepareCameraException;
import nss.mobile.video.video.camera.RecordingSize;
import nss.mobile.video.video.configuration.CaptureConfiguration;
import nss.mobile.video.video.preview.CapturePreview;
import nss.mobile.video.video.preview.CapturePreviewInterface;

import java.io.File;
import java.io.IOException;

public class VideoRecorder implements OnInfoListener, CapturePreviewInterface {

    private CameraWrapper mCameraWrapper;
    private CapturePreview mVideoCapturePreview;

    private  CaptureConfiguration mCaptureConfiguration;
    private  VideoFile mVideoFile;

    private MediaRecorder mRecorder;
    private boolean mRecording = false;
    private final VideoRecorderInterface mRecorderInterface;
    private File nowSaveFile;//当前存储地址

    public VideoRecorder(VideoRecorderInterface recorderInterface,
                         CaptureConfiguration captureConfiguration,
                         VideoFile videoFile,
                         CameraWrapper cameraWrapper,
                         SurfaceHolder previewHolder,
                         boolean useFrontFacingCamera) {
        mCaptureConfiguration = captureConfiguration;
        mRecorderInterface = recorderInterface;
        mVideoFile = videoFile;
        mCameraWrapper = cameraWrapper;


        initializeCameraAndPreview(previewHolder, useFrontFacingCamera);
    }

    protected void initializeCameraAndPreview(SurfaceHolder previewHolder, boolean useFrontFacingCamera) {
        try {
            mCameraWrapper.openCamera(useFrontFacingCamera);
        } catch (final OpenCameraException e) {
            e.printStackTrace();
            mRecorderInterface.onRecordingFailed(e.getMessage());
            return;
        }

        mVideoCapturePreview = new CapturePreview(this, mCameraWrapper, previewHolder);
    }

    public void toggleRecording() throws AlreadyUsedException {
        if (mCameraWrapper == null) {
            throw new AlreadyUsedException();
        }

        if (isRecording()) {
            stopRecording(null);
        } else {
            startRecording();
        }
    }

    protected void startRecording() {
        mRecording = false;

        if (!initRecorder()) return;
        if (!prepareRecorder()) return;
        if (!startRecorder()) return;

        mRecording = true;
        mRecorderInterface.onRecordingStarted();
        CLog.d(CLog.RECORDER, "Successfully started recording - outputfile: " + mVideoFile.getFullPath());
    }

    public void stopRecording(String message) {
        if (!isRecording()) return;

        try {
            getMediaRecorder().stop();
            mRecorderInterface.onRecordingSuccess();
            CLog.d(CLog.RECORDER, "Successfully stopped recording - outputfile: " + mVideoFile.getFullPath());
        } catch (final RuntimeException e) {
            CLog.d(CLog.RECORDER, "Failed to stop recording");
        }

        mRecording = false;
        mRecorderInterface.onRecordingStopped(message);
    }

    private boolean initRecorder() {
        try {
            mCameraWrapper.prepareCameraForRecording();
        } catch (final PrepareCameraException e) {
            e.printStackTrace();
            mRecorderInterface.onRecordingFailed("Unable to record video");
            CLog.e(CLog.RECORDER, "Failed to initialize recorder - " + e.toString());
            return false;
        }

        setMediaRecorder(new MediaRecorder());
        configureMediaRecorder(getMediaRecorder(), mCameraWrapper.getCamera());

        CLog.d(CLog.RECORDER, "MediaRecorder successfully initialized");
        return true;
    }

    @SuppressWarnings("deprecation")
    protected void configureMediaRecorder(final MediaRecorder recorder, android.hardware.Camera camera)
            throws IllegalStateException, IllegalArgumentException {
        recorder.setCamera(camera);
        recorder.setAudioSource(mCaptureConfiguration.getAudioSource());
        recorder.setVideoSource(mCaptureConfiguration.getVideoSource());

        CamcorderProfile baseProfile = mCameraWrapper.getBaseRecordingProfile();
        baseProfile.fileFormat = mCaptureConfiguration.getOutputFormat();
        // TODO: 2018/10/25 设置竖屏录制全屏视频，将 宽高 替换，原因为查询到
        RecordingSize size = mCameraWrapper.getSupportedRecordingSize(mCaptureConfiguration.getVideoWidth(), mCaptureConfiguration.getVideoHeight());
//        RecordingSize size = mCameraWrapper.getSupportedRecordingSize(mCaptureConfiguration.getVideoHeight(), mCaptureConfiguration.getVideoWidth());
        baseProfile.videoFrameWidth = size.width;
        baseProfile.videoFrameHeight = size.height;
        baseProfile.videoBitRate = mCaptureConfiguration.getVideoBitrate();

        baseProfile.audioCodec = mCaptureConfiguration.getAudioEncoder();
        baseProfile.videoCodec = mCaptureConfiguration.getVideoEncoder();

        recorder.setProfile(baseProfile);
        recorder.setMaxDuration(mCaptureConfiguration.getMaxCaptureDuration());
         nowSaveFile = mVideoFile.getFile();
        recorder.setOutputFile(nowSaveFile);
        int rotationCorrection = mCameraWrapper.getRotationCorrection();
//        int rotationCorrection = mCameraWrapper.getRotationCorrectionF();
        recorder.setOrientationHint(rotationCorrection);
        recorder.setVideoFrameRate(mCaptureConfiguration.getVideoFPS());
        try {
            recorder.setMaxFileSize(mCaptureConfiguration.getMaxCaptureFileSize());
        } catch (IllegalArgumentException e) {
            CLog.e(CLog.RECORDER, "Failed to set max filesize - illegal argument: " + mCaptureConfiguration.getMaxCaptureFileSize());
        } catch (RuntimeException e2) {
            CLog.e(CLog.RECORDER, "Failed to set max filesize - runtime exception");
        }
        recorder.setOnInfoListener(this);
    }

    private boolean prepareRecorder() {
        try {
            getMediaRecorder().prepare();
            CLog.d(CLog.RECORDER, "MediaRecorder successfully prepared");
            return true;
        } catch (final IllegalStateException e) {
            e.printStackTrace();
            CLog.e(CLog.RECORDER, "MediaRecorder preparation failed - " + e.toString());
            return false;
        } catch (final IOException e) {
            e.printStackTrace();
            CLog.e(CLog.RECORDER, "MediaRecorder preparation failed - " + e.toString());
            return false;
        }
    }

    private boolean startRecorder() {
        try {
            getMediaRecorder().start();
            CLog.d(CLog.RECORDER, "MediaRecorder successfully started");
            return true;
        } catch (final IllegalStateException e) {
            e.printStackTrace();
            CLog.e(CLog.RECORDER, "MediaRecorder start failed - " + e.toString());
            return false;
        } catch (final RuntimeException e2) {
            e2.printStackTrace();
            CLog.e(CLog.RECORDER, "MediaRecorder start failed - " + e2.toString());
            mRecorderInterface.onRecordingFailed("Unable to record video with given settings");
            return false;
        }
    }

    protected boolean isRecording() {
        return mRecording;
    }

    protected void setMediaRecorder(MediaRecorder recorder) {
        mRecorder = recorder;
    }

    protected MediaRecorder getMediaRecorder() {
        return mRecorder;
    }

    public void releaseRecorderResources() {
        MediaRecorder recorder = getMediaRecorder();
        if (recorder != null) {
            recorder.release();
            setMediaRecorder(null);
        }
    }

    public void releaseAllResources() {
        if (mVideoCapturePreview != null) {
            mVideoCapturePreview.releasePreviewResources();
        }
        if (mCameraWrapper != null) {
            mCameraWrapper.releaseCamera();
            mCameraWrapper = null;
        }
        releaseRecorderResources();
        CLog.d(CLog.RECORDER, "Released all resources");
    }
    public boolean isRelease(){
        return mCameraWrapper == null;
    }

    @Override
    public void onCapturePreviewFailed() {
        mRecorderInterface.onRecordingFailed("Unable to show camera preview");
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        switch (what) {
            case MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN:
                // NOP
                break;
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                CLog.d(CLog.RECORDER, "MediaRecorder max duration reached");
                stopRecording("Capture stopped - Max duration reached");
                break;
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                CLog.d(CLog.RECORDER, "MediaRecorder max filesize reached");
                stopRecording("Capture stopped - Max file size reached");
                break;
            default:
                break;
        }
    }

    public CaptureConfiguration getCaptureConfiguration() {
        return mCaptureConfiguration;
    }


    public void setVideoCapturePreview(CapturePreview videoCapturePreview) {
        this.mVideoCapturePreview = videoCapturePreview;
    }

    public File getNowSaveFile(){
        return nowSaveFile;
    }
    public void setVideoFile(VideoFile videoFile) {
        this.mVideoFile = videoFile;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void pause() {
        getMediaRecorder().pause();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void again() {
        getMediaRecorder().resume();
    }
}