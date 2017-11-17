package com.youyou.uuelectric.renter.Utils.mina;

import android.app.NotificationManager;
import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.uu.access.app.header.HeaderCommon;
import com.uu.access.longconnection.pb.LongConnection;
import com.youyou.uuelectric.renter.Network.AESUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.Service.LoopRequest;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.observer.ObserverListener;
import com.youyou.uuelectric.renter.Utils.observer.ObserverManager;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * User: qing
 * Date: 2015-07-01 11:50
 * Desc: 长连接业务逻辑处理类
 */
public class LongConnectHandler extends IoHandlerAdapter {

    private static final String TAG = LongConnectHandler.class.getSimpleName();

    private MinaLongConnectManager mMinaLongConnectManager;
    private Context context;

    public static NotificationManager mNotificationManager = null;
    /**
     * 注册Push处理Observer的name前缀
     */
    private static final String PREFIX = "push_option_listener";
    /**
     * 存放所有处理服务器端下发的Push的Listener的集合
     */
    private Map<String, ObserverListener> pushListenerMap = new HashMap<>();

    public LongConnectHandler(MinaLongConnectManager mMinaLongConnectManager, Context context) {
        this.mMinaLongConnectManager = mMinaLongConnectManager;
        this.context = context;
    }

    /**
     * 会话开始时写入长连接握手对象发送到服务器端进行首次长连接握手
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void sessionCreated(IoSession session) throws Exception {
        super.sessionCreated(session);
        // 创建Session时就向其中写入心跳请求数据
        session.write(LongConnectPackageFactory.creatLongBytePacage(context));
        // 注册EventBus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        // 注册服务端下发的Push处理Listener
        initObserver();
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);
        EventBus.getDefault().unregister(this);
    }


    /**
     * 接收到服务器端响应的消息时回调该方法，由于配置了编解码过滤器，
     * 所以在回调该方法时，message应该已经转换成客户端可识别的响应对象ResponsePackage
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        if (message instanceof HeaderCommon.ResponsePackage) {
            try {
                HeaderCommon.ResponsePackage responsePackage = (HeaderCommon.ResponsePackage) message;
                UUResponseData uuResponseData = new UUResponseData();
                if (responsePackage.getRet() == 0) {
                    boolean isPush = false;
                    //成功
                    if (responsePackage.getSeq() == -1) {
                        //长连接PUSH
                        HeaderCommon.ResponseData responseData = HeaderCommon.ResponseData.parseFrom(AESUtils.decrypt(UserConfig.getUserInfo().getB3Key(), responsePackage.getResData().toByteArray()));
                        int cmd = responseData.getCmd();
                        uuResponseData.setCmd(cmd);
                        uuResponseData.setSeq(-1);
                        uuResponseData.setRet(0);
                        uuResponseData.setBusiData(responseData.getBusiData().toByteArray());
                        isPush = true;
                    } else {
                        //心跳,握手协议返回
                        HeaderCommon.ResponseData responseData = HeaderCommon.ResponseData.parseFrom(AESUtils.decrypt(UserConfig.getUserInfo().getB3Key(), responsePackage.getResData().toByteArray()));
                        int cmd = responseData.getCmd();
                        uuResponseData.setCmd(cmd);
                        uuResponseData.setSeq(responsePackage.getSeq());
                        uuResponseData.setRet(0);
                        uuResponseData.setBusiData(responseData.getBusiData().toByteArray());
                        isPush = false;
                    }

                    // 处理握手和Push业务逻辑
                    if (uuResponseData.getRet() == 0 && UserConfig.getUserInfo().getSessionKey() != null) {
                        if (isPush) {
                            // 使用EventBus发布事件
                            EventBus.getDefault().post(new LongConnectMessageEvent(LongConnectMessageEvent.TYPE_PUSH, uuResponseData));
                        } else {
                            //握手协议返回
                            if (uuResponseData.getRet() == 0 && UserConfig.getUserInfo().getSessionKey() != null) {
                                L.i("握手协议成功返回...");
                            } else {
                                L.i("握手协议失败...关闭长连接....");
                                // 清除用户信息
                                // UserConfig.clearUserInfo(context);
                                mMinaLongConnectManager.closeConnect();
                            }
                        }
                    } else {
                        L.i("无法处理握手协议...关闭长连接....");
                        // 清除用户信息
                        // UserConfig.clearUserInfo(context);
                        mMinaLongConnectManager.closeConnect();
                    }

                } else if (responsePackage.getRet() == -11) {
                    mMinaLongConnectManager.closeConnect();

                } else if (responsePackage.getRet() == -13) {
                    mMinaLongConnectManager.closeConnect();

                } else if (responsePackage.getRet() == -12) {
                    mMinaLongConnectManager.closeConnect();
                } else if (responsePackage.getRet() == -14) {
                    mMinaLongConnectManager.closeConnect();
                } else {
                    mMinaLongConnectManager.closeConnect();
                }
            } catch (Exception e) {
                e.printStackTrace();

                //关闭长连接
                mMinaLongConnectManager.closeConnect();
            }

        }

    }

    /**
     * 接收EventBus发布的事件，在主线程中处理服务器端响应逻辑
     *
     * @param event
     */
    public void onEventMainThread(LongConnectMessageEvent event) {
        // Push类型的逻辑处理
        if (event.getType() == LongConnectMessageEvent.TYPE_PUSH) {
            UUResponseData data = event.getData();
            try {
                LongConnection.NotifyMsg.Response notifyMsg = LongConnection.NotifyMsg.Response.parseFrom(data.getBusiData());
                // 有最新消息需要拉取
                if (notifyMsg.getRet() == 0) {
                    L.i("收到长连接消息，开始拉取消息...");
                    // 先关闭轮询服务,防止重复发送轮询请求
//                    LoopService.quitLoopService(context);


                    // 发送轮询请求
                    LoopRequest.getInstance(context).sendLoopRequest();

//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            L.i(LoopService.LOOP_INTERVAL_SECS +"s后重新启动轮询服务...");
//                            LoopService.startLoopService(context);
//                        }
//                    }, LoopService.LOOP_INTERVAL_SECS * 1000);

                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }

        }

    }


    /**
     * 初始化所有处理服务器端下发的Push处理逻辑的Listener
     */
    private void initObserver() {

        // 注册所以的Listener
        for (String key : pushListenerMap.keySet()) {
            ObserverManager.addObserver(key, pushListenerMap.get(key));
        }
    }


}
