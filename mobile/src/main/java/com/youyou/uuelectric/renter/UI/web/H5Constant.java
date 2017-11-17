package com.youyou.uuelectric.renter.UI.web;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.youyou.uuelectric.renter.Utils.Support.SysConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuchao on 2015/9/1.
 * H5页面工具类，自定义的常量
 */
public class H5Constant {
    public static final String SCHEME = "uuyongchemobile://";
    // H5页面选择相机拍照请求码
    public static final int h5CameraCode = 100;
    // H5页面跳转请求码
    public static final int h5RequestCode = 101;
    // H5页面跳转返回码
    public static final int h5ResultCode = 102;
    // 票据信息
    public static final String TOKEN = "token";
    // scheme URL
    public static final String MURL = "url";
    // H5页面title
    public static final String TITLE = "title";
    //是否允许刷新,下拉刷新组件是否可用
    public static final String CARFLUSH = "canFlush";
    //是否屏蔽长按功能
    public static final String OPENLONGCLICK = "long_click";
    //软键盘在H5中出现时，是否改变布局(遮住底部布局或者将底部布局显示在键盘之上)
    public static final String SOFT_INPUT_IS_CHANGE_LAYOUT = "isChangeLayout";
    // SCHEME参数，判断H5页面是否可以长按复制
    public static final String CANSELECT = "canSelect";
    // SCHEME参数，执行动作名称
    public static final String ACTION_NAME = "name";
    // SCHEME参数，返回N层？
    public static final String NUMBEROFCLOSEPRE = "numberOfClosePre";
    // SCHEME参数，是否需要刷新
    public static final String RELOADPRE = "reloadPre";
    // 返回前N页的时候是否需要刷新（仅用于返回动作是传递参数值）
    public static boolean isNeedFlush = false;
    // 返回上一层是否需要刷新
    public static String ISNEEDFLUSH = "isNeedFlush";
    // 判断标识，判断打开的是否是“其他待付费用”H5页面
    public static final String ISPAYMENTNOTICE = "isPaymentNotice";
    // 判断标识，判断打开的是否是“待处理违章”H5页面
    public static final String ISPECCANCYLIST = "isPeccancyList";

    // scheme调用相机时回调的js方法名称
    public static final String CALLBACK = "callback";
    // scheme调用相机之后调用的js回调方法
    public static String cameraCallback = "";
    // scheme调用相机时临时图片名称
    public static final String tempImage = "image_temp_scheme_origin.jpg";
    // scheme调用相机时临时图片路径
    public static String bigPicPath = SysConfig.SD_IMAGE_PATH + tempImage;
    // scheme调用相机压缩之后的图片名称
    public static final String compressImage = "image_temp_scheme_compress.jpg";
    // scheme调用相机压缩之后的图片路径
    public static String picPath = SysConfig.SD_IMAGE_PATH + compressImage;

    // 跳转登录页面传递的schemeURl
    public static final String BACKTOSCHEMEURL = "backToSchemeUrl";


    /**
     * 解析URI中的query条件
     */
    public static Map<String, String> parseUriQuery(String query) {

        Map<String, String> map = new HashMap<String, String>();

        if (!TextUtils.isEmpty(query)) {
            String[] splitValue = query.split("&");
            for (String str : splitValue) {
                String[] key_value = str.split("=");
                if (key_value.length == 2)
                    map.put(key_value[0], key_value[1]);
            }
        }
        return map;
    }

    /**
     * 从scheme的url中构建出Intent，用于界面跳转
     *
     * @param url
     * @return
     */
    public static Intent buildSchemeFromUrl(String url) {
        if (url != null && url.indexOf(H5Constant.SCHEME) != -1) {
            Uri uri = Uri.parse(url);
            String[] urlSplit = url.split("\\?");
            Map<String, String> queryMap = new HashMap<String, String>();
            String h5Url = null;
            if (urlSplit.length == 2) {
                queryMap = H5Constant.parseUriQuery(urlSplit[1]);
                h5Url = queryMap.get(H5Constant.MURL);
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (!TextUtils.isEmpty(h5Url)) {
                intent.putExtra(H5Constant.MURL, h5Url);
            }
            return intent;
        }
        return null;
    }


    /**
     * 打开一个H5页面
     *
     * @param context
     * @param currentUrl
     * @param title
     * @param isFinishCurrent
     */
    public static void startH5Activity(Activity context, String currentUrl, String title, boolean isFinishCurrent) {
        if (context == null) {
            return;
        }
        if (TextUtils.isEmpty(currentUrl)) {
            return;
        }
        Intent intent = new Intent(context, H5Activity.class);
        intent.putExtra(H5Constant.MURL, currentUrl);
        intent.putExtra(H5Constant.TITLE, title);
        context.startActivity(intent);
        if (isFinishCurrent) {
            context.finish();
        }
    }

    /**
     * 打开一个H5页面
     *
     * @param context
     * @param currentUrl
     * @param title
     */
    public static void startH5Activity(Activity context, String currentUrl, String title) {
        startH5Activity(context, currentUrl, title, false);
    }
}
