package com.youyou.uuelectric.renter.Utils.Support;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;


import com.youyou.uuelectric.renter.BuildConfig;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.ChannelUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TaurusXi on 2014/7/3.
 */
public class SysConfig
{
    /**
     * app级别各种信息储存Name,
     * 例如 版本号等
     */
    public static String SP_APP_NAME = "AppInfo";

    public static final boolean DEVELOP_MODE = BuildConfig.LOG_DEBUG;
    public static final String NETWORK_FAIL = "网络连接错误，请打开网络重试";
    public static final String BEI_JING_CITY = "北京市";
    public static String SD_IMAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/youyou/uucar/image/";
    public static String DOWN_APP_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/youyou/uucar/apk/";
    public static int widthPx;//屏幕宽度
    public static int heightPx;//屏幕高度

    public static String getAppVerSion(Context context) {
        String pkName = context.getPackageName();
        try {
            return context.getPackageManager().getPackageInfo(pkName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取系统UA信息
     *
     * @return
     */
    public static List<String> getSystemUa(Context context) {
        List<String> uaList = new ArrayList<String>();
        uaList.add("appVersion=" + SysConfig.getAppVerSion(context) + ";");
        uaList.add("systemVersion=" + Build.VERSION.SDK + ";");
        uaList.add("deviceModel=" + Build.MODEL + ";");
        uaList.add("brand=" + Build.BOARD + ";");
        uaList.add("widthPx=" + widthPx + ";");
        uaList.add("heightPx=" + heightPx + ";");
        uaList.add("type=" + "android" + ";");

        String ua = "A_" + SysConfig.getAppVerSion(context) + "&" + Build.VERSION.RELEASE + "&" + Build.MODEL + "&" + ChannelUtil.getChannel(context);
        uaList.add("ua=" + ua + ";");
        uaList.add("uuid=" + UUApp.initUUID(ua, context) + ";");
        // 添加经纬度信息到Cookie中
        uaList.add("lat=" + Config.lat);
        uaList.add("lng=" + Config.lng);
        return uaList;
    }

    /**
     * activity MetaData读取
     */
    public static String getChannel(Context context) {
        ApplicationInfo appInfo;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return appInfo.metaData.getString("UMENG_CHANNEL");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
