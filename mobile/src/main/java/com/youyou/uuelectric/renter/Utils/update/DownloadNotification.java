package com.youyou.uuelectric.renter.Utils.update;

import android.app.Activity;
import android.app.Notification;
import android.support.v4.app.NotificationCompat;

import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UUApp;

/**
 * Created by aaron on 16/5/4.
 * 下载app，notification更新
 */
public class DownloadNotification {

    public static final int notofyId = 10111;

    /**
     * 更新通知栏显示
     * @param title
     * @param content
     * @param notifyId
     */
    public static void showNotification(Activity mContext, String title, String content, int notifyId) {
        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.mipmap.icon)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(android.R.drawable.stat_sys_download);

        Notification notification = mNotifyBuilder.build();
        // notification.flags = Notification.FLAG_NO_CLEAR;
        UUApp.notificationManager.notify(notifyId, notification);
    }
}
