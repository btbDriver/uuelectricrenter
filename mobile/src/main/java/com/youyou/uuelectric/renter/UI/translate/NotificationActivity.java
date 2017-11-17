package com.youyou.uuelectric.renter.UI.translate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.umeng.analytics.MobclickAgent;
import com.youyou.uuelectric.renter.UI.web.H5Constant;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.UMCountConstant;
import com.youyou.uuelectric.renter.Utils.notification.NotifeConstant;

/**
 * Created by liuchao on 2015/11/6.
 */
public class NotificationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.i("接收到通知点击事件...");
        Intent realIntent = getIntent().getParcelableExtra(NotifeConstant.REAL_INTENT);
        // 解析scheme并跳转
        gotoRealScheme(this, realIntent);
    }


    /**
     * notification中跳转SCHEME，根据有效时间判断跳转URL地址
     *  跳转之后更具网络请求判断用户当前状态
     */
    private void gotoRealScheme(Context context, Intent realIntent) {
        if (realIntent == null || context == null) {
            finish();
            return;
        }
        try {
            L.i("开始解析通知中的参数...");
            long startShowTime = realIntent.getLongExtra(NotifeConstant.START_SHOW_TIME, 0);
            // 有效期时间，单位:s（秒）
            long validTime = realIntent.getLongExtra(NotifeConstant.VALID_TIME, 0);
            long currentTime = System.currentTimeMillis();
            String validActionUrl = realIntent.getStringExtra(NotifeConstant.VALID_ACTION_URL);
            String invalidActionUrl = realIntent.getStringExtra(NotifeConstant.INVALID_ACTION_URL);
            Intent schemeIntent;
            L.i("开始根据URL构建Intent对象...");
            if ((currentTime - startShowTime) / 1000L <= validTime) {
                schemeIntent = H5Constant.buildSchemeFromUrl(validActionUrl);
            } else {
                schemeIntent = H5Constant.buildSchemeFromUrl(invalidActionUrl);
            }
            if (schemeIntent != null) {
                // 设置当前页面为通知栏打开
                Config.isNotificationOpen = true;
                context.startActivity(schemeIntent);
                finish();
                //对通知栏点击事件统计
                MobclickAgent.onEvent(context, UMCountConstant.PUSH_NOTIFICATION_CLICK);
            } else {
                finish();
            }
        } catch (Exception e) {
            // 异常情况下退出当前Activity
            finish();
        }
    }
}
