package nss.mobile.video.http.ali;

import java.util.List;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/12/15
 *
 * @author ql
 */
public class AliVideoDetails {


    /**
     * RequestId : D9F7BCCA-EC98-4477-9D6F-E259644452CF
     * Video : {"CoverURL":"http://sfj.meeting365.net/7356cd728744406e9f458d0ba865f403/covers/93f6a741de7741fa8004325f7b9657aa-00001.jpg","DownloadSwitch":"on","PreprocessStatus":"UnPreprocess","ModificationTime":"2018-12-15T07:56:57Z","VideoId":"7356cd728744406e9f458d0ba865f403","ModifyTime":"2018-12-15 15:56:57","Title":"androidTest1","CreationTime":"2018-12-15T07:56:20Z","Status":"Normal","TemplateGroupId":"e9faf6230be6d55704bb9b0c66cf7e82","RegionId":"cn-shanghai","Duration":5.949,"CreateTime":"2018-12-15 15:56:20","Snapshots":{"Snapshot":["http://sfj.meeting365.net/7356cd728744406e9f458d0ba865f403/covers/93f6a741de7741fa8004325f7b9657aa-00001.jpg"]},"StorageLocation":"outin-03739bd988bb11e88d0900163e1a65b6.oss-cn-shanghai.aliyuncs.com","Size":401717}
     */

    private String RequestId;
    private VideoBean Video;

    public String getRequestId() {
        return RequestId;
    }

    public void setRequestId(String RequestId) {
        this.RequestId = RequestId;
    }

    public VideoBean getVideo() {
        return Video;
    }

    public void setVideo(VideoBean Video) {
        this.Video = Video;
    }

    public static class VideoBean {
        /**
         * CoverURL : http://sfj.meeting365.net/7356cd728744406e9f458d0ba865f403/covers/93f6a741de7741fa8004325f7b9657aa-00001.jpg
         * DownloadSwitch : on
         * PreprocessStatus : UnPreprocess
         * ModificationTime : 2018-12-15T07:56:57Z
         * VideoId : 7356cd728744406e9f458d0ba865f403
         * ModifyTime : 2018-12-15 15:56:57
         * Title : androidTest1
         * CreationTime : 2018-12-15T07:56:20Z
         * Status : Normal
         * TemplateGroupId : e9faf6230be6d55704bb9b0c66cf7e82
         * RegionId : cn-shanghai
         * Duration : 5.949
         * CreateTime : 2018-12-15 15:56:20
         * Snapshots : {"Snapshot":["http://sfj.meeting365.net/7356cd728744406e9f458d0ba865f403/covers/93f6a741de7741fa8004325f7b9657aa-00001.jpg"]}
         * StorageLocation : outin-03739bd988bb11e88d0900163e1a65b6.oss-cn-shanghai.aliyuncs.com
         * Size : 401717
         */

        private String CoverURL;
        private String DownloadSwitch;
        private String PreprocessStatus;
        private String ModificationTime;
        private String VideoId;
        private String ModifyTime;
        private String Title;
        private String CreationTime;
        private String Status;
        private String TemplateGroupId;
        private String RegionId;
        private double Duration;
        private String CreateTime;
        private SnapshotsBean Snapshots;
        private String StorageLocation;
        private int Size;

        public String getCoverURL() {
            return CoverURL;
        }

        public void setCoverURL(String CoverURL) {
            this.CoverURL = CoverURL;
        }

        public String getDownloadSwitch() {
            return DownloadSwitch;
        }

        public void setDownloadSwitch(String DownloadSwitch) {
            this.DownloadSwitch = DownloadSwitch;
        }

        public String getPreprocessStatus() {
            return PreprocessStatus;
        }

        public void setPreprocessStatus(String PreprocessStatus) {
            this.PreprocessStatus = PreprocessStatus;
        }

        public String getModificationTime() {
            return ModificationTime;
        }

        public void setModificationTime(String ModificationTime) {
            this.ModificationTime = ModificationTime;
        }

        public String getVideoId() {
            return VideoId;
        }

        public void setVideoId(String VideoId) {
            this.VideoId = VideoId;
        }

        public String getModifyTime() {
            return ModifyTime;
        }

        public void setModifyTime(String ModifyTime) {
            this.ModifyTime = ModifyTime;
        }

        public String getTitle() {
            return Title;
        }

        public void setTitle(String Title) {
            this.Title = Title;
        }

        public String getCreationTime() {
            return CreationTime;
        }

        public void setCreationTime(String CreationTime) {
            this.CreationTime = CreationTime;
        }

        public String getStatus() {
            return Status;
        }

        public void setStatus(String Status) {
            this.Status = Status;
        }

        public String getTemplateGroupId() {
            return TemplateGroupId;
        }

        public void setTemplateGroupId(String TemplateGroupId) {
            this.TemplateGroupId = TemplateGroupId;
        }

        public String getRegionId() {
            return RegionId;
        }

        public void setRegionId(String RegionId) {
            this.RegionId = RegionId;
        }

        public double getDuration() {
            return Duration;
        }

        public void setDuration(double Duration) {
            this.Duration = Duration;
        }

        public String getCreateTime() {
            return CreateTime;
        }

        public void setCreateTime(String CreateTime) {
            this.CreateTime = CreateTime;
        }

        public SnapshotsBean getSnapshots() {
            return Snapshots;
        }

        public void setSnapshots(SnapshotsBean Snapshots) {
            this.Snapshots = Snapshots;
        }

        public String getStorageLocation() {
            return StorageLocation;
        }

        public void setStorageLocation(String StorageLocation) {
            this.StorageLocation = StorageLocation;
        }

        public int getSize() {
            return Size;
        }

        public void setSize(int Size) {
            this.Size = Size;
        }

        public static class SnapshotsBean {
            private List<String> Snapshot;

            public List<String> getSnapshot() {
                return Snapshot;
            }

            public void setSnapshot(List<String> Snapshot) {
                this.Snapshot = Snapshot;
            }
        }
    }
}
