package nss.mobile.video.base;

import java.util.ArrayList;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/6/1
 *
 * @author ql
 */

public interface IAlbum {

    /**
     * 打开相册
     */
    void toOpenAlbum();

    /**
     * 选择多张图片
     *
     * @param imgs
     */
    void pickImage(ArrayList<String> imgs);

    void openAlbumMore(int sSAVE_IMAGE_MAX_SIZE, ArrayList<String> srcList);
}
