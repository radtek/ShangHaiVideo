package nss.mobile.video.bean;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.util.List;

import nss.mobile.video.ui.adapter.IFile;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/12/14
 *
 * @author ql
 */
public class AliFileBean extends LitePalSupport implements IFile {
    public static final String STATUS_NORMAL = "未上传";
    public static final String STATUS_UPLOADING = "上传中";
    public static final String STATUS_OVER = "上传完成";
    public static final String STATUS_WANTING = "等待中";
    public static final String STATUS_ERROR = "上传失败";
    private int id;
    private String filePath;
    private String code;
    private String startTime;
    private String fileName;
    private String upstatus;
    private String playTime;
    private boolean select;
    private String fileSize;

    private String aliVideoId;
    private String videVod; //播放地址


    private int submitServerStatus;//上传到自有服务器状态 -1失败，0，未上传，1上传成功


    public static List<AliFileBean> getStatus(String status) {
        return LitePal.where("upstatus = ?", status).find(AliFileBean.class);

    }

    public static AliFileBean getFile(AliFileBean d) {
        List<AliFileBean> ts = LitePal.where("filePath = ?", d.getFilePath().toString()).find(AliFileBean.class);
        if (ts.size() == 0) {
            return null;
        }
        return ts.get(0);
    }

    public static AliFileBean getFile(String filePath) {
        List<AliFileBean> aliFileBeans = LitePal.where("filePath = ?", filePath).find(AliFileBean.class);
        if (aliFileBeans.size() == 0) {
            return null;
        }
        return aliFileBeans.get(0);
    }

    public void saveSingle() {
        List<AliFileBean> ts = LitePal.where("filePath = ?", getFilePath().toString()).find(AliFileBean.class);
        if (ts.size() == 0) {
            save();
            return;
        }
        AliFileBean aliFileBean = ts.get(0);
        update(aliFileBean.getId());
    }

    public String getAliVideoId() {
        return aliVideoId;
    }

    public int getSubmitServerStatus() {
        return submitServerStatus;
    }

    public void setAliVideoId(String aliVideoId) {
        this.aliVideoId = aliVideoId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setUpstatus(String upstatus) {
        this.upstatus = upstatus;
    }

    public void setPlayTime(String playTime) {
        this.playTime = playTime;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }


    @Override
    public CharSequence getFilePath() {
        return filePath;
    }


    @Override
    public CharSequence getCode() {
        return code;
    }

    @Override
    public CharSequence getStartTime() {
        return startTime;
    }

    @Override
    public CharSequence getFileName() {
        return fileName;
    }

    @Override
    public CharSequence getFileSize() {
        return fileSize;
    }

    @Override
    public CharSequence getUpStatus() {
        return upstatus;
    }

    @Override
    public CharSequence getPlayTime() {
        if (playTime == null) {
            return "";
        }
        return playTime;
    }

    @Override
    public boolean isSelect() {
        return select;
    }


    public void setVideVod(String videVod) {
        this.videVod = videVod;
    }

    public String getVideVod() {
        return videVod;
    }

    public void setSubmitServerStatus(int submitServerStatus) {
        this.submitServerStatus = submitServerStatus;
    }
}
