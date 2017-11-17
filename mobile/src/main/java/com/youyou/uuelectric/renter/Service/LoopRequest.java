package com.youyou.uuelectric.renter.Service;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;

import com.android.volley.VolleyError;
import com.google.protobuf.InvalidProtocolBufferException;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.message.pb.common.LongMsgCommon;
import com.uu.facade.message.pb.iface.LongMsgInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.UI.main.MainActivity;
import com.youyou.uuelectric.renter.UI.main.MainLoopActivity;
import com.youyou.uuelectric.renter.UI.order.TripOrderDetailActivity;
import com.youyou.uuelectric.renter.UI.pay.BasePayFragmentUtils;
import com.youyou.uuelectric.renter.UI.web.H5Constant;
import com.youyou.uuelectric.renter.Utils.DialogUtil;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;
import com.youyou.uuelectric.renter.Utils.notification.NotificationUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 发送轮询请求的工具类
 */
public class LoopRequest {

    private Context context;
    private SharedPreferences sp;
    public static final String SP_NAME = "loop_server";
    public static final String KEY_VERSION = "version";
    private static LoopRequest instance;
    /**
     * 上一次APP是否处于前台
     */
    private boolean preStatus = false;

    private LoopRequest(Context context) {
        this.context = context;
        sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);

    }

    public synchronized static LoopRequest getInstance(Context context) {
        if (instance == null) {
            instance = new LoopRequest(context);
        }
        return instance;
    }

    /**
     * 发送轮询请求
     */
    public synchronized void sendLoopRequest() {
        if (!Config.isNetworkConnected(context)) {
            return;
        }
        long version = sp.getLong(KEY_VERSION, 0);
        L.i("开始请求轮询接口，当前version:" + version);
        LongMsgInterface.GetInstantMsg.Request.Builder builder = LongMsgInterface.GetInstantMsg.Request.newBuilder();
        builder.setVersion(version);

        // 当前APP所在状态
        boolean currentStatus = Config.outApp(context);
        // APP在后台
        if (currentStatus) {
            builder.setMsgScene(LongMsgCommon.MessageScene.UNACTIVATED_STATE);
            L.i("当前场景：后台");
        } else {
            builder.setMsgScene(LongMsgCommon.MessageScene.ACTIVATED_STATE);
            L.i("当前场景：前台");
        }
        // 上一次时后台，当前又处于前台，此时的状态是：由后台切换到APP应用内
        if (preStatus && !currentStatus) {
            builder.setMsgScene(LongMsgCommon.MessageScene.ACTIVATED_TO_UNACTIVATED_STATE);
            L.i("当前场景：后台转前台");
        }
        preStatus = currentStatus;
//        builder.setMsgScene(LongMsgCommon.MessageScene.ACTIVATED_STATE);
        NetworkTask task = new NetworkTask(Cmd.CmdCode.GetInstantMsg_NL_VALUE);
        task.setBusiData(builder.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {

                    try {
                        LongMsgInterface.GetInstantMsg.Response response = LongMsgInterface.GetInstantMsg.Response.parseFrom(responseData.getBusiData());

                        if (response.getRet() == 0) {

                            long newVersion = response.getVersion();
                            // 保存服务端下发的最新Version
                            sp.edit().putLong(KEY_VERSION, newVersion).commit();

                            feedbackHasGetMsg(newVersion);

                            List<LongMsgCommon.MsgStructPackage> msgStructPackages = response.getMsgStructPackageListList();
                            L.i("轮询接口拉取成功，获取到的消息数:" + msgStructPackages.size());

                            // 应用外PUSH直接发送notification，并过滤
                            boolean isPush = false;
                            if (msgStructPackages != null && msgStructPackages.size() > 0) {

                                List<LongMsgCommon.MsgStructPackage> mmsgStructPackages = new ArrayList<>();
                                for (LongMsgCommon.MsgStructPackage msgStructPackage : msgStructPackages) {
                                    if (msgStructPackage.getMessageType() == LongMsgCommon.MessageType.OUT_OF_APP_MSG) {
                                        isPush = true;
                                        // 添加应用外判断，应用内的时候不发送notification
                                        if (Config.outApp(context)) {
                                            if (!BasePayFragmentUtils.payActionIsOver) {
                                                continue;
                                            }
                                            LongMsgCommon.OutOfAppMsg outOfAppMsg = LongMsgCommon.OutOfAppMsg.parseFrom(msgStructPackage.getReqData());
                                            // 设置默认不相同的的notificationId
                                            NotificationUtil.showNotification(context, outOfAppMsg/*NotifeConstant.NOTIFICATION_ID*//*NotifeConstant.getRadom()*/);
                                        }
                                    } else {
                                        mmsgStructPackages.add(msgStructPackage);
                                    }
                                }

                                msgStructPackages = mmsgStructPackages;
                            }

                            // 若存在notification并且消息队列为空，则不再发送
                            if (isPush && (msgStructPackages == null || msgStructPackages.size() == 0)) {
                            } else {
                                BaseEvent baseEvent = new BaseEvent(EventBusConstant.EVENT_TYPE_LONGCONNECTION_LOOP, msgStructPackages);
                                EventBus.getDefault().post(baseEvent);
                            }

                        } else {
                            L.i("response.getRet():轮询接口拉取失败...");
                            BaseEvent baseEvent = new BaseEvent(EventBusConstant.EVENT_TYPE_LONGCONNECTION_LOOP, null);
                            EventBus.getDefault().post(baseEvent);
                        }

                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        BaseEvent baseEvent = new BaseEvent(EventBusConstant.EVENT_TYPE_LONGCONNECTION_LOOP, null);
                        EventBus.getDefault().post(baseEvent);
                    }


                } else {
                    L.i("responseData.getRet():轮询接口拉取失败...");
                    BaseEvent baseEvent = new BaseEvent(EventBusConstant.EVENT_TYPE_LONGCONNECTION_LOOP, null);
                    EventBus.getDefault().post(baseEvent);
                }

            }

            @Override
            public void onError(VolleyError errorResponse) {

                L.i("onError:轮询接口拉取失败...");
                BaseEvent baseEvent = new BaseEvent(EventBusConstant.EVENT_TYPE_LONGCONNECTION_LOOP, null);
                EventBus.getDefault().post(baseEvent);

            }

            @Override
            public void networkFinish() {

            }
        });
    }

    /**
     * 消息接收成功后，反馈给消息服务器已经获取消息
     */
    public synchronized void feedbackHasGetMsg(long version) {
        if (Config.isNetworkConnected(context)) {
            LongMsgInterface.FeedbackHasGetMsg.Request.Builder builder = LongMsgInterface.FeedbackHasGetMsg.Request.newBuilder();
            builder.setVersion(version);
            NetworkTask task = new NetworkTask(Cmd.CmdCode.FeedbackHasGetMsg_NL_VALUE);
            task.setBusiData(builder.build().toByteArray());
            NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
                @Override
                public void onSuccessResponse(UUResponseData responseData) {

                }

                @Override
                public void onError(VolleyError errorResponse) {

                }

                @Override
                public void networkFinish() {

                }
            });
        }

    }


    /**
     * 解析长连接和轮询拉取的数据
     *
     * @pam msgList
     */
    public static void parserLongConnResult(Object msgObj, final Activity mContext) {
        if (msgObj == null || mContext == null) {
            L.i("消息体为null");
            return;
        }

        List<LongMsgCommon.MsgStructPackage> msgList = (List<LongMsgCommon.MsgStructPackage>) msgObj;
        // 若没有拉取到数据
        if (msgList.size() == 0) {
            L.i("msgList:" + msgList.size());
            return;
        } else {
            L.i("msgList:" + msgList.size());
        }

        try {
            // 拉取到消息发送EventBus事件（开关车门等一部操作需要此逻辑）
            sendResultMsg();
            /**
             *  暂时只解析前两个字段，其他字段后续解析
             *  required MessageType messageType = 1; // 消息展示类型
             required bytes reqData = 2; // 该字段是消息结构体序列化后的数据
             required int32 validSecs = 3[default = -1]; // 消息在客户端保存的有效秒数，超过这个时间客户端不展示，如果是-1，表示不需要限制
             required int32 delayShowSecs = 4[default = 0]; // 客户端拿到消息后，延迟多少秒再展示，0，表示不延迟
             required int32 level = 5[default = 0]; // 此消息结构在端显示优先级别，级别越高，最先展示
             */
            for (int i = 0; i < msgList.size(); i++) {
                LongMsgCommon.MsgStructPackage msgStructPackage = msgList.get(i);
                /**
                 * 消息展示类型
                 */
                // toast类型消息
                if (msgStructPackage.getMessageType() == LongMsgCommon.MessageType.MSG_TYPE_TOAST) {
                    LongMsgCommon.ToastMsgStruct toastMsgStruct = LongMsgCommon.ToastMsgStruct.parseFrom(msgStructPackage.getReqData());
                    Config.showToast(mContext, toastMsgStruct.getContent());
                    // 判断toast中是否含有URL，若存在则跳转
                    if (!TextUtils.isEmpty(toastMsgStruct.getActionUrl())) {
                        Intent schemeIntent = H5Constant.buildSchemeFromUrl(toastMsgStruct.getActionUrl());
                        mContext.startActivity(schemeIntent);
                    }
                }
                // 带按钮的alert提示弹窗
                else if (msgStructPackage.getMessageType() == LongMsgCommon.MessageType.ALTER_WITH_BUTTON_MSG) {
                    LongMsgCommon.AlterWithButtonMsg alterWithButtonMsg = LongMsgCommon.AlterWithButtonMsg.parseFrom(msgStructPackage.getReqData());
                    Dialog dialog = DialogUtil.getInstance(mContext).showMaterialTipDialogNoTitle(alterWithButtonMsg.getContent(), alterWithButtonMsg.getSingleButton().getText(), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                }
                // 操作订单后的提示
                else if (msgStructPackage.getMessageType() == LongMsgCommon.MessageType.OP_ORDER_ALTER_WITH_BUTTON_MSG) {
                    final LongMsgCommon.AlterWithButtonMsg alterWithButtonMsg = LongMsgCommon.AlterWithButtonMsg.parseFrom(msgStructPackage.getReqData());
                    Dialog dialog = DialogUtil.getInstance(mContext).showMaterialTipDialogNoTitle(alterWithButtonMsg.getContent(), alterWithButtonMsg.getSingleButton().getText(), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // 若按钮的SCHEME不为空，则跳转
                            if (!TextUtils.isEmpty(alterWithButtonMsg.getSingleButton().getActionUrl())) {
                                Intent schemeIntent = H5Constant.buildSchemeFromUrl(alterWithButtonMsg.getSingleButton().getActionUrl());
                                mContext.startActivity(schemeIntent);
                            }
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                }
                // 接收订单支付完成时服务器端下发的PUSH
                else if (msgStructPackage.getMessageType() == LongMsgCommon.MessageType.OP_ORDER_PAID_MSG) {
                    LongMsgCommon.OperateOrder operateOrder = LongMsgCommon.OperateOrder.parseFrom(msgStructPackage.getReqData());
                    L.i("收到【订单】支付完成的PUSH...");
                    if (operateOrder != null && !TextUtils.isEmpty(operateOrder.getOrderId())) {
                        if (BasePayFragmentUtils.payActionIsOver) {
                            L.i("【订单】支付操作已完成...");
                            return;
                        }
                        // 第三方支付已经返回，此时收到了PUSH，直接跳转
                        if (BasePayFragmentUtils.isPayBack) {
                            L.i("【订单】第三方支付已经返回");
                            Config.dismissProgress();
                            // 支付返回，主界面可以开始Loop
                            MainLoopActivity.isCanLoop = true;
                            // 设置支付动作已完成
                            BasePayFragmentUtils.payActionIsOver = true;

                            // 支付成功，跳转到支付成功界面
                            Intent intent = new Intent(mContext, TripOrderDetailActivity.class);
                            intent.putExtra(IntentConfig.ORDER_ID, operateOrder.getOrderId());
                            intent.putExtra(IntentConfig.KEY_PAGE_TYPE, TripOrderDetailActivity.PAGE_TYPE_TRIP_DETAIL);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            mContext.startActivity(intent);
                        } else {
                            L.i("【订单】收到PUSH,第三方支付未返回");
                            // 如果第三方支付未返回，则记录接收到了PUSH
                            BasePayFragmentUtils.isReceivePush = true;

                            Config.setOrderId(mContext, operateOrder.getOrderId());
                        }
                    }
                }
                // 充值成功后的PUSH
                else if (msgStructPackage.getMessageType() == LongMsgCommon.MessageType.RECHARGE_MSG) {
                    LongMsgCommon.RechargeMsg struct = LongMsgCommon.RechargeMsg.parseFrom(msgStructPackage.getReqData());
                    L.i("【余额充值】接收到PUSH消息：title:" + struct.getTitle() + "\t content:" + struct.getContent());
                    BasePayFragmentUtils.payMsg = struct.getContent();
                    if (struct != null) {
                        if (BasePayFragmentUtils.payActionIsOver) {
                            L.i("【余额充值】支付操作已完成...");
                            return;
                        }
                        // 第三方支付已经返回，此时收到了PUSH，直接跳转
                        if (BasePayFragmentUtils.isPayBack) {
                            L.i("【余额充值】第三方支付已经返回");
                            Config.dismissProgress();
                            // 支付返回，主界面可以开始Loop
                            MainLoopActivity.isCanLoop = true;
                            // 设置支付动作已完成
                            BasePayFragmentUtils.payActionIsOver = true;

                            // 支付成功，返回到余额页面
                            EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_ACTIVITY_PAY_BACK));
                        } else {
                            L.i("【余额充值】收到PUSH,第三方支付未返回");
                            // 如果第三方支付未返回，则记录接收到了PUSH
                            BasePayFragmentUtils.isReceivePush = true;
                            BasePayFragmentUtils.receivePushType = 1;
                        }
                    }
                }
                // 其他带支付费用和违章费用，支付成功后的PUSH
                else if (msgStructPackage.getMessageType() == LongMsgCommon.MessageType.OTHER_FEE_TYPE_MSG) {
                    LongMsgCommon.OtherFeeTypeMsg struct = LongMsgCommon.OtherFeeTypeMsg.parseFrom(msgStructPackage.getReqData());
                    L.i("【其他带支付费用和违章费用】收到PUSH通知:" + struct.getContent());
                    BasePayFragmentUtils.payMsg = struct.getContent();
                    if (struct != null) {
                        if (BasePayFragmentUtils.payActionIsOver) {
                            L.i("【其他带支付费用和违章费用】支付操作已完成...");
                            return;
                        }
                        // 第三方支付已经返回，此时收到了PUSH，直接跳转
                        if (BasePayFragmentUtils.isPayBack) {
                            L.i("【其他带支付费用和违章费用】第三方支付已经返回");
                            Config.dismissProgress();
                            // 支付返回，主界面可以开始Loop
                            MainLoopActivity.isCanLoop = true;
                            // 设置支付动作已完成
                            BasePayFragmentUtils.payActionIsOver = true;

                            // 支付成功，返回主页面
                            Intent intent = new Intent(mContext, MainActivity.class);
                            intent.putExtra("goto", MainActivity.GOTO_MAP);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            mContext.startActivity(intent);
                        } else {
                            L.i("【其他带支付费用和违章费用】收到PUSH,第三方支付未返回");
                            // 如果第三方支付未返回，则记录接收到了PUSH
                            BasePayFragmentUtils.isReceivePush = true;
                            BasePayFragmentUtils.receivePushType = 0;
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 开关车门寻车等操作成功之后发送EventBus事件
     */
    private static void sendResultMsg() {
        // 拉取消息成功之后发送EventBus事件（开关车门等异步操作需要此逻辑）
        BaseEvent baseEvent = new BaseEvent(EventBusConstant.EVENT_TYPE_ASYNC_RESULT, EventBusConstant.EVENT_TYPE_RESULT_SUCCESS);
        EventBus.getDefault().post(baseEvent);
    }

}
