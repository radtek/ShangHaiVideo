package nss.mobile.video.bean.db;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.util.List;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/12
 *
 * @author ql
 */
public class UploadFile extends LitePalSupport {
    //准备上传
    public static final String UPLOAD_READY = "0";
    //正在上传
    public static final String UPLOAD_ING = "4";
    //上传完成;
    public static final String UPLOAD_END = "1";
    //上传失败
    public static final String UPLOAD_FAILED = "2";
    //暂停上传
    public static final String UPLOAD_PAUSE = "3";

    private int id;

    private String filePath;
    private String status;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static UploadFile selectByFilePath(String filePath) {
        List<UploadFile> uploadFiles = LitePal.where("filePath = ?", filePath).find(UploadFile.class);
        if (uploadFiles == null || uploadFiles.size() == 0) {
            return null;
        }
        return uploadFiles.get(0);
    }

    public static void deleteByFilePath(String filePath) {
        List<UploadFile> uploadFiles = LitePal.where("filePath = ?", filePath).find(UploadFile.class);
        if (uploadFiles.size() != 0) {
            for (UploadFile uploadFile : uploadFiles) {
                uploadFile.delete();
            }
        }
    }

    public static void deleteAll() {
        LitePal.deleteAll(UploadFile.class);
    }

    /**
     * 获得第一条 需要上传的文件呢
     *
     * @return
     */
    public static UploadFile firstUploadFile() {

        List<UploadFile> uploadFiles = LitePal.where("status = ? or status = ?", UPLOAD_ING,UPLOAD_READY).order("id ASC").find(UploadFile.class);
        if (uploadFiles.size() == 0) {
            return null;
        }
        return uploadFiles.get(0);
    }
}
