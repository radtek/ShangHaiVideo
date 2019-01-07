package nss.mobile.video.card.utils;

import android.content.res.Resources;

import nss.mobile.video.MyApp;

/**
 * 公共工具类
 * Created by kuyue on 2017/7/13 上午10:18.
 * 邮箱:595327086@qq.com
 **/
public class CommonUtils {

    /**
     * 获取dimens定义的大小
     * @param dimensionId
     * @return
     */
    public static int getPixelById(int dimensionId) {
        Resources resources = MyApp.getInstance().getResources();
        LogUtil.i("resources:"+resources);
        LogUtil.i("resources:"+dimensionId);
        int dimensionPixelSize = resources.getDimensionPixelSize(dimensionId);
        LogUtil.i("dimensionPixelSize:"+dimensionPixelSize);
        return dimensionPixelSize;
    }
}
