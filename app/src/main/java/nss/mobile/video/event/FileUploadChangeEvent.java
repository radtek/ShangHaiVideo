package nss.mobile.video.event;

import org.greenrobot.eventbus.EventBus;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/12
 *
 * @author ql
 */
public class FileUploadChangeEvent extends EventBus {
    private static FileUploadChangeEvent instance;

    public static FileUploadChangeEvent getInstance() {
        if (instance == null) {
            synchronized (FileUploadChangeEvent.class) {
                if (instance == null) {
                    instance = new FileUploadChangeEvent();
                }
            }
        }
        return instance;
    }


    public void postMemoryEvent(Integer l) {
        post(l);
    }

}
