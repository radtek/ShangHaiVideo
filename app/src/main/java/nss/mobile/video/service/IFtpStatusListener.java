package nss.mobile.video.service;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/13
 *
 * @author ql
 */
public interface IFtpStatusListener {

    void ftpConnectSuccess();

    void ftpConnectFailed(Exception e);

    //服务连接断开
    void ftpOff();

    void ftpDisconnect();

}
