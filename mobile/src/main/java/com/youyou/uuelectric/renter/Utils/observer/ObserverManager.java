package com.youyou.uuelectric.renter.Utils.observer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ObserverManager
{

    public static final String MAINTABNUM         = "MAINTABNUM";//通知主页,去读底导数字
    public static final String MAINLOGIN          = "MAINLOGIN";//登录
    public static final String MAINLOGOUT         = "MAINLOGOUT";//主页登录改变状态
    public static final String MYLOGOUT           = "MYLOGOUT";//注销登录
    public static final String TICKET_LOGOUT      = "TICKET_LOGOUT";//票据失效，清除用户身份
    public static final String COUPONNUM          = "COUPONNUM";//优惠券数量变更
    public static final String SPEEDRENT          = "SPEEDRENT";//快速约车列表变更
    public static final String RENTERTOPAY        = "RENTERTOPAY";//租客去支付
    public static final String RENTERTOPAY_STROKE = "RENTERTOPAY_STROKE";//租客去支付
    public static final String RENTERORDERFINISH  = "RENTERORDERFINISH";//租客订单完成
    public static final String RENTERORDERFAILURE = "RENTERORDERFAILURE";//租客发起一对一、一对多约车请求后，如果车主拒绝或者超时未响应，则给租客下发该push
    public static final String CAR_STATUS_CHANGE  = "CAR_STATUS_CHANGE";//车辆状态变化
    public static final String USER_STATUS_CHANGE = "USER_STATUS_CHANGE";//用户身份变化
    public static final String SCHEDULE_PUSH      = "SCHEDULE_PUSH";//行程列表PUSH
    public static final String ORDER_OWNER_PUSH   = "ORDER_OWNER_PUSH";// 车主订单PUSH
    public static final String WISH_LIST_PUSH     = "WISH_LIST_PUSH";// 意向单PUSH
    public static final String ORDER_RENTER_PUSH  = "ORDER_RENTER_PUSH";// 租客订单PUSH
    public static final String MYSTROKEFRAGMENT   = "MYSTROKE_FRAGMENT";//行程页刷新
    public static final String CARMANAGERFRAGMENT = "CARMANAGER_FRAGMENT";//车主页刷新
    public static final String OWNERORDERDETAIL   = "OWNER_ORDER_DETAIL";//车主订单详情
    public static final String QUICKRENTCARPUSH   = "QUICKRENTCARPUSH";//快速约车,租客收到push\


//    public static final AtomicBoolean dialogBool = new AtomicBoolean(false);

    /**
     * String 为生产者 为当前生产者的所有观察者
     */
    public static ConcurrentHashMap<String, ObserverListener> observer = new ConcurrentHashMap<String, ObserverListener>();

    public static void addObserver(String name, ObserverListener o)
    {
//        if(observer.containsKey(name))
//        {
//            return;
//        }
//        else
        {
            observer.put(name, o);
        }
    }

    public static ObserverListener getObserver(String name)
    {
//        System.out.println("=name="+name+"   has? + "+observer.containsKey(name));
        if (!observer.containsKey(name))
        {
            observer.put(name, temp);
        }
        return observer.get(name);
    }

    public static void removeObserver(String name)
    {
        if (observer.containsKey(name))
        {
            observer.remove(name);
        }
    }

    private static ObserverListener temp = new ObserverListener()
    {
        @Override
        public void observer(String from, Object msg)
        {

        }
    };

    /**
     * 主题 遍历 推送消息给 所有观察者
     */
    public static void observerAll(String from, Object msg)
    {
        for (Map.Entry<String, ObserverListener> listen : observer.entrySet())
        {
            listen.getValue().observer(from, msg);
        }
    }
}
