package com.youyou.uuelectric.renter.UI.pay;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.VolleyError;
import com.google.protobuf.InvalidProtocolBufferException;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.order.pb.bean.OrderInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;

import java.util.List;

/**
 * Created by aaron on 16/5/3.
 *  解决用户取消订单，重复支付的问题
 */
public class PayCancelUtils {

    /**
     * 用户打开第三方支付之后点击取消，则发送请求，通知服务器端，解决重复支付的问题
     */
    public static final void payCancelNoticeServer(Context mContext, final List<String> orderList, int payType) {
        if (!Config.isNetworkConnected(mContext)) {
            L.e("用户取消第三方支付上报服务器失败，当前没有网络...");
            return;
        }

        OrderInterface.PayActionCancel.Request.Builder request = OrderInterface.PayActionCancel.Request.newBuilder();
        request.addAllOrderId(orderList);
        if (payType == BasePayFragmentUtils.ORDER_TYPE_COMMON) {
            request.setType(OrderInterface.PayActionConfirmType.ORDER);
        } else if (payType ==BasePayFragmentUtils.ORDER_TYPE_H5) {
            request.setType(OrderInterface.PayActionConfirmType.ARREAR);
        }
        NetworkTask task = new NetworkTask(Cmd.CmdCode.PayActionCancel_NL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                OrderInterface.PayActionCancel.Response response = null;
                try {
                    response = OrderInterface.PayActionCancel.Response.parseFrom(responseData.getBusiData());
                    if (response.getRet() == 0) {
                        L.i("用户取消第三方支付，上报服务器成功");
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                L.e("用户取消第三方支付，上报服务器失败:onError()...");
            }

            @Override
            public void networkFinish() {
                orderList.clear();
            }
        });
    }
}
