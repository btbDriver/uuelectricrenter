package com.youyou.uuelectric.renter.UI.login;

import android.os.CountDownTimer;
import android.view.View;

/**
 * Created by liuchao on 2015/9/10.
 * 登陆界面倒计时工具类
 */
public class LoginPhoneCountDown extends CountDownTimer {
    /**
     * 验证码发送成功之后的间隔时间
     */
    public static final int TIME_COUNT = 61 * 1000;
    /**
     * 验证码不可用时显示的时间间隔
     */
    public static final int TIME_INT = 1 * 1000;
    /**
     * 20秒之后显示语音验证码连接
     */
    public static final int LATER_COUNT = 21 * 1000;

    LoginPhoneActivity context = null;
    long tCount = 0L;
    long iCount = 0L;

    public LoginPhoneCountDown(LoginPhoneActivity context) {
        super(TIME_COUNT, TIME_INT);// 参数依次为总时长,和计时的时间间隔
        tCount = TIME_COUNT;
        iCount = TIME_INT;
        this.context = context;
    }

    @Override
    public void onFinish() {// 计时完毕时触发
        context.validatePhone.setText("获取验证码");
        context.validatePhoneView.setOnClickListener(context.onClickNetworkListener);
    }

    @Override
    public void onTick(long millisUntilFinished) {// 计时过程显示
        tCount = tCount - iCount;

        if (TIME_COUNT - tCount >= LATER_COUNT) {
            context.validate_desc_text.setVisibility(View.VISIBLE);
        }
        context.validatePhone.setText(tCount / 1000 + "s");
    }
}
