package com.feiling.video.utils.eventbus;

import org.greenrobot.eventbus.EventBus;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/8/23
 *
 * @author ql
 */
public class ChangeDetailsEventBus extends EventBus {
   private static ChangeDetailsEventBus changeDetails;
    public static ChangeDetailsEventBus getInstance(){
        if (changeDetails == null) {
            changeDetails = new ChangeDetailsEventBus();
        }
        return changeDetails;
    }

    public void postDetails(Integer i){
        post(i);
    }
}
