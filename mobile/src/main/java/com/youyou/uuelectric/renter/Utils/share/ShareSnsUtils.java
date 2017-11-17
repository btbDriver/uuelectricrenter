package com.youyou.uuelectric.renter.Utils.share;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.web.H5Activity;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;

/**
 * User: qing
 * Date: 2015-05-25 15:58
 * Desc: 执行分享的工具类
 */
public class ShareSnsUtils implements View.OnClickListener {
    public static final String APP_ID = Config.WX_APP_ID;
    /**
     * 应用上下文
     */
    private Activity context;
    /**
     * 微信分享API
     */
    public IWXAPI api;
    /**
     * 分享对象
     */
    public ShareInfo mShareInfo;
    /**
     * 分享弹窗
     */
    private AlertDialog dialog;


    // ##############  构造方法 start ####################
    public ShareSnsUtils(Activity context) {
        this.context = context;
        if (api == null) {
            api = WXAPIFactory.createWXAPI(context, APP_ID, true);
            api.registerApp(APP_ID);
        }
    }

    public ShareSnsUtils(Activity context, ShareInfo shareInfo) {
        this.context = context;
        this.mShareInfo = shareInfo;
        if (api == null) {
            api = WXAPIFactory.createWXAPI(context, APP_ID, true);
            api.registerApp(APP_ID);
        }
    }
    // ############### 构造方法 end #######################

    /**
     * 显示分享弹窗
     */
    public void showShareDialog(RelativeLayout rl) {
        //普通的dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.share_to_sns_dialog, null);
        LinearLayout llPYQ = (LinearLayout) view.findViewById(R.id.ll_pyq);
        LinearLayout llWeixin = (LinearLayout) view.findViewById(R.id.ll_weixin);

        llPYQ.setOnClickListener(this);
        llWeixin.setOnClickListener(this);

        builder.setView(view);
        dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        //弹窗效果的dialog（分享红包）
        /*final SweetSheet mSweetSheet3 = new SweetSheet(rl);
        mSweetSheet3.setBackgroundEffect(new DimEffect(0.7f));
        CustomDelegate customDelegate = new CustomDelegate(false,
                CustomDelegate.AnimationType.DuangLayoutAnimation);
        View view = LayoutInflater.from(context).inflate(R.layout.coupon_dialog_layout, null, false);
        customDelegate.setCustomView(view);
        customDelegate.setSweetSheetColor(Color.WHITE);
        mSweetSheet3.setDelegate(customDelegate);
        mSweetSheet3.setBackgroundClickEnable(true);

        view.findViewById(R.id.circle_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!Config.isNetworkConnected(context)) {
                    H5Activity h5Activity = (H5Activity) context;
                    h5Activity.showDefaultNetworkSnackBar();
                    return;
                }
                shareByFriends();
                mSweetSheet3.dismiss();
            }
        });
        view.findViewById(R.id.friend_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Config.isNetworkConnected(context)) {
                    H5Activity h5Activity = (H5Activity) context;
                    h5Activity.showDefaultNetworkSnackBar();
                    return;
                }
                shareByWeixin();
                mSweetSheet3.dismiss();
            }
        });
        mSweetSheet3.toggle();*/

    }

    @Override
    public void onClick(View v) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
        switch (v.getId()) {
            case R.id.ll_pyq:
                if (!Config.isNetworkConnected(context)) {
                    H5Activity h5Activity = (H5Activity) context;
                    h5Activity.showDefaultNetworkSnackBar();
                    return;
                }
                shareByFriends();
                break;
            case R.id.ll_weixin:
                if (!Config.isNetworkConnected(context)) {
                    H5Activity h5Activity = (H5Activity) context;
                    h5Activity.showDefaultNetworkSnackBar();
                    return;
                }
                shareByWeixin();
                break;
        }
    }

    /**
     * 微信分享
     */
    public void shareByWeixin() {
        if (Config.isWXAppInstalled(context)) {
            toOperatorWechart(mShareInfo);
        } else {
            showNotWechartDialog();
        }
    }

    /**
     * 朋友圈分享
     */
    public void shareByFriends() {
        if (Config.isWXAppInstalled(context)) {
            toOperatorFriend(mShareInfo);
        } else {
            showNotWechartDialog();
        }
    }

    /**
     * 分享到微信
     * @param shareInfo
     */
    private void toOperatorWechart(ShareInfo shareInfo) {
        WXWebpageObject webpage = new WXWebpageObject();
        if (!TextUtils.isEmpty(shareInfo.getShareUrlWechart())) {
            webpage.webpageUrl = shareInfo.getShareUrlWechart();
        } else {
            webpage.webpageUrl = shareInfo.getUrl();
        }

        Config.reportLog(1, "WeChatFriend\\t" + webpage.webpageUrl);
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = shareInfo.getShareTitle();
        msg.description = shareInfo.getContext();
        msg.setThumbImage(BitmapFactory.decodeResource(context.getResources(), R.mipmap.share_icon));
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;
        boolean result = api.sendReq(req);
        L.i("share wechart result:" + result);
    }

    /**
     * 分享到朋友圈
     * @param shareInfo
     */
    private void toOperatorFriend(ShareInfo shareInfo) {
        WXWebpageObject webpage2 = new WXWebpageObject();
        if (!TextUtils.isEmpty(shareInfo.getShareUrlFriend())) {
            webpage2.webpageUrl = shareInfo.getShareUrlFriend();
        } else {
            webpage2.webpageUrl = shareInfo.getUrl();
        }

        Config.reportLog(1, "WeChatTimeline\\t" + webpage2.webpageUrl);
        WXMediaMessage msg2 = new WXMediaMessage(webpage2);
        msg2.title = shareInfo.getShareTitle();
        msg2.description = shareInfo.getContext();
        msg2.setThumbImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.get_friend_icon));
        SendMessageToWX.Req req2 = new SendMessageToWX.Req();
        req2.transaction = String.valueOf(System.currentTimeMillis());
        req2.message = msg2;
        req2.scene = SendMessageToWX.Req.WXSceneTimeline;
        boolean result = api.sendReq(req2);
        L.i("share friend result:" + result);
    }

    /**
     * 微信分享
     */
    public void shareByWeixin(final ShareInfo shareInfo) {
        if (Config.isWXAppInstalled(context)) {
            {
                if (shareInfo.imgUrl != null && shareInfo.imgUrl.indexOf("http") != -1) {

                    UUApp.getInstance().getImageLoaderInstance().get(shareInfo.imgUrl, new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            if (response.getBitmap() != null) {
                                WXWebpageObject webpage = new WXWebpageObject();
                                if (!TextUtils.isEmpty(shareInfo.getShareUrlWechart())) {
                                    webpage.webpageUrl = shareInfo.getShareUrlWechart();
                                } else {
                                    webpage.webpageUrl = shareInfo.getUrl();
                                }
                                Config.reportLog(1, "WeChatFriend\\t" + webpage.webpageUrl);
                                WXMediaMessage msg = new WXMediaMessage(webpage);
                                msg.title = shareInfo.getShareTitle();
                                msg.description = shareInfo.getContext();
                                msg.setThumbImage(response.getBitmap());
                                SendMessageToWX.Req req = new SendMessageToWX.Req();
                                req.transaction = String.valueOf(System.currentTimeMillis());
                                req.message = msg;
                                req.scene = SendMessageToWX.Req.WXSceneSession;
                                api.sendReq(req);
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            toOperatorWechart(shareInfo);
                        }
                    });
                } else {
                    toOperatorWechart(shareInfo);
                }

            }

        } else {
            showNotWechartDialog();
        }
    }


    /**
     * 朋友圈分享
     */
    public void shareByFriends(final ShareInfo shareInfo) {
        if (Config.isWXAppInstalled(context)) {
            if (shareInfo.imgUrl != null && shareInfo.imgUrl.indexOf("http") != -1) {

                UUApp.getInstance().getImageLoaderInstance().get(shareInfo.imgUrl, new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        if (response.getBitmap() != null) {
                            WXWebpageObject webpage2 = new WXWebpageObject();
                            if (!TextUtils.isEmpty(shareInfo.getShareUrlFriend())) {
                                webpage2.webpageUrl = shareInfo.getShareUrlFriend();
                            } else {
                                webpage2.webpageUrl = shareInfo.getUrl();
                            }
                            Config.reportLog(1, "WeChatFriend\\t" + webpage2.webpageUrl);
                            WXMediaMessage msg2 = new WXMediaMessage(webpage2);
                            msg2.title = shareInfo.getShareTitle();
                            msg2.description = shareInfo.getContext();
                            msg2.setThumbImage(response.getBitmap());
                            SendMessageToWX.Req req2 = new SendMessageToWX.Req();
                            req2.transaction = String.valueOf(System.currentTimeMillis());
                            req2.message = msg2;
                            req2.scene = SendMessageToWX.Req.WXSceneTimeline;
                            api.sendReq(req2);
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        toOperatorFriend(shareInfo);
                    }
                });
            } else {
                toOperatorFriend(shareInfo);
            }

        } else {
            showNotWechartDialog();
        }
    }


    /**
     * 显示未安装微信弹窗
     */
    public void showNotWechartDialog() {
        Config.showToast(context, "您未安装微信，无法分享");
    }
}
