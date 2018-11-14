package nss.mobile.video.bean.db;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/14
 *
 * @author ql
 */
public class FtpIp extends LitePalSupport {

    private int id;

    private String ip;
    @Column(defaultValue = "80")
    private String port;
    @Column(defaultValue = "")
    private String account;
    @Column(defaultValue = "")
    private String pw;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }
}
