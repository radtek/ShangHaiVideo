package nss.mobile.video.service.ali;

import java.io.File;
import java.util.List;

import nss.mobile.video.service.UploadFileService;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/12/13
 *
 * @author ql
 */
public interface IUploadFileService {

    void startUploadFile();

    void removeFile(File file);

    void addFile(File file);

    void addFiles(List<File> files);

    void stopUpLoadFile();

    void addListener(AliUploadFileService.OnUploadFileListener l);

    void removeListener(AliUploadFileService.OnUploadFileListener l);
}
