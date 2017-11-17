package com.youyou.uuelectric.renter.Utils.task;

import android.app.Activity;

import com.youyou.uuelectric.renter.UI.main.MainActivity;
import com.youyou.uuelectric.renter.UI.translate.NativeAppActivity;
import com.youyou.uuelectric.renter.UUApp;

import java.util.List;

/**
 * Created by liuchao on 2016/1/20.
 */
public class ActivityUtil {

    /**
     * 关闭N个activities
     * @param closeNumberActivities 关闭activity的个数
     */
    public static void closeNumberActivities(int closeNumberActivities) {
        // 关闭个数小于1的时候直接跳出
        if (closeNumberActivities <= 0) {
            return;
        }
        List<Activity> mActivities = UUApp.getInstance().getActivitys();
        if (mActivities != null && mActivities.size() <= 1) {
            return;
        }

        try {
            int countTemp = 0;
            // 倒序遍历acitivty
            for (int i = mActivities.size() - 1; i >= 0; i--) {
                // 如果当前页面为NativeAppActivity，则直接finish();
                Activity mActivity = mActivities.get(i);
                if (mActivity != null && mActivity instanceof NativeAppActivity) {
                    mActivity.finish();
                    mActivities.remove(mActivity);
                }
                // 其他情况下finish掉activity
                else {
                    // 当前页面不能是最后一页
                    if (mActivities.size() > 1 && !(mActivity instanceof MainActivity)) {
                        mActivity.finish();
                        mActivities.remove(mActivity);
                        countTemp ++;
                    } else {
                        i = -1;
                    }
                }
                // 退出循环
                if (countTemp == closeNumberActivities) {
                    i = -1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
