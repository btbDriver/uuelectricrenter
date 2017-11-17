package com.youyou.uuelectric.renter.UI.web;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.youyou.uuelectric.renter.Network.HexUtil;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.UI.web.url.URLConfig;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.Support.SysConfig;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

/**
 * Created by liuchao on 2015/9/11.
 * cookie操作工具类
 */
public class H5Cookie {

    /**
     * 客户端将cookie种入H5页面中
     */
    public static void synCookies(Context context, String url, String cookie) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        Uri uri = null;
        String domain = "";
        try {
            uri = Uri.parse(URLDecoder.decode(url, "utf-8"));
            domain = uri.getHost();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String cookieStr = cookieManager.getCookie(url);
        // 判断token是否发生变化，发生变化的话则更新cookie
        L.i("cookieEquce:" + cookie.equals(H5Constant.TOKEN + "="));
        if (!TextUtils.isEmpty(cookieStr) && cookieStr.contains(cookie) && !cookie.equals(H5Constant.TOKEN + "=")) {
            L.i("无需执行更新cookie的操作...");
            // return;
        }
        // 更新domain(不再从UserInfo中获取，更改为从UrlInfo中获取)
        if (!TextUtils.isEmpty(URLConfig.getUrlInfo().getB4Domain())) {
            domain = URLConfig.getUrlInfo().getB4Domain();
        }
        List<String> uaList = SysConfig.getSystemUa(context);
        // 若不添加domain的话,则部分手机可能有问题...
        String md = ";domain=" + domain;

        cookieManager.setCookie(url, cookie + ";" + md);
        if (uaList != null && uaList.size() > 0) {
            for (String coo : uaList) {
                cookieManager.setCookie(url, coo + md);
            }
        }

        CookieSyncManager.getInstance().sync();
    }


    /**
     * 移除cookie
     */
    public static void removeCookies(Context context) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        CookieSyncManager.getInstance().sync();
    }

    /**
     * 获取token字符串
     *
     * @return
     */
    public static String getToken() {
        String tokenValue = HexUtil.toHexString1(UserConfig.getUserInfo().getB4());
        return H5Constant.TOKEN + "=" + tokenValue;
    }
}
