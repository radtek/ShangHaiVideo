package nss.mobile.video.http.ali;

import java.util.List;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/12/20
 *
 * @author ql
 */
public class AliPlayInfoResult {
    private String RequestId;
    private AliVideoBean VideoBase;
    private PlayInfoListBean PlayInfoList;

    public PlayInfoListBean getPlayInfoList() {
        return PlayInfoList;
    }

    public void setPlayInfoList(PlayInfoListBean playInfoList) {
        PlayInfoList = playInfoList;
    }

    public String getRequestId() {
        return RequestId;
    }

    public void setRequestId(String requestId) {
        RequestId = requestId;
    }

    public AliVideoBean getVideoBase() {
        return VideoBase;
    }

    public void setVideoBase(AliVideoBean videoBase) {
        VideoBase = videoBase;
    }

    public static class PlayInfoListBean {
        private List<AliPlayInfo> PlayInfo;

        public List<AliPlayInfo> getPlayInfo() {
            return PlayInfo;
        }

        public void setPlayInfo(List<AliPlayInfo> playInfo) {
            this.PlayInfo = playInfo;
        }
    }

}
