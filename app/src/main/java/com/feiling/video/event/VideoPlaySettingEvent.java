package com.feiling.video.event;

import org.greenrobot.eventbus.EventBus;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/10/26
 *
 * @author ql
 */
public class VideoPlaySettingEvent extends EventBus {

    private static VideoPlaySettingEvent instance;

    public static VideoPlaySettingEvent getInstance() {
        if (instance == null) {
            synchronized (VideoPlaySettingEvent.class) {
                if (instance == null) {
                    instance = new VideoPlaySettingEvent();
                }
            }
        }

        return instance;
    }

    public void postSetting() {
        post(1);
    }

}
