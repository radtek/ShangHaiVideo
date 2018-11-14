package nss.mobile.video.service;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/13
 *
 * @author ql
 */
public interface IUploadAction {

    /**
     * 开始下载
     */
    void startUpload();


    /**
     * 停止下载
     */
    void stopUpload();


    /**
     * 移出需要上传的文件
     *
     * @param absPath 移出的文件的绝对路径
     */
    void removeUpload(String absPath);

    /**
     * 移出全部需要上传的文件;
     */
    void removeAll();

}
