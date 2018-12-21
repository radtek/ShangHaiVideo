package nss.mobile.video.http.ali;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/12/20
 *
 * @author ql
 */
public interface OnAliVideoPlayInfoListener {
    void onAliVideoPlaySuccess(AliPlayInfoResult aliPlayInfoResult);

    void onAliVideoPlayFailed(Exception e, String hint);
}
