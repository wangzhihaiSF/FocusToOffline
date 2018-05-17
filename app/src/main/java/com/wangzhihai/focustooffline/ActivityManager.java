package com.wangzhihai.focustooffline;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 王治海 on 2018/5/16.
 */
public class ActivityManager {

    private static List<Activity> activities = new ArrayList<>();

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public static void finishAllActivity() {
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }
}
