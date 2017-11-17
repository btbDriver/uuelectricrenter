package com.youyou.uuelectric.renter.Utils.mina;

import com.youyou.uuelectric.renter.Network.UUResponseData;

/**
 * User: qing
 * Date: 2015-07-08 13:13
 * Desc: 定义EventBus的事件类型，主要用来处理长连接中的事件
 */
public class LongConnectMessageEvent {
    /**
     * push事件类型
     */
    public static final int TYPE_PUSH = 0;
    /**
     * 改变IP事件类型
     */
    public static final int TYPE_CHANGEIP = 1;
    /**
     * 重连长连接类型
     */
    public static final int TYPE_RESTART = 2;

    /**
     * 关闭长连接
     */
    public static final int TYPE_CLOSE = 3;


    public int type;
    public UUResponseData data;

    public LongConnectMessageEvent(int type, UUResponseData data) {
        this.type = type;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public UUResponseData getData() {
        return data;
    }
}
