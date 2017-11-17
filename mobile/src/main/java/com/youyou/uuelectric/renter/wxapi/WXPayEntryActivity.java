package com.youyou.uuelectric.renter.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.youyou.uuelectric.renter.UI.main.MainLoopActivity;
import com.youyou.uuelectric.renter.UI.pay.BasePayFragmentUtils;
import com.youyou.uuelectric.renter.UI.pay.PayCancelUtils;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;

/**
 * 微信支付回调监听
 */
public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {


    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, Config.WX_APP_ID, true);
        api.registerApp(Config.WX_APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
    }

    @Override
    public void onResp(BaseResp resp) {
        L.i("支付返回，返回码： " + resp.errCode);
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            // 支付成功
            if (resp.errCode == BaseResp.ErrCode.ERR_OK) {
                BasePayFragmentUtils.isPaySuccessForWeiChat = true;
            }
            // 支付失败
            else {
                // 支付取消，则通知服务器
                // if (resp.errCode == BaseResp.ErrCode.ERR_USER_CANCEL) {
                    PayCancelUtils.payCancelNoticeServer(this, BasePayFragmentUtils.orderList, BasePayFragmentUtils.orderType);
                // }
                BasePayFragmentUtils.isPaySuccessForWeiChat = false;
                MainLoopActivity.isCanLoop = true;
                Config.showToast(this, "支付失败，请重新支付!");
            }
        }
        finish();
    }
}
