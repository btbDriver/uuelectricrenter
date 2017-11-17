package com.youyou.uuelectric.renter.UI.main;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.VolleyError;
import com.google.protobuf.InvalidProtocolBufferException;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.message.pb.common.LongMsgCommon;
import com.uu.facade.message.pb.iface.LongMsgInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Service.LoopRequest;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by liuchao on 2015/12/24.
 */
public class MsgHandler {


    /**
     * 发送轮询请求
     */
    public static void sendLoopRequest(final Activity mContext) {
        if (mContext == null)
            return;

        boolean preStatus = false;

        final SharedPreferences sp = mContext.getSharedPreferences(LoopRequest.SP_NAME, Context.MODE_PRIVATE);
        long version = sp.getLong(LoopRequest.KEY_VERSION, 0);
        L.i("开始请求轮询接口，当前version:" + version);
        LongMsgInterface.GetInstantMsg.Request.Builder builder = LongMsgInterface.GetInstantMsg.Request.newBuilder();
        builder.setVersion(version);

        // 当前APP所在状态
        boolean currentStatus = Config.outApp(mContext);
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
                            sp.edit().putLong(LoopRequest.KEY_VERSION, newVersion).commit();

                            LoopRequest.getInstance(mContext).feedbackHasGetMsg(newVersion);

                            List<LongMsgCommon.MsgStructPackage> msgStructPackages = response.getMsgStructPackageListList();
                            L.d("轮询接口拉取成功，获取到的消息数:" + msgStructPackages.size());
                            if (msgStructPackages == null || msgStructPackages.size() == 0) {
                                sendErrorEvent();
                                return;
                            }
                            BaseEvent baseEvent = new BaseEvent(EventBusConstant.EVENT_TYPE_LONGCONNECTION_LOOP, msgStructPackages);
                            EventBus.getDefault().post(baseEvent);

                        } else {
                            L.d("response.getRet():轮询接口拉取失败...");
                            sendErrorEvent();
                        }

                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        sendErrorEvent();
                    }

                } else {
                    L.d("responseData.getRet():轮询接口拉取失败...");
                    sendErrorEvent();
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                L.d("onError:轮询接口拉取失败...");
                sendErrorEvent();
            }

            @Override
            public void networkFinish() {
            }
        });
    }


    /**
     * 发送开关车门等异步超时通知
     */
    public static void sendErrorEvent() {
        BaseEvent baseEvent = new BaseEvent(EventBusConstant.EVENT_TYPE_ASYNC_RESULT, EventBusConstant.EVENT_TYPE_RESULT_FAILIE);
        EventBus.getDefault().post(baseEvent);
    }

}
