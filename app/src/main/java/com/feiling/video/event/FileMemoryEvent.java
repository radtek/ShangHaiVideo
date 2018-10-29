package com.feiling.video.event;

import com.feiling.video.utils.FileMeoryUtils;

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


    public void postMemoryEvent(Long l) {
        post(l);
    }
}
