package com.youyou.uuelectric.renter.UI.web;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.WindowManager;
import android.webkit.WebSettings;

/**
 * Created by liuchao on 2015/11/30.
 */
public class H5FragmentUtils {
    /**
     * 设置H5页面title
     *
     * @param h5Fragment
     * @param title
     */
    public static void setTitle(H5Fragment h5Fragment, String title) {
        if (h5Fragment == null || h5Fragment.getActivity() == null) {
            return;
        }
        Intent intent = h5Fragment.getActivity().getIntent();
        if (intent != null && intent.getStringExtra(H5Constant.TITLE) != null) {
            title = intent.getStringExtra(H5Constant.TITLE);
        }
        if (h5Fragment.getActivity() instanceof H5Activity) {
            H5Activity h5Activity = (H5Activity)(h5Fragment.getActivity());
            h5Activity.setTitle(title);
        }
    }

    /**
     * 获取请求URL
     *
     * @param h5Fragment
     * @param currentUrl
     */
    public static String getUrl(H5Fragment h5Fragment, String currentUrl) {
        if (h5Fragment == null || h5Fragment.getActivity() == null) {
            return currentUrl;
        }
        Intent intent = h5Fragment.getActivity().getIntent();
        if (intent != null && intent.getStringExtra(H5Constant.MURL) != null) {
            currentUrl = intent.getStringExtra(H5Constant.MURL);
        }

        return currentUrl;
    }

    /**
     * 初始化组件WebView
     *
     * @param h5Fragment
     */
    public static void initH5View(H5Fragment h5Fragment) {
        if (h5Fragment == null || h5Fragment.getActivity() == null) {
            return;
        }

        if (h5Fragment.getActivity().getIntent().getBooleanExtra(H5Constant.SOFT_INPUT_IS_CHANGE_LAYOUT, false)) {
            h5Fragment.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        // 设置H5页面默认能够长按复制
        /*if (!h5Fragment.getActivity().getIntent().getBooleanExtra(H5Constant.OPENLONGCLICK, false)) {

            h5Fragment.mWebView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });
        }*/
        WebSettings webSettings = h5Fragment.mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowFileAccess(false);
        webSettings.setUseWideViewPort(false);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setDatabaseEnabled(false);
        webSettings.setAppCacheEnabled(false);
        webSettings.setBlockNetworkImage(true);
        // 设置webview默认的UserAgent
        StringBuilder sb = new StringBuilder().append(webSettings.getUserAgentString()).append(" UUYongche/android/").append(getVersionName(h5Fragment));
        // L.i("UserAgent: =" + sb.toString());
        webSettings.setUserAgentString(sb.toString());
    }


    /**
     * 默认版本号
     */
    private static final String DEFAULT_VERISON = "1.0.0";
    /**
     * 获取APP版本号
     * @return
     * @throws Exception
     */
    private static String getVersionName(H5Fragment h5Fragment){
        try {
            if (h5Fragment == null || h5Fragment.getActivity() == null) {
                return "";
            }
            // 获取packagemanager的实例
            PackageManager packageManager = h5Fragment.getActivity().getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(h5Fragment.getActivity().getPackageName(), 0);
            return packInfo.versionName;
        } catch (Exception e) {
            return DEFAULT_VERISON;
        }
    }
}
