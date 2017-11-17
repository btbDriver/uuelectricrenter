package com.youyou.uuelectric.renter.Network.listen;

import android.view.View;

/**
 * Created by liuchao on 2015/9/2.
 * 通用自定义View处理点击事件
 */
public abstract class OnClickNormalListener extends BaseClickListener {

    @Override
    public void onClick(View view) {
        super.onClick(view);
        // 可在后续添加相应逻辑入：添加日志，添加打点功能，性能分析等等
        onNormalClick(view);
    }

    public abstract void onNormalClick(View v);
}
