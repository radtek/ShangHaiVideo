package nss.mobile.video.ui;

import android.text.TextUtils;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/12
 *
 * @author ql
 */
public class FileBean {
    //单元内容:预览图、⽂文件名、开始时间、时⻓长、⽂文件⼤大⼩小、上传状态、校验码;
    private String icon;
    private String fileName;
    private String startTime;
    private String playTime;
    private String fileSize;
    private String upStatus;
    private String code;
    private String filePath;
    private boolean select;

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getPlayTime() {
        return playTime;
    }

    public void setPlayTime(String playTime) {
        this.playTime = playTime;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getUpStatus() {
        if (TextUtils.isEmpty(upStatus)) {
            return "未上传";
        }
        return upStatus;
    }

    public void setUpStatus(String upStatus) {
        this.upStatus = upStatus;
    }

    public String getCode() {
        if (TextUtils.isEmpty(code)) {
            return "校验码";
        }
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String filePath() {
        return filePath;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean isSelect) {
        select = isSelect;
    }
}
