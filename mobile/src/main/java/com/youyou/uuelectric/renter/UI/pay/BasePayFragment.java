package com.youyou.uuelectric.renter.UI.pay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.youyou.uuelectric.renter.UI.base.BaseFragment;

/**
 * Created by liuchao on 2015/9/12.
 * 支付Fragment基类
 */
public class BasePayFragment extends BaseFragment {

    @Override
    public View setView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    /*@Override
    public void onResume() {
        super.onResume();
        *//**
         * 支付的初始化
     *//*
        BasePayFragmentUtils.onPayResume(this);
    }


    *//**
     * ################################################# 支付相关逻辑代码  #########################################
     *//*

    // 支付相关参数
    PayCommon.PayInfo payInfo;
    // 支付金额
    public float needRechargeAmount;


    *//**
     * 开始执行支付动作
     *
     * @param payInfo
     *//*
    public void queryPayOrderInfo(PayCommon.PayInfo payInfo) {
        // 初始化支付动作
        BasePayFragmentUtils.payActionIsOver = false;
        BasePayFragmentUtils.isPayBack = false;
        BasePayFragmentUtils.isReceivePush = false;

        this.payInfo = payInfo;
        needRechargeAmount = this.payInfo.getRechargeAmout();
        final UuCommon.ThirdPayType type = payInfo.getType();
        if (type.equals(UuCommon.ThirdPayType.ALIPAY)) {
            toAlipay();
        } else if (type.equals(UuCommon.ThirdPayType.WECHAT)) {
            sendPayReq();
        }
    }


    // ########################### 微信支付 start ##################################################
    // 微信支付相关参数，可参考：https://pay.weixin.qq.com/wiki/doc/api/app.php?chapter=9_12&index=2
    public IWXAPI api;
    // APP_ID 替换为你的应用从官方网站申请到的合法appId
    public static final String APP_ID = Config.WX_APP_ID;
    // 商家向财付通申请的商家id
    public static final String PARTNER_ID = "1268656901";

    boolean isPayCallback;
    boolean isWECHAT = false;

    private void sendPayReq() {
        PayReq req = new PayReq();
        // 填充APP_ID
        if (payInfo.getWechatPayParam().hasAppId()) {
            req.appId = payInfo.getWechatPayParam().getAppId();
        } else {
            req.appId = APP_ID;
        }
        // 填充商户ID
        if (payInfo.getWechatPayParam().hasMchId()) {
            req.partnerId = payInfo.getWechatPayParam().getMchId();
        } else {
            req.partnerId = PARTNER_ID;
        }
        // 预支付ID
        req.prepayId = payInfo.getWechatPayParam().getPrePayId();
        // 扩展字段--暂填写固定值Sign=WXPay
        req.packageValue = "Sign=WXPay";
        // 随机字符串
        if (payInfo.getWechatPayParam().hasNonceStr()) {
            req.nonceStr = payInfo.getWechatPayParam().getNonceStr();
        } else {
            req.nonceStr = BasePayFragmentUtils.genNonceStr();
        }
        // 时间戳
        if (payInfo.getWechatPayParam().hasTimestamp()) {
            req.timeStamp = String.valueOf(payInfo.getWechatPayParam().getTimestamp());
        } else {
            req.timeStamp = String.valueOf(BasePayFragmentUtils.genTimeStamp());
        }

        List<NameValuePair> signParams = new LinkedList<NameValuePair>();
        signParams.add(new BasicNameValuePair("appid", req.appId));
        signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
        signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
        signParams.add(new BasicNameValuePair("package", req.packageValue));
        signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
        signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));

        // 签名
        if (payInfo.getWechatPayParam().hasSign()) {
            req.sign = payInfo.getWechatPayParam().getSign();
        } else {
            req.sign = BasePayFragmentUtils.genSign(signParams);
        }

        // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
        boolean isOK = api.sendReq(req);
        if (isOK) {
            isPayCallback = true;
            isWECHAT = true;
        }
    }


    // ########################### 支付宝支付 start ##################################################
    // 支付宝参数
    private static final int SDK_PAY_FLAG = 1;

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG:
                    PayResult payResult = new PayResult((String) msg.obj);
                    // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                    String resultInfo = payResult.getResult();
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // loading中，等待服务器端通知支付是否成功的PUSH
                        BasePayFragmentUtils.onPaySuccess(BasePayFragment.this);

                    } else {
                        // 判断resultStatus 为非“9000”则代表可能支付失败
                        // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        showSnackBarMsg("支付失败，请重新支付!");
                    }
                    break;
                default:
                    break;
            }
        }
    };


    *//**
     * 跳转支付宝支付
     *//*
    public void toAlipay() {
        try {
            // 生成支付信息
            String orderInfo = BasePayFragmentUtils.getNewOrderInfo(payInfo, needRechargeAmount);
            // 生成SIGN
            String sign = SignUtils.sign(orderInfo, "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALvPYHTEZS+HbrY0NECzpMTjjRQrxvDaN0hlJqKHO/8+uiqeaJL1FhIWWy0Ok1/qUOyIxWPrGB3N0YMMxTnGOBHbbeOmbws3w8+L9Qsu+FHP3LaGdDyDqTS41puibeLvhYB0e5m9s+USrgdno2u9xFTs6OvcKIKHgnozkoCoK+BxAgMBAAECgYB6/ZVnIm45L/HatFk7vek7XuE2wmxnsh/d8w/YA8PQpZ1454AILSQk+CsBWLg+ac5Q+Eh75KtIaU65CZXm/d4dA9ohY68i+qukoLBivDa9q0ST+G47Nya+m9JEBRwqdGZ6T0zZ4vJn1vEeaWmbKKVHBrcYpZNoe5UK8+6fzdJMAQJBAOl+OsNlsJOCTSDFXlkM2ZR28zLPYWjBGV7uhQJOZvBCm8oclqR+zQSdakW0oxDgWUpfvKRvO5zWeGEJWEcbcqECQQDN6dq+BwMK9If2yzTXxaQlMd+L5DRwRqnM+bu/oNdKvDDBL499Gu1o+cKi7cQ2TnUszPkTmaallvW7VcENmmvRAkEAgTNAAO88DeOEGiYcVtota2GGoQ7vr69qAoWpQ+VuQHQbEHNRSCSB/ZO9QmT59lSuE+F12OdT7S0f31H0byRZAQJADJFZaH/FD8YdBlMgxoqpmhuRKVikWrX1Zy1W6DtI6KbT0va0K06Zbu7PkmIwt5/SRwm7qhaWtUSheu2g+tOSAQJAfwmpJbhVYebxW38oseASPrwQzcG3htK2wTd9n178KBQkO7siLPQfqfCXA6QJ9C076Cg3LkzoIO23C81HNht3aw==");
            sign = URLEncoder.encode(sign, "UTF-8");
            // 完整的符合支付宝参数规范的订单信息
            final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
                    + BasePayFragmentUtils.getSignType();
            Runnable payRunnable = new Runnable() {

                @Override
                public void run() {
                    // 构造PayTask 对象
                    PayTask alipay = new PayTask(mContext);
                    // 调用支付接口，获取支付结果
                    String result = alipay.pay(payInfo);
                    Message msg = new Message();
                    msg.what = SDK_PAY_FLAG;
                    msg.obj = result;
                    mHandler.sendMessage(msg);
                }
            };

            // 必须异步调用
            Thread payThread = new Thread(payRunnable);
            payThread.start();
        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }*/

}
