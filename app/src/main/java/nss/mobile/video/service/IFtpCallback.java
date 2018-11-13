package nss.mobile.video.service;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/12
 *
 * @author ql
 */
public interface IFtpCallback {
    void ftpFailed(String error);

    void ftpConnectSuccess();

    void ftpLoginFailed(String msg);
}
