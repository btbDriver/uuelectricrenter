package com.youyou.uuelectric.renter.Utils.eventbus;

/**
 * Created by liuchao on 2015/9/14.
 * EventBus 常量
 */
public class EventBusConstant {

    /**
     * eventbus 主界面显示登陆或者是退出
     */
    public static final String EVENTBUS_MAINACTIVITY_SHOWLOGIN = "eventbus_mainactivity_showlogin";

    /**
     * eventbus 我的-用户状态
     */
    public static final String EVENTBUS_USER_STATUS = "eventbus_user_status";

    /**
     * EventBus事件:主地图页面  在附近网点列表中，选择一个网点后需在地图上更新Marker标记
     */
    public static final String EVENT_TYPE_UPDATE_DOT = "map_update_dot";
    /**
     * EventBus事件：主地图页面  网络状态发生变化
     */
    public static final String EVENT_TYPE_NETWORK_STATUS = "map_network_status";

    /**
     * EventBus时间：VolleyNetworkHelper中网络异常，发送弹窗事件
     */
    public static final String EVENT_TYPE_NETWORK_TOLOGIN = "network_tologin";
    /**
     * EventBus事件，解析长连接或者是轮询拉取到的数据
     */
    public static final String EVENT_TYPE_LONGCONNECTION_LOOP = "longconnection_loop";
    /**
     * EventBus事件，拉取到首页广告数据
     */
    public static final String EVENT_TYPE_QUERY_ADV = "query_adv";
    /**
     * 支付的PUSH事件
     */
    public static final String EVENT_TYPE_PAY_PUSH = "pay_push";
    /**
     * 发送选择的网点事件
     */
    public static final String EVENT_TYPE_SELECTED_DOTINFO = "selected_dotInfo";

    /**
     * 处理网点列表选择后事件(常用网点， 附近网点，地图选择网点，地图列表网点)
     */
    public static final String EVENT_TYPE_SELECTED_DOTINFO2 = "selected_dotInfo2";

    /**
     * 刷新地图页网点数据
     */
    public static final String EVENT_TYPE_REFRESH_MAP_DOT = "refresh_map_dot";

    /**
     * 刷新底部车辆详情的数据
     */
    public static final String EVENT_TYPE_REFRESH_CARDETAIL = "refresh_cardetail";
    /**
     * 网点列表发送刷新地图页网点事件
     */
    public static final String EVENT_TYPE_REFRESH_DOT_FORM_DOT_LIST = "refresh_dot_form_dot_list";
    /**
     * 异步操作结果（开关车门，寻车等操作）
     */
    public static final String EVENT_TYPE_ASYNC_RESULT = "async_operator_result";

    /**
     * 执行支付返回事件
     */
    public static final String EVENT_TYPE_ACTIVITY_PAY_BACK = "activity_pay_back";

    /**
     * 异步操作的结果（成功）
     */
    public static final int EVENT_TYPE_RESULT_SUCCESS = 1;
    /**
     * 异步操作的结果（失败）
     */
    public static final int EVENT_TYPE_RESULT_FAILIE = -1;
    /**
     * 更新侧导栏（余额）文案
     */
    public static final String EVENT_TYPE_REFRESH_NAVIGATION = "refresh_navigation";
    /**
     * 更换地图页的图标
     */
    public static final String EVENT_TYPE_UPDATE_MAPICON = "update_map_icon";

    /**
     * 在用户登录成功后，如果地图页车详情卡片已弹出，则刷新底部车详情数据
     */
    public static final String EVENT_TYPE_UPDATE_CAR_DETAIL_BY_LOGIN = "update_car_detail_by_login";
    /**
     * 关闭规则弹窗事件
     */
    public static final String EVENT_TYPE_CLOSE_RULE_DIALOG = "close_rule_dialog";
    /**
     * 取消订单事件
     */
    public static final String EVENT_TYPE_CANCEL_ORDER = "cancel_order";

    /**
     * 关闭取消订单弹窗事件(用在账户被踢的情况下)
     */
    public static final String EVENT_TYPE_CLOSE_CANCEL_DIALOG = "close_cancel_dialog";

    /**
     * 取车页面更新timeCount
     */
    public static final String EVENT_TYPE_UPDATE_TIMECOUNT = "update_timecount";
    /**
     * 当前行程页面cancel timecount
     */
    public static final String EVENT_TYPE_CANCEL_TIMECOUNT = "cancel_timecount";
    /**
     * 取车页面等待类型（免费等待、超时等待）
     */
    public static final String EVENT_TYPE_UPDATE_WAIT_TYPE = "update_waittype";
    /**
     * App退出后台发送取消弹窗事件
     */
    public static final String EVENT_TYPE_CANCEL_DIALOG = "cancel_dialog";
}
