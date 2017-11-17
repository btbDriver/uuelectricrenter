package com.youyou.uuelectric.renter.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.Support.Config;

/**
 * 微信分享回调监听类
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    public Activity context;
    public String tag = "WXEntryActivity";
    // IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI api;

    public void onCreate(Bundle b) {
        super.onCreate(b);
        Config.setActivityState(this);
        context = this;
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
    public void onReq(BaseReq baseReq) {
    }

    @Override
    public void onResp(BaseResp resp) {
        String result = "";
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result = "分享成功";
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = "取消分享";
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = "分享失败";
                break;
        }
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UUApp.getInstance().removeActivity(this);
        api.detach();
        api = null;
    }
}
