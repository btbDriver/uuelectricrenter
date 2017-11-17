package com.youyou.uuelectric.renter.Network.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Created by liuchao on 2015/9/1.
 * SP工具类用于统一保存用户数据
 */
public class UserSPUtils {

    /**
     * 保存数据的方法，将对象类型转化为Gson字符串然后保存
     *
     * @param context
     * @param key
     * @param object
     */
    public static void setParam(Context context, String spName, String key, Object object) {
        if (context == null || TextUtils.isEmpty(spName) || TextUtils.isEmpty(key) || object == null) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, GsonUtils.getInstance().toJson(object));
        editor.apply();
    }


    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param context
     * @param key
     * @return
     */
    public static String getParam(Context context, String spName, String key) {
        if (context == null || TextUtils.isEmpty(spName) || TextUtils.isEmpty(key)) {
            return SPConstant.DEFAULT_VALUE;
        }
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        return sp.getString(key, SPConstant.DEFAULT_VALUE);

    }

    /**
     * 清空SP的内容
     *
     * @param context
     * @param spName
     */
    public static void clearSP(Context context, String spName) {
        if (context == null || TextUtils.isEmpty(spName)) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }
}
