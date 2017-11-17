package com.youyou.uuelectric.renter.Utils.Support;

/**
 * Created by 刘瀚阳 on 08/28 028.
 * Intent传值的Key都放在这里
 */
public class IntentConfig {
    /**
     * 确认租车界面数据实体key
     */
    public static final String KEY_RENT_CONFIRM_DETAIL = "RENT_CONFIRM_DETAIL";
    /**
     * 附近网点数据集合key
     */
    public static final String KEY_NEAR_DOT_LIST = "near_dot_list";

    /**
     * 订单ID
     */
    public static final String ORDER_ID = "order_id";

    /**
     * 剩余公里数
     *
     */
    public static final String CAR_MILEAGE = "CarMileage";

    /**
     * 车辆编号
     */
    public static final String CAR_IMGURL="car_Imgurl";
    /**
     * 车辆名称
     */
    public static final String CAR_NAME="car_name";
    /**
     * 车辆编号
     */
    public static final String CAR_NUMBER="car_number";
    /**
     * 附件网点是否需要拉接口,  boolean
     */
    public static final String NEAR_STATION_NEED_GET_DATA = "near_station_need_get_data";

    /**
     * 支付完成或者取消页面类型Key
     */
    public static final String KEY_PAGE_TYPE = "page_type";

    /**
     * 优惠券列表:表示打开优惠券列表的位置：0：从侧导中打开优惠券，1：从订单详情中打开优惠券
     */
    public static final String FAVOUR_FROM = "favourFrom";
    /**
     * 优惠券列表，点击Item返回参数优惠券ID
     */
    public static final String COUPON_ID = "coupon_id";
    /**
     * 判断红包是否显示参数
     */
    public static final String IF_REDBAG_SHOW = "surePay";
    /**
     * 导航的终点坐标Key
     */
    public static final String KEY_END_LAT_LNG = "endLatLng";

    /**
     * 导航类型key：步行，驾车
     */
    public static final String KEY_NAV_TYPE = "navType";
    /**
     * 是否从行程列表进入订单完成界面
     */
    public static final String KEY_FROM_TRIP = "fromTrip";

    /**
     * 错误页面进入登陆页面时参数key
     */
    public static final String KEY_FROM = "from";
    /**
     * 是否从错误页面进入登陆页面
     */
    public static final String KEY_FROM_ERROR = "fromError";
    /**
     * 网点经纬度
     */
    public static final String KEY_DOTLAT = "DotLat";

    /**
     * 网点经纬度
     */
    public static final String KEY_DOTLON = "DotLon";

    /**
     * 网点名称
     */
    public static final String KEY_DOTNAME = "DotName";
    /**
     * 是否全网开放异地还车
     */
    public static final String KEY_ISALLDOTSA2B  = "isAllDotsA2B ";
    /**
     * 网点地址
     */
    public static final String KEY_DOTADDR = "DotAddr";

    /**
     * 车辆基本信息key
     */
    public static final String KEY_CARBASEINFO = "CarBaseInfo";
    /**
     * 网点ID
     */
    public static final String KEY_DOTINFO = "dotInfo";

    /**
     * 网点内的可用车辆数的key
     */
    public static final String KEY_DOT_CAR_NUMBER = "dot_car_number";

    /**
     * 车况反馈title和url
     */
    public static final String CAR_FEEDBACK_TITLE = "car_feeback_title";
    public static final String CAR_FEEDBACK_URL = "car_feedback_url";
    /**
     * 用车指南title和url
     */
    public static final String CAR_GUIDE_TITLE = "car_guide_title";
    public static final String CAR_GUIDE_URL = "car_guide_url";
    /**
     * 保险故障title和url
     */
    public static final String CAR_SAFE_TITLE = "car_safe_title";
    public static final String CAR_SAFE_URL = "car_safe_url";


    /**
     * 常用网点 和 附近网点
     */
    public static final String DOT_AWLAYS_NEAR_RECORD_TYPE = "dot_awlays_near_record_type";
    public static final String DOT_NEAR = "dot_near";
    public static final String DOT_AWLAYS = "dot_awlays";
    public static final String DOT_A2A = "dot_a2a";
    public static final String DOT_ID = "dot_id";

    /**
     * 跳转到网点搜索类型：1费用预估传入，2行程中更改传入
     */
    public static final String GET_DOT_TYPE = "get_dot_type";

}
