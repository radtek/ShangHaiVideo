package nss.mobile.video.event;

import nss.mobile.video.bean.MemoryBean;
import nss.mobile.video.utils.FileMeoryUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/10/25
 *
 * @author ql
 */
public class FileMemoryEvent extends EventBus {
    private static FileMemoryEvent instance;

    public static FileMemoryEvent getInstance() {
        if (instance == null) {
            synchronized (FileMemoryEvent.class) {
                if (instance == null) {
                    instance = new FileMemoryEvent();
                }
            }
        }
        return instance;
    }


    public void postMemoryEvent(MemoryBean l) {
        post(l);
    }
}
