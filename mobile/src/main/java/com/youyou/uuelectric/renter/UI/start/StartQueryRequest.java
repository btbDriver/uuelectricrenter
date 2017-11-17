package com.youyou.uuelectric.renter.UI.start;

import android.app.Activity;

import com.android.volley.VolleyError;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.user.protobuf.bean.UserInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.UI.web.url.URLConfig;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;

import de.greenrobot.event.EventBus;

/**
 * Created by liuchao on 2015/12/7.
 */
public class StartQueryRequest {
    // 控制变量，标识startQuery接口是否拉去成功
    public static boolean isStartQuerySuccess = false;

    /**
     * 请求startquery接口获取WEBVIEW数据
     */
    public static void startQueryRequest(Activity mContext) {
        if (mContext == null) {
            StartActivity.isLoadStartQueryComplete = true;
            return;
        }
        // 若已经拉取成功则不再拉取
        if (isStartQuerySuccess) {
            StartActivity.isLoadStartQueryComplete = true;
            return;
        }
        // 判断当前网络
        if (!Config.isNetworkConnected(mContext)) {
            StartActivity.isLoadStartQueryComplete = true;
            return;
        }
        L.i("开始调用startQuery接口.....");
        UserInterface.StartQueryInterface.Request.Builder request = UserInterface.StartQueryInterface.Request.newBuilder();
        NetworkTask task = new NetworkTask(Cmd.CmdCode.StartQueryInterface_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        UserInterface.StartQueryInterface.Response response = UserInterface.StartQueryInterface.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            // 更新本地URL信息
                            URLConfig.updateURLInfo(response, URLConfig.getUrlInfo());
                            isStartQuerySuccess = true;
                            L.i("调用startQuery接口成功....发送event事件，更新侧导（余额）文案");
                            // 发送通知更新侧导（余额）文案
                            BaseEvent baseEvent = new BaseEvent(EventBusConstant.EVENT_TYPE_REFRESH_NAVIGATION, URLConfig.getUrlInfo().getRechargeDesc());
                            EventBus.getDefault().post(baseEvent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
            }

            @Override
            public void networkFinish() {
                StartActivity.isLoadStartQueryComplete = true;
            }
        });
    }
}
