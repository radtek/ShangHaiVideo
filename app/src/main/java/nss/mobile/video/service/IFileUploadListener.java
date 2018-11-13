package nss.mobile.video.service;

import java.io.File;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/9
 *
 * @author ql
 */
public interface IFileUploadListener {

    void uploadFileStart(File lastFile);

    void uploadFileError(File lastFile, Exception e);

    void uploadFileSuccess(File lastFile);

    void uploadingFile(float progress, long total, File lastFile);

}
