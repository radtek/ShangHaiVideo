package com.feiling.video.utils;


import com.feiling.video.base.BaseActivity;

import java.util.ArrayList;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/6/5
 *
 * @author ql
 */

public class ActivityUtils {
    private static final ArrayList<BaseActivity> queue = new ArrayList<>();

    public static void addAty(BaseActivity aty) {
        queue.add(aty);
    }

    public static void remove(BaseActivity aty) {
        queue.remove(aty);
    }

    public static void quit() {
        System.exit(0);
    }

    public static void singleAty(BaseActivity aty) {
        for (BaseActivity baseActivity : queue) {
            if (baseActivity.equals(aty)) {
                continue;
            }
            baseActivity.finish();
        }
    }
}
