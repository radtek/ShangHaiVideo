package nss.mobile.video.ui;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/12
 *
 * @author ql
 */
public class FileProgressBean extends FileBean {
    private int max;
    private int progress;
    private String uploadErrorHint;

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setUploadErrorHint(String uploadErrorHint) {
        this.uploadErrorHint = uploadErrorHint;
    }

    public String getUploadErrorHint() {
        if (uploadErrorHint == null) {
            return "";
        }
        return uploadErrorHint;
    }
}
