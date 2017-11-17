package com.youyou.uuelectric.renter.Network.user;

/**
 * Created by liuchao on 2015/9/2.
 * 用于保存SP中需要使用的常量信息，如：SP文件名称，Key值
 */
public class SPConstant {
    // 默认的GSON数据值
    public static final String DEFAULT_VALUE = "{}";

    // ***************** SP NAME *******************************
    // 用于保存用户信息，其他的SP文件名称后续添加
    public static final String SPNAME_USER_INFO = "user_info";
    public static final String SPNAME_UUID = "sp_uuid";
    // 保存搜索历史SP
    public static final String SPNAME_SEARCH_HISTORY = "search_history";
    // start query URL信息
    public static final String SPNAME_WEBVIEW_URL = "webview_url";
    // 引导图SP
    public static final String SP_NAME_GUIDE_VERSION = "guide_version";
    // SP:首页地图Marker的Icon对应的url
    public static final String SP_NAME_MARKER_ICON_URLS = "marker_icon_urls";
    // 取车页面是否是新版本第一次打开
    public static final String SPNAME_GETCAR_FIRST = "getcar_first";
    // 当前行程页面是否是首单自动弹出驾车指南弹窗
    public static final String SPNAME_CURRENT_FIRST = "current_first";
    // 当前行程页面是否用户是否点击了四小时还车
    public static final String SPNAME_CURRENT_FOURHOURCLICK = "current_fourhour_click";


    // **************** SP key *************************************
    public static final String SPKEY_USER_INFO = "userInfo";
    public static final String SPKEY_UUID = "spuuid";
    // 搜索历史KEY
    public static final String SPKEY_HISTORY = "history";
    // start query URL key
    public static final String SPKEY_WEBVIEW_URL = "webviewurl";

    public static final String SP_KEY_GUIDE_VERSION = "version";

    public static final String SP_KEY_MARKER_ICON_URLS = "key_marker_icon_urls";
    // 取车页面是否是新版本第一次打开
    public static final String SPKEY_GETCAR_FIRST = "key_getcar_first";
    // 当前行程页面是否是首单自动弹出驾车指南
    public static final String SPKEY_CURRENT_FIRST = "key_current_first";
}
