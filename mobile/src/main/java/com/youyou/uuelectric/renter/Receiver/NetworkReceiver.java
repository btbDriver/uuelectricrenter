package com.youyou.uuelectric.renter.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;
import com.youyou.uuelectric.renter.Utils.Support.L;

import de.greenrobot.event.EventBus;

/**
 * 监听网络状态变化广播
 */
public class NetworkReceiver extends BroadcastReceiver {
    /**
     * 用于保存上一次接收到的广播的网络状态信息
     */
    private static NetworkInfo preNetworkInfo = null;
    /**
     * 控制变量，判断当前是否是第一次接受网络状态变化的广播
     */
    private static boolean isFirst = true;

    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
        BaseEvent baseEvent = new BaseEvent(EventBusConstant.EVENT_TYPE_NETWORK_STATUS);
        // 由于网络状态变化时会收到对个广播消息，做消息拦截
        if (isFirst) {
            preNetworkInfo = activeInfo;
            isFirst = false;
        } else {
            if (activeInfo == null && preNetworkInfo == null)
                return;
            if (activeInfo != null && preNetworkInfo != null)
                return;
            preNetworkInfo = activeInfo;
        }
        if (activeInfo == null) {
            L.i("监听到网络已经断开...");
            baseEvent.setExtraData("close");
        } else {
            L.i("监听到网络已打开...");
            baseEvent.setExtraData("open");
        }
        EventBus.getDefault().post(baseEvent);

    }
}
