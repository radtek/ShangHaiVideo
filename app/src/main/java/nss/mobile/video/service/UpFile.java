package nss.mobile.video.service;

import java.io.File;
import java.util.Objects;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/12
 *
 * @author ql
 */
public class UpFile {
    private File file;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpFile that = (UpFile) o;
        return Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {

        return Objects.hash(file);
    }
}
