package nss.mobile.video.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/12
 *
 * @author ql
 */
public class FileBox {

    private List<UpFile> uploadFiles = new ArrayList<>();

    public void addFile(UpFile file) {
        for (UpFile uploadFile : uploadFiles) {
            if (uploadFile.getFile().getAbsolutePath().equals(file.getFile().getAbsolutePath())) {
                return;
            }
        }
        uploadFiles.add(file);
    }

    public void remove(File file) {

        uploadFiles.remove(file);
    }

    public UpFile next() {
        if (uploadFiles.size() == 0) {
            return null;
        }
        UpFile upFile = uploadFiles.get(0);
        uploadFiles.remove(0);
        return upFile;
    }


}
