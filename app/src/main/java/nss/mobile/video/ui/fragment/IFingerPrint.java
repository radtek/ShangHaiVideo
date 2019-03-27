package nss.mobile.video.ui.fragment;

import android.graphics.Bitmap;

import nss.mobile.video.card.authentication.api.sfz.ParseSFZAPI;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2019/2/25
 *
 * @author ql
 */
public interface IFingerPrint {

    boolean openFingerDev();

    boolean closeFingerDev();

    void checkFinger(ParseSFZAPI.People people);


    void setFingerListener(IBHMFingerListener listener);

    void onPause();

    void onResume();

    public interface IBHMFingerListener {
        void checkSuccess();

        void checkFailed(String error);

        void printFinger(Bitmap bitmap);
    }

}
