package nss.mobile.video.ui.adapter;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/12/14
 *
 * @author ql
 */
public interface IFile {
    CharSequence getFilePath();

    CharSequence getCode();

    CharSequence getStartTime();

    CharSequence getFileName();

    CharSequence getFileSize();

    CharSequence getUpStatus();

    CharSequence getPlayTime();

    boolean isSelect();
}
