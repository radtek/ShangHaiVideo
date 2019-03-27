package nss.mobile.video.card.authentication.base;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：李阳
 * 时间：2018/12/6
 * 描述：
 */
public class ActivityCollector {

    private static List<Activity> activities = new ArrayList<>();

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }
    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }
    public static void finishAll() {
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        activities.clear();
    }


}
