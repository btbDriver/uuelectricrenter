package com.youyou.uuelectric.renter.Utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by liuchao on 2016/3/10.
 * app版本工具类
 */
public class VersionUtils {

    /**
     * 获取当前App版本号
     * @param mContext
     * @return
     */
    public static String getVersionName(Context mContext) {
        String result = "1.0.0";
        try {
            if (mContext == null) {
                return result;
            }
            // 获取packagemanager的实例
            PackageManager packageManager = mContext.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            return packInfo.versionName;
        } catch (Exception e) {
            return result;
        }
    }

    /**
     * 获取当前应用版本号
     * @param mContext
     * @return
     */
    public static int getVersionCode(Context mContext) {
        int result = 1;
        try {
            if (mContext == null) {
                return result;
            }
            // 获取packagemanager的实例
            PackageManager packageManager = mContext.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            return packInfo.versionCode;
        } catch (Exception e) {
            return result;
        }
    }
}
