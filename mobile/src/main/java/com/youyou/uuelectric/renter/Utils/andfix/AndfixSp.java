package com.youyou.uuelectric.renter.Utils.andfix;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Created by liuchao on 2016/3/10.
 */
public class AndfixSp {

    // 客户端保存在本地的补丁包版本号
    public static final String ANDFIX_APATCH_VERSION = "andfix_apatch_version";
    // 客户端保存在本地的补丁包键值
    public static final String ANDFIX_APATCH_VERSION_KEY = "andfix_apatch_version_key";

    /**
     * 保存补丁包版本号
     * @param mContext
     */
    public static void putPatchVersion(Context mContext, String version) {
        if (mContext == null) {
            return;
        }
        if (TextUtils.isEmpty(version)) {
            return;
        }

        SharedPreferences sp = mContext.getSharedPreferences(ANDFIX_APATCH_VERSION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(ANDFIX_APATCH_VERSION_KEY, version);
        editor.apply();
    }

    /**
     * 获取补丁包版本号
     * @param mContext
     * @return
     */
    public static String getPatchVersion(Context mContext) {
        String DEFAULT_VERSION = "";
        String result = DEFAULT_VERSION;
        if (mContext == null) {
            return result;
        }

        SharedPreferences sp = mContext.getSharedPreferences(ANDFIX_APATCH_VERSION, Context.MODE_PRIVATE);

        result = sp.getString(ANDFIX_APATCH_VERSION_KEY, DEFAULT_VERSION);

        return result;
    }

    /**
     * 清空补丁包版本号
     * @param mContext
     */
    public static void clearPatchVersion(Context mContext) {
        if (mContext == null) {
            return;
        }
        mContext.getSharedPreferences(ANDFIX_APATCH_VERSION, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
