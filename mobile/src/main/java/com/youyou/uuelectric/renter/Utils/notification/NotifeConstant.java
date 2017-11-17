package com.youyou.uuelectric.renter.Utils.notification;

/**
 * Created by liuchao on 2015/10/21.
 * 通知栏常量
 */
public class NotifeConstant {
    /**
     * 通知栏ID
     */
    public static final int NOTIFICATION_ID = 1030;
    /**
     * 开始显示时间
     */
    public static final String START_SHOW_TIME = "startShowTime";
    /**
     * 有效期内SCHEME地址
     */
    public static final String VALID_ACTION_URL = "validActionUrl";
    /**
     * 失效SCHEME地址
     */
    public static final String INVALID_ACTION_URL = "invalidActionUrl";
    /**
     * 截止的有效日期，单位秒，当客户端本地时间超过该值，则push已经失效
     */
    public static final String VALID_TIME = "validTime";
    /**
     * 通知receiver接收的intent参数名
     */
    public static final String REAL_INTENT = "realIntent";


    /**
     * 返回的随机数屏蔽循环时的影响
     *
     * @return
     */
    public static int getRadom() {
        return (int) (System.currentTimeMillis() / 1000) + (int) (Math.random() * 1000);
    }
}
