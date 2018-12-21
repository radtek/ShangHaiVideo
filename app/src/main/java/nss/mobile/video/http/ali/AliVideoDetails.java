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
    private AliVideoBean Video;

    public String getRequestId() {
        return RequestId;
    }

    public void setRequestId(String RequestId) {
        this.RequestId = RequestId;
    }

    public AliVideoBean getVideo() {
        return Video;
    }

    public void setVideo(AliVideoBean Video) {
        this.Video = Video;
    }


}
