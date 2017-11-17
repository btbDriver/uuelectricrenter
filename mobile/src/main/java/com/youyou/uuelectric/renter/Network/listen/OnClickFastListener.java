package com.youyou.uuelectric.renter.Network.listen;

import android.view.View;

/**
 * Created by liuchao on 2015/9/1.
 * View点击的时候判断屏蔽快速点击事件
 */
public abstract class OnClickFastListener extends BaseClickListener {

    private static long lastClickTime;

    private static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 900) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    @Override
    public void onClick(View v) {
        if (OnClickFastListener.isFastDoubleClick()) {
            return;
        }

        onFastClick(v);
    }

    public abstract void onFastClick(View v);
}
