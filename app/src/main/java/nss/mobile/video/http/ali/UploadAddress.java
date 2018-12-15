package nss.mobile.video.http.ali;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/12/15
 *
 * @author ql
 */
public class UploadAddress {


    /**
     * Endpoint : https://oss-cn-shanghai.aliyuncs.com
     * Bucket : outin-03739bd988bb11e88d0900163e1a65b6
     * FileName : sv/50edf80a-167b04618e6/50edf80a-167b04618e6.mp4
     */

    private String Endpoint;
    private String Bucket;
    private String FileName;

    public String getEndpoint() {
        return Endpoint;
    }

    public void setEndpoint(String Endpoint) {
        this.Endpoint = Endpoint;
    }

    public String getBucket() {
        return Bucket;
    }

    public void setBucket(String Bucket) {
        this.Bucket = Bucket;
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String FileName) {
        this.FileName = FileName;
    }
}
