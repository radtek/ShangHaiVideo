package nss.mobile.video.info;


/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/8/18
 * 018200000104
 *
 * @author ql
 */
public class  UrlApi {

//    public static final String baseUrl = "http://nss.justice.org.cn/notary_test";
    public static final String baseUrl = "http://nss.justice.org.cn/notary";

    //    public static final String baseUrl = "http://192.168.6.111:10881";
    public static String getUrl() {
        return baseUrl;
    }

    //设备状态提交
    public static String status_submit = baseUrl + "/api/mobile-camera";

    //阿里云上传文件状态
    public static String file_status_submit = baseUrl + "/api/mobile-video";


    /**
     * 获得信息
     */
    public static String update_app = baseUrl + "/api/mobile-camera";

}
