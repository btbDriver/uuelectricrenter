package com.youyou.uuelectric.renter.Utils.mina;


import com.youyou.uuelectric.renter.Utils.Support.L;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;

import de.greenrobot.event.EventBus;

/**
 * User: qing
 * Date: 2015-07-02 16:57
 * Desc: 监测会话是否断开的过滤器
 */
public class LongConnectReconnectionFilter extends IoFilterAdapter {

    private boolean hasSend = false;

    private long lastSessionClose;
    /**
     * session关闭的时间间隔，小于此间隔说明session持续启动和关闭多次，此时应该直接关闭连接
     */
    private static final long INTERVAL = 5000;
    /**
     * 是否发送过异常情况关闭session的消息
     */
    private boolean isExceptionEvent;

    public LongConnectReconnectionFilter() {
        isExceptionEvent = false;
    }


    @Override
    public void exceptionCaught(NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
        super.exceptionCaught(nextFilter, session, cause);
        EventBus.getDefault().post(new LongConnectMessageEvent(LongConnectMessageEvent.TYPE_RESTART, null));
        hasSend = true;
        L.i("监测到出现异常进入exceptionCaught方法，发送TYPE_RESTART事件...");
    }

    @Override
    public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
        super.sessionClosed(nextFilter, session);
        if (hasSend) {
            L.i("已发送过TYPE_RESTART事件...");
            hasSend = false;
            return;
        }
        long current = System.currentTimeMillis();
        if (current - lastSessionClose < INTERVAL) {
            lastSessionClose = current;
            if (!isExceptionEvent) {
                L.i("当前session关闭时间小于5s，属于异常关闭，此种异常不是出错exception，是内部状态异常，不发送TYPE_RESTART事件，发送TYPE_CLOSE事件...");
                isExceptionEvent = true;
                EventBus.getDefault().post(new LongConnectMessageEvent(LongConnectMessageEvent.TYPE_CLOSE, null));
            }
            return;
        }

        lastSessionClose = current;
        EventBus.getDefault().post(new LongConnectMessageEvent(LongConnectMessageEvent.TYPE_RESTART, null));
        L.i("监测到session关闭，发送TYPE_RESTART事件...");
    }
}
