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

package nss.mobile.video.video.configuration;

import android.media.MediaRecorder;
import android.os.Parcel;
import android.os.Parcelable;

import nss.mobile.video.video.configuration.PredefinedCaptureConfigurations.CaptureQuality;
import nss.mobile.video.video.configuration.PredefinedCaptureConfigurations.CaptureResolution;

public class CaptureConfiguration implements Parcelable {

    public static final int MBYTE_TO_BYTE = 1024 * 1024;
    public static final int MSEC_TO_SEC = 1000;

    public static final int NO_DURATION_LIMIT = -1;
    public static final int NO_FILESIZE_LIMIT = -1;

    private int videoWidth = PredefinedCaptureConfigurations.WIDTH_1080P;
    private int videoHeight = PredefinedCaptureConfigurations.HEIGHT_1080P;
    private int bitrate = PredefinedCaptureConfigurations.BITRATE_HQ_1080P;
    private int maxDurationMs = NO_DURATION_LIMIT;
    private int maxFilesizeBytes = NO_FILESIZE_LIMIT;
    private boolean showTimer = true;
    private boolean allowFrontFacingCamera = true;
    private int videoFramerate = PredefinedCaptureConfigurations.FPS_30;     //Default FPS is 30.

    private int OUTPUT_FORMAT = MediaRecorder.OutputFormat.MPEG_4;
    private int AUDIO_SOURCE = MediaRecorder.AudioSource.DEFAULT;
    private int AUDIO_ENCODER = MediaRecorder.AudioEncoder.AAC;
    private int VIDEO_SOURCE = MediaRecorder.VideoSource.CAMERA;
    private int VIDEO_ENCODER = MediaRecorder.VideoEncoder.H264;

    private CaptureResolution resolution;
    private CaptureQuality quality;

    public static CaptureConfiguration getDefault() {
        return new CaptureConfiguration();
    }

    private CaptureConfiguration() {
        // Default configuration
    }

    @Deprecated
    public CaptureConfiguration(CaptureResolution resolution, CaptureQuality quality) {
        this.resolution = resolution;
        this.quality = quality;
        videoWidth = resolution.width;
        videoHeight = resolution.height;
        bitrate = resolution.getBitrate(quality);
    }

    @Deprecated
    public CaptureConfiguration(CaptureResolution resolution, CaptureQuality quality, int maxDurationSecs,
                                int maxFilesizeMb, boolean showTimer) {
        this(resolution, quality, maxDurationSecs, maxFilesizeMb, showTimer, false);
        this.showTimer = showTimer;
    }

    @Deprecated
    public CaptureConfiguration(CaptureResolution resolution, CaptureQuality quality, int maxDurationSecs,
                                int maxFilesizeMb, boolean showTimer, boolean allowFrontFacingCamera) {
        this(resolution, quality, maxDurationSecs, maxFilesizeMb);
        this.showTimer = showTimer;
        this.allowFrontFacingCamera = allowFrontFacingCamera;
    }

    @Deprecated
    public CaptureConfiguration(CaptureResolution resolution, CaptureQuality quality, int maxDurationSecs,
                                int maxFilesizeMb, boolean showTimer, boolean allowFrontFacingCamera,
                                int videoFPS) {
        this(resolution, quality, maxDurationSecs, maxFilesizeMb, showTimer, allowFrontFacingCamera);
        videoFramerate = videoFPS;
    }

    @Deprecated
    public CaptureConfiguration(CaptureResolution resolution, CaptureQuality quality, int maxDurationSecs,
                                int maxFilesizeMb) {
        this(resolution, quality);
        maxDurationMs = maxDurationSecs * MSEC_TO_SEC;
        maxFilesizeBytes = maxFilesizeMb * MBYTE_TO_BYTE;
    }

    @Deprecated
    public CaptureConfiguration(int videoWidth, int videoHeight, int bitrate) {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.bitrate = bitrate;
    }

    @Deprecated
    public CaptureConfiguration(int videoWidth, int videoHeight, int bitrate, int maxDurationSecs, int maxFilesizeMb) {
        this(videoWidth, videoHeight, bitrate);
        maxDurationMs = maxDurationSecs * MSEC_TO_SEC;
        maxFilesizeBytes = maxFilesizeMb * MBYTE_TO_BYTE;
    }

    /**
     * @return Width of the captured video in pixels
     */
    public int getVideoWidth() {
        return videoWidth;
    }

    /**
     * @return Height of the captured video in pixels
     */
    public int getVideoHeight() {
        return videoHeight;
    }

    /**
     * @return Bitrate of the captured video in bits per second
     */
    public int getVideoBitrate() {
        return bitrate;
    }

    /**
     * @return Maximum duration of the captured video in milliseconds
     */
    public int getMaxCaptureDuration() {
        return maxDurationMs;
    }

    /**
     * @return Maximum filesize of the captured video in bytes
     */
    public int getMaxCaptureFileSize() {
        return maxFilesizeBytes;
    }

    /**
     * @return If timer must be displayed during video capture
     */
    public boolean getShowTimer() {
        return showTimer;
    }

    /**
     * @return If front facing camera toggle must be displayed before capturing video
     */
    public boolean getAllowFrontFacingCamera() {
        return allowFrontFacingCamera;
    }

    public int getOutputFormat() {
        return OUTPUT_FORMAT;
    }

    public int getAudioSource() {
        return AUDIO_SOURCE;
    }

    public int getAudioEncoder() {
        return AUDIO_ENCODER;
    }

    public int getVideoSource() {
        return VIDEO_SOURCE;
    }

    public int getVideoEncoder() {
        return VIDEO_ENCODER;
    }

    public int getVideoFPS() {
        return videoFramerate;
    }

    public static class Builder {

        private final CaptureConfiguration configuration;

        public Builder(CaptureResolution resolution, CaptureQuality quality) {
            configuration = new CaptureConfiguration();
            configuration.setQuality(quality);
            configuration.setResolution(resolution);
            configuration.videoWidth = resolution.width;
            configuration.videoHeight = resolution.height;
            configuration.bitrate = resolution.getBitrate(quality);
        }

        public Builder(int width, int height, int bitrate) {
            configuration = new CaptureConfiguration();
            configuration.videoWidth = width;
            configuration.videoHeight = height;
            configuration.bitrate = bitrate;
        }

        public CaptureConfiguration build() {
            return configuration;
        }

        public Builder maxDuration(int maxDurationSec) {
            configuration.maxDurationMs = maxDurationSec * MSEC_TO_SEC;
            return this;
        }

        public Builder maxFileSize(int maxFileSizeMb) {
            configuration.maxFilesizeBytes = maxFileSizeMb * MBYTE_TO_BYTE;
            return this;
        }

        public Builder frameRate(int framesPerSec) {
            configuration.videoFramerate = framesPerSec;
            return this;
        }

        public Builder showRecordingTime() {
            configuration.showTimer = true;
            return this;
        }

        public Builder noCameraToggle() {
            configuration.allowFrontFacingCamera = false;
            return this;
        }
    }


    public CaptureResolution getResolution() {
        return resolution;
    }

    public void setResolution(CaptureResolution resolution) {
        this.resolution = resolution;
    }

    public CaptureQuality getQuality() {
        return quality;
    }

    public void setQuality(CaptureQuality quality) {
        this.quality = quality;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.videoWidth);
        dest.writeInt(this.videoHeight);
        dest.writeInt(this.bitrate);
        dest.writeInt(this.maxDurationMs);
        dest.writeInt(this.maxFilesizeBytes);
        dest.writeByte(this.showTimer ? (byte) 1 : (byte) 0);
        dest.writeByte(this.allowFrontFacingCamera ? (byte) 1 : (byte) 0);
        dest.writeInt(this.videoFramerate);
        dest.writeInt(this.OUTPUT_FORMAT);
        dest.writeInt(this.AUDIO_SOURCE);
        dest.writeInt(this.AUDIO_ENCODER);
        dest.writeInt(this.VIDEO_SOURCE);
        dest.writeInt(this.VIDEO_ENCODER);
        dest.writeInt(this.resolution == null ? -1 : this.resolution.ordinal());
        dest.writeInt(this.quality == null ? -1 : this.quality.ordinal());
    }

    protected CaptureConfiguration(Parcel in) {
        this.videoWidth = in.readInt();
        this.videoHeight = in.readInt();
        this.bitrate = in.readInt();
        this.maxDurationMs = in.readInt();
        this.maxFilesizeBytes = in.readInt();
        this.showTimer = in.readByte() != 0;
        this.allowFrontFacingCamera = in.readByte() != 0;
        this.videoFramerate = in.readInt();
        this.OUTPUT_FORMAT = in.readInt();
        this.AUDIO_SOURCE = in.readInt();
        this.AUDIO_ENCODER = in.readInt();
        this.VIDEO_SOURCE = in.readInt();
        this.VIDEO_ENCODER = in.readInt();
        int tmpResolution = in.readInt();
        this.resolution = tmpResolution == -1 ? null : CaptureResolution.values()[tmpResolution];
        int tmpQuality = in.readInt();
        this.quality = tmpQuality == -1 ? null : CaptureQuality.values()[tmpQuality];
    }

    public static final Creator<CaptureConfiguration> CREATOR = new Creator<CaptureConfiguration>() {
        @Override
        public CaptureConfiguration createFromParcel(Parcel source) {
            return new CaptureConfiguration(source);
        }

        @Override
        public CaptureConfiguration[] newArray(int size) {
            return new CaptureConfiguration[size];
        }
    };
}