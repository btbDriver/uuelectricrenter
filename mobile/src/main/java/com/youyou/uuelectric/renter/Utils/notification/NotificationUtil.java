package com.youyou.uuelectric.renter.Utils.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.uu.facade.message.pb.common.LongMsgCommon;
import com.youyou.uuelectric.renter.UI.translate.NotificationActivity;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.main.MainActivity;

/**
 * Created by liuchao on 2015/10/8.
 */
public class NotificationUtil {

    /**
     * 通知栏发送通知消息
     *
     * @param context
     * @param outOfAppMsg
     */
    public static void showNotification(Context context, LongMsgCommon.OutOfAppMsg outOfAppMsg) {
        if (context == null || outOfAppMsg == null)
            return;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // 保存notification信息
        Intent realIntent = saveInfoToIntent(outOfAppMsg);
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.putExtra(NotifeConstant.REAL_INTENT, realIntent);
        // requestCode 设置不同的值，防止后面的notification对前面的notification影响
        PendingIntent pendingIntent = PendingIntent.getActivity(context, NotifeConstant.getRadom(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String contentTitle = outOfAppMsg.getTitle();
        String contentText = outOfAppMsg.getDescription();
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon);

        NotificationCompat.BigTextStyle bigTextStyle = new android.support.v4.app.NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(contentText);
        bigTextStyle.setSummaryText("查看更多");
        bigTextStyle.setBigContentTitle(contentTitle);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(contentTitle)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                //.setLargeIcon(icon)
                .setContentText(contentText)
                .setStyle(bigTextStyle)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.mipmap.icon)
                .setContentIntent(pendingIntent);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(outOfAppMsg.getId(), notification);
    }


    /**
     * 保存Intent信息
     */
    private static Intent saveInfoToIntent(LongMsgCommon.OutOfAppMsg outOfAppMsg) {
        Intent intent = new Intent();
        // 当前通知显示时的时间，由于通知会根据失效与否进行不同的跳转逻辑，所以需要结合当前显示时间showTime和validTime来判断如何跳转
        intent.putExtra("goto", MainActivity.GOTO_SCHEME);
        intent.putExtra(NotifeConstant.START_SHOW_TIME, System.currentTimeMillis());
        intent.putExtra(NotifeConstant.VALID_ACTION_URL, outOfAppMsg.getValidTimeActionUrl());
        intent.putExtra(NotifeConstant.INVALID_ACTION_URL, outOfAppMsg.getInvalidTimeActionUrl());
        intent.putExtra(NotifeConstant.VALID_TIME, Long.valueOf(outOfAppMsg.getValidTime()));

        return intent;
    }

}
