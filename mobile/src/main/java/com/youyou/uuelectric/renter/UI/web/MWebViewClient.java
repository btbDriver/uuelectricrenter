package com.youyou.uuelectric.renter.UI.web;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.uu.access.app.header.HeaderCommon;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.base.common.UuCommon;
import com.uu.facade.pay.pb.bean.PayCommon;
import com.uu.facade.pay.pb.iface.PayInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.SysConfig;
import com.youyou.uuelectric.renter.Utils.UrlBase64;
import com.youyou.uuelectric.renter.Utils.share.ShareInfo;
import com.youyou.uuelectric.renter.Utils.share.ShareSnsUtils;
import com.youyou.uuelectric.renter.Utils.task.ActivityUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuchao on 2015/9/1.
 */
public class MWebViewClient extends WebViewClient {

    public H5Fragment h5Fragment = null;
    public Activity h5Activity = null;

    public MWebViewClient(H5Fragment h5Fragment) {
        this.h5Fragment = h5Fragment;
        if (h5Fragment.getActivity() == null) {
            h5Activity = Config.currentContext;
        } else {
            h5Activity = h5Fragment.getActivity();
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        //解析scheme
        if (url.indexOf(H5Constant.SCHEME) != -1) {
            try {
                Uri uri = Uri.parse(url);
                String[] urlSplit = url.split("\\?");
                Map<String, String> queryMap = new HashMap<String, String>();
                String h5Url = null;
                if (urlSplit.length == 2) {
                    queryMap = H5Constant.parseUriQuery(urlSplit[1]);
                    h5Url = queryMap.get(H5Constant.MURL);
                }

                /**
                 * desc:需要判断是否包含token,若包含token的话，需要在网络请求的header中添加Jump2ClientSource参数
                 */
                //判断是否执行back动作
                if (queryMap.containsKey(H5Constant.ACTION_NAME) && "back".equals(queryMap.get(H5Constant.ACTION_NAME).trim())) {

                    //返回N层
                    String reloadpre = queryMap.get(H5Constant.NUMBEROFCLOSEPRE);
                    Intent intent = new Intent();
                    if (queryMap.containsKey(H5Constant.NUMBEROFCLOSEPRE) && !"".equals(queryMap.get(H5Constant.NUMBEROFCLOSEPRE))) {
                        int numberOfClosePre = Integer.valueOf(reloadpre);
                        // 关闭N层activity
                        ActivityUtil.closeNumberActivities(numberOfClosePre + 1);
                    }
                    // 若设置刷新，则刷新页面
                    if (queryMap.containsKey(H5Constant.RELOADPRE) && "1".equals(queryMap.get(H5Constant.RELOADPRE))) {
                        H5Constant.isNeedFlush = true;
                    }
                    h5Activity.setResult(H5Constant.h5ResultCode, intent);
                    h5Activity.finish();
                }
                // 分享内容
                else if ("shareToSNS".equals(queryMap.get(H5Constant.ACTION_NAME))) {
                    shareOption(queryMap);
                }
                // 调用相机
                else if ("camera".equals(queryMap.get(H5Constant.ACTION_NAME))) {
                    // 保存js的回调函数
                    if (queryMap.containsKey(H5Constant.CALLBACK) && !TextUtils.isEmpty(queryMap.get(H5Constant.CALLBACK))) {
                        H5Constant.cameraCallback = queryMap.get(H5Constant.CALLBACK);
                    }
                    openCamera();
                }
                // 支付其他款项
                else if ("payForOtherItem".equals(queryMap.get(H5Constant.ACTION_NAME))) {
                    payForOtherItem(queryMap);
                }
                // 跳转NativeAppActivity解析
                else {
                    // 若设置刷新，则刷新页面
                    if (queryMap.containsKey(H5Constant.RELOADPRE) && "1".equals(queryMap.get(H5Constant.RELOADPRE))) {
                        h5Fragment.isNeedFlushPreH5 = true;
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    h5Activity.startActivityForResult(intent, H5Constant.h5RequestCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        // 打电话
        else if (url.indexOf("tel://") != -1) {
            final String number = url.substring("tel://".length());
            Config.callPhoneByNumber(h5Activity, number);
            return true;
        } else if (url.indexOf("tel:") != -1) {
            final String number = url.substring("tel:".length());
            Config.callPhoneByNumber(h5Activity, number);
            return true;
        }
        // 其他跳转方式
        else {
            view.loadUrl(url);
            //如果不需要其他对点击链接事件的处理返回true，否则返回false
            return false;
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);

    }


    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        h5Fragment.swipeRefreshLayout.setRefreshing(false);
        if (h5Activity.getTitle().toString().equals("找不到网页")) {
            h5Fragment.mProgressLayout.showError(h5Fragment.errorOnClickListener);
            return;
        }
        if (h5Fragment.isSuccess)
            h5Fragment.mProgressLayout.showContent();
        else
            h5Fragment.mProgressLayout.showError(h5Fragment.errorOnClickListener);

        h5Fragment.onLoadFinish(h5Fragment.isSuccess);
        if (h5Fragment.isSuccess) {
            h5Fragment.mWebView.getSettings().setBlockNetworkImage(false);
        }
    }

    // 该方法为android23中新添加的API，android23中会执行该方法
    @TargetApi(21)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        if (Build.VERSION.SDK_INT >= 21) {
            if (request.isForMainFrame()) {
                h5Fragment.isSuccess = false;
                h5Fragment.mProgressLayout.showError(h5Fragment.errorOnClickListener);
            }
        }
    }

    /**
     * 在android23中改方法被onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) 替代
     * 因此在android23中执行替代方法
     * 在android23之前执行该方法
     * @param view
     * @param errorCode
     * @param description
     * @param failingUrl
     */
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (Build.VERSION.SDK_INT < 23) {
            h5Fragment.isSuccess = false;
            h5Fragment.mProgressLayout.showError(h5Fragment.errorOnClickListener);
        }
    }

    // httpError方法被删除点，这是请求http网页，资源文件错误时的回调方法，这是android23时新添加的API
    /*@TargetApi(21)
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
    }*/

    // sslError方法删除掉，这是请求ssl请求,资源文件收到错误时的回调方法
    /*@Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
    }*/

    /**
     * 解析Scheme进行分享操作
     *
     * @param queryMap
     */
    private void shareOption(Map<String, String> queryMap) {
        ShareInfo shareInfo = new ShareInfo();
        String title = queryMap.get("title");
        if (!TextUtils.isEmpty(title)) {
            title = UrlBase64.decode(title);
            shareInfo.setShareTitle(title);
        }
        String urlWechat = queryMap.get("urlWechat");
        if (!TextUtils.isEmpty(urlWechat)) {
            urlWechat = UrlBase64.decode(urlWechat);
            shareInfo.setShareUrlWechart(urlWechat);
        }
        String urlFriend = queryMap.get("urlFriend");
        if (!TextUtils.isEmpty(urlFriend)) {
            urlFriend = UrlBase64.decode(urlFriend);
            shareInfo.setShareUrlFriend(urlFriend);
        }
        String content = queryMap.get("content");
        if (!TextUtils.isEmpty(content)) {
            content = UrlBase64.decode(content);
            shareInfo.setContext(content);
        }
        String url = queryMap.get("url");
        if (!TextUtils.isEmpty(url)) {
            url = UrlBase64.decode(url);
            shareInfo.setUrl(url);
        }
        ShareSnsUtils shareSnsUtils = new ShareSnsUtils(h5Activity, shareInfo);
        shareSnsUtils.showShareDialog(h5Fragment.rl);
    }


    /**
     * 跳转支付
     */
    public void payForOtherItem(Map<String, String> queryMap) {
        if (queryMap == null) {
            Config.showFiledToast(h5Activity);
            return;
        }
        // 解析SCHEME
        try {
            String businessType = queryMap.get("businessType"); // 支付的业务类型：1-充值，2-偿还欠费
            String orderIds = queryMap.get("orderId"); // 订单ID，已"|"分割开
            String feeTotal = queryMap.get("feeTotal");// 支付总金额
            String payType = queryMap.get("payType");// 支付类型

            if (TextUtils.isEmpty(businessType) || TextUtils.isEmpty(payType)) {
                Config.showToast(h5Activity, h5Activity.getString(R.string.network_error_tip));
                return;
            }
            PayInterface.PayForOtherItem.Request.Builder request = PayInterface.PayForOtherItem.Request.newBuilder();
            request.setBusinessType(Integer.parseInt(businessType)); // 支付类型
            // 判断当前支付总额是否为空，为空的话，不再设置
            if (!TextUtils.isEmpty(feeTotal)) {
                request.setFineTotal(Float.parseFloat(feeTotal));// 支付总额
            }
            int intPayType = Integer.parseInt(payType);// 支付方式
            UuCommon.ThirdPayType thirdPayType;
            if (intPayType == 2) {
                thirdPayType = UuCommon.ThirdPayType.ALIPAY;
            } else {
                thirdPayType = UuCommon.ThirdPayType.WECHAT;
            }
            request.setPayType(thirdPayType);
            // 保存订单列表，传递给支付接口，当用户调用第三方支付点击取消的时候回根据订单列表上报服务器解决重复支付的问题
            final ArrayList<String> orderList = new ArrayList<String>();
            // 判断订单ID是否为空，为空的话则不再设置
            if (!TextUtils.isEmpty(orderIds)) {
                String[] orderId = orderIds.split("\\|");
                orderList.addAll(Arrays.asList(orderId));
                request.addAllOrderId(orderList);
            }

            Config.showProgressDialog(h5Activity, false, null);
            NetworkTask task = new NetworkTask(Cmd.CmdCode.PayForOtherItem_NL_VALUE);
            task.setBusiData(request.build().toByteArray());
            NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
                @Override
                public void onSuccessResponse(UUResponseData responseData) {
                    if (responseData.getRet() == 0) {
                        PayCommon.PayInfo payInfo = null;
                        try {
                            showResponseCommonMsg(h5Activity, responseData.getResponseCommonMsg());
                            PayInterface.PayForOtherItem.Response response = PayInterface.PayForOtherItem.Response.parseFrom(responseData.getBusiData());
                            if (response.getRet() == 0) {
                                payInfo = response.getPayInfo();
                            } else if (response.getRet() == -1){
                                Config.showToast(h5Activity, "支付失败");
                            } else if (response.getRet() == -2){
                                // Config.showToast(h5Activity, "为避免重复付款，请稍候支付");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Config.showToast(h5Activity, h5Activity.getString(R.string.network_error_tip));
                        }
                        if (payInfo != null) {
                            h5Fragment.doPayOption(payInfo, orderList);
                        }
                    } else {
                        Config.showToast(h5Activity, h5Activity.getString(R.string.network_error_tip));
                    }
                }

                @Override
                public void onError(VolleyError errorResponse) {
                    Config.showToast(h5Activity, h5Activity.getString(R.string.network_error_tip));
                }

                @Override
                public void networkFinish() {
                    Config.dismissProgress();
                }
            });
        } catch (Exception e) {
            Config.showToast(h5Activity, h5Activity.getString(R.string.network_error_tip));
            Config.dismissProgress();
        }
    }

    /**
     * 初始化相机控件
     */
    private void openCamera() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            // 部分机型更改了action的值，这里尽量使用系统定义的常量
            Intent getImageByCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (TextUtils.isEmpty(H5Constant.bigPicPath)) {
                H5Constant.bigPicPath = SysConfig.SD_IMAGE_PATH + H5Constant.tempImage;
            }
            // 将相机所拍摄图片保存在临时目录
            getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(H5Constant.bigPicPath)));
            // 添加隐士intent跳转的判断，若没有对应的activity，则不做处理
            if (getImageByCamera.resolveActivity(h5Activity.getPackageManager()) != null) {
                h5Activity.startActivityForResult(getImageByCamera, H5Constant.h5CameraCode);
            }
        } else {
            Toast.makeText(h5Activity, "请确认已经插入SD卡", Toast.LENGTH_LONG).show();
        }
    }

    public void showResponseCommonMsg(Activity h5Activity, HeaderCommon.ResponseCommonMsg msg) {
        if (msg.getMsg() != null && msg.getMsg().length() > 0) {
            if (msg.hasShowType()) {
                if (msg.getShowType().equals(HeaderCommon.ResponseCommonMsgShowType.TOAST)) {
                    showSnackBarMsg(h5Activity, msg.getMsg());
                } else if (msg.getShowType().equals(HeaderCommon.ResponseCommonMsgShowType.WINDOW)) {
                    showResponseCommonMsg(h5Activity, msg, null);
                }
            } else {
                showSnackBarMsg(h5Activity, msg.getMsg());
            }
        }
    }

    /**
     * 显示一个默认时间的SnackBar
     *
     * @param msg
     */
    public void showSnackBarMsg(Activity h5Activity, String msg) {
        Config.showToast(h5Activity, msg);
    }

    public void showResponseCommonMsg(Activity h5Activity, HeaderCommon.ResponseCommonMsg msg, View.OnClickListener listener) {
        /*AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(msg.getMsg());
        builder.setNegativeButton(msg.getButtonsList().get(0).getButtonText(), listener);
        Dialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();*/

        Config.showTiplDialog(h5Activity, null, msg.getMsg(), msg.getButtonsList().get(0).getButtonText(), null);
    }
}
