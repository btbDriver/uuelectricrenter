package com.youyou.uuelectric.renter.Network.user;

import com.google.gson.Gson;

/**
 * Created by liuchao on 2015/9/2.
 */
public class GsonUtils {
    private GsonUtils() {
    }

    private static Gson gson = null;

    //静态工厂方法
    public static Gson getInstance() {
        if (gson == null) {
            synchronized (GsonUtils.class) {
                if (gson == null) {
                    gson = new Gson();
                }
            }
        }
        return gson;
    }
}
