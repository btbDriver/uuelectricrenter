package com.youyou.uuelectric.renter.UI.pay;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;
import com.android.volley.VolleyError;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.base.common.UuCommon;
import com.uu.facade.pay.pb.bean.PayCommon;
import com.uu.facade.user.protobuf.bean.UserInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.Service.LoopRequest;
import com.youyou.uuelectric.renter.UI.base.BaseFragment;
import com.youyou.uuelectric.renter.UI.main.MainActivity;
import com.youyou.uuelectric.renter.UI.main.MainLoopActivity;
import com.youyou.uuelectric.renter.UI.order.TripOrderDetailActivity;
import com.youyou.uuelectric.renter.UI.web.H5Constant;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;
import com.youyou.uuelectric.renter.pay.MD5;
import com.youyou.uuelectric.renter.pay.PayResult;
import com.youyou.uuelectric.renter.pay.SignUtils;
import com.youyou.uuelectric.renter.pay.Util;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import de.greenrobot.event.EventBus;

/**
 * Created by liuchao on 2015/9/12.
 * desc:fragment支付工具类
 */
public class BasePayFragmentUtils {

    /**
     * 支付动作是否完成标识
     */
    public static boolean payActionIsOver = true;

    /**
     * 等待接收PUSH通知的最长时间
     */
    public static int WAIT_PUSH_MAX_TIME = 30 * 1000;

    /**
     * 支付是否返回
     */
    public static boolean isPayBack = false;
    /**
     * 是否接收到PUSH
     */
    public static boolean isReceivePush = false;
    /**
     * 微信支付：是否支付成功，由微信SDK的返回码来决定此值是否为true
     */
    public static boolean isPaySuccessForWeiChat = false;

    // 支付相关参数
    private PayCommon.PayInfo payInfo;
    // 支付金额
    private float needRechargeAmount;
    private IWXAPI api;
    // 商家向财付通申请的商家id
    private static final String PARTNER_ID = "1290710601";
    // 支付宝参数
    private static final int SDK_PAY_FLAG = 1;

    private BaseFragment payFragment;
    private Activity mContext;
    /**
     * 订单类型：正常的支付租车费用
     */
    public static int ORDER_TYPE_COMMON = 1;
    /**
     * 订单类型：H5页面支付违章等相关费用
     */
    public static int ORDER_TYPE_H5 = 2;
    /**
     * 当前订单支付类型
     */
    public static int orderType = ORDER_TYPE_COMMON;
    /**
     * 支付订单列表（用户取消第三方支付时使用，修复重复支付的bug）
     * 普通订单支付时，列表大小为1
     * 其他待支付费用，列表大小为N
     * 充值支付时，列表大小为0
     */
    public static List<String> orderList = new ArrayList();

    public BasePayFragmentUtils(BaseFragment payFragment, int orderType) {
        this.payFragment = payFragment;
        this.orderType = orderType;
        mContext = payFragment.getActivity();
    }

    /**
     * 微信支付：onResume方法中执行支付初始化
     */
    public void onPayResume() {
        // SDK返回支付成功状态
        if (isPaySuccessForWeiChat) {
            if (orderType == ORDER_TYPE_COMMON) {
                onPaySuccess(payFragment);
            } else if (orderType == ORDER_TYPE_H5) {
                onH5PaySuccess();
            }
            isPaySuccessForWeiChat = false;
        }
    }

    /**
     * 开始执行支付动作：微信支付、支付宝支付
     *
     * @param info
     */
    public void queryPayOrderInfo(PayCommon.PayInfo info, List<String> orderList) {
        // 初始化支付动作
        payActionIsOver = false;
        isPayBack = false;
        isReceivePush = false;
        isPaySuccessForWeiChat = false;
        receivePushType = -1;

        H5Constant.isNeedFlush = false;

        payInfo = info;
        // 添加逻辑判断若支付参数为空则提示
        if (payInfo == null) {
            Config.showToast(mContext, mContext.getResources().getString(R.string.network_error_tip));
            return;
        }

        // 保存订单列表，用于取消第三方支付时的上报服务器
        this.orderList.clear();
        this.orderList.addAll(orderList);

        needRechargeAmount = payInfo.getRechargeAmout();
        final UuCommon.ThirdPayType type = payInfo.getType();
        if (type.equals(UuCommon.ThirdPayType.ALIPAY)) {
            toAlipay();
        } else if (type.equals(UuCommon.ThirdPayType.WECHAT)) {
            if (!Config.isWXAppInstalled(mContext)) {
                Config.showTiplDialog(mContext, "温馨提示", "您未安装微信，不能进行微信支付，请选择其他支付方式。", "知道了", null);
                payFragment.dismissProgress();
                return;
            }
            sendPayReq();
        }

        MainLoopActivity.isCanLoop = false;
    }

    /**
     * 微信支付初始化
     */
    private void sendPayReq() {
        // 商户APP工程中引入微信JAR包，调用API前，需要先向微信注册您的APPID
        api = WXAPIFactory.createWXAPI(payFragment.getContext(), Config.WX_APP_ID);
        api.registerApp(Config.WX_APP_ID);
        PayReq req = new PayReq();
        // 填充APP_ID
        if (payInfo.getWechatPayParam().hasAppId()) {
            req.appId = payInfo.getWechatPayParam().getAppId();
        } else {
            req.appId = Config.WX_APP_ID;
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
            req.nonceStr = genNonceStr();
        }
        // 时间戳
        if (payInfo.getWechatPayParam().hasTimestamp()) {
            req.timeStamp = String.valueOf(payInfo.getWechatPayParam().getTimestamp());
        } else {
            req.timeStamp = String.valueOf(genTimeStamp());
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
            req.sign = genSign(signParams);
        }

        // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
        api.sendReq(req);
    }

    /**
     * 支付宝支付初始化
     */
    public void toAlipay() {
        try {
            // 生成支付信息
            String orderInfo = getNewOrderInfo(payInfo, needRechargeAmount);
            // 生成SIGN
            String sign = SignUtils.sign(orderInfo, "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALvPYHTEZS+HbrY0NECzpMTjjRQrxvDaN0hlJqKHO/8+uiqeaJL1FhIWWy0Ok1/qUOyIxWPrGB3N0YMMxTnGOBHbbeOmbws3w8+L9Qsu+FHP3LaGdDyDqTS41puibeLvhYB0e5m9s+USrgdno2u9xFTs6OvcKIKHgnozkoCoK+BxAgMBAAECgYB6/ZVnIm45L/HatFk7vek7XuE2wmxnsh/d8w/YA8PQpZ1454AILSQk+CsBWLg+ac5Q+Eh75KtIaU65CZXm/d4dA9ohY68i+qukoLBivDa9q0ST+G47Nya+m9JEBRwqdGZ6T0zZ4vJn1vEeaWmbKKVHBrcYpZNoe5UK8+6fzdJMAQJBAOl+OsNlsJOCTSDFXlkM2ZR28zLPYWjBGV7uhQJOZvBCm8oclqR+zQSdakW0oxDgWUpfvKRvO5zWeGEJWEcbcqECQQDN6dq+BwMK9If2yzTXxaQlMd+L5DRwRqnM+bu/oNdKvDDBL499Gu1o+cKi7cQ2TnUszPkTmaallvW7VcENmmvRAkEAgTNAAO88DeOEGiYcVtota2GGoQ7vr69qAoWpQ+VuQHQbEHNRSCSB/ZO9QmT59lSuE+F12OdT7S0f31H0byRZAQJADJFZaH/FD8YdBlMgxoqpmhuRKVikWrX1Zy1W6DtI6KbT0va0K06Zbu7PkmIwt5/SRwm7qhaWtUSheu2g+tOSAQJAfwmpJbhVYebxW38oseASPrwQzcG3htK2wTd9n178KBQkO7siLPQfqfCXA6QJ9C076Cg3LkzoIO23C81HNht3aw==");
            sign = URLEncoder.encode(sign, "UTF-8");
            // 完整的符合支付宝参数规范的订单信息
            final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
                    + getSignType();
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
    }

    /**
     * 支付宝回调监听Handler
     */
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG:
                    PayResult payResult = new PayResult((String) msg.obj);
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        if (orderType == ORDER_TYPE_COMMON) {
                            onPaySuccess(payFragment);
                        } else if (orderType == ORDER_TYPE_H5) {
                            onH5PaySuccess();
                        }
                    }
                    // 支付失败
                    else {
                        // 用户取消支付
                        // if (TextUtils.equals(resultStatus, "6001")) {
                            PayCancelUtils.payCancelNoticeServer(mContext, orderList, orderType);
                        // }
                        // 判断resultStatus 为非“9000”则代表可能支付失败
                        // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        Config.showToast(mContext, "支付失败，请重新支付!");
                        MainLoopActivity.isCanLoop = true;
                    }
                    break;
                default:
                    break;
            }
        }
    };


    // ############################# 支付完成回调方法 start ###############################################

    /**
     * 接收到Push对应的类型：
     *  0 -- 其他待支付费用和违章费用的PUSH通知
     *  1 -- 余额充值的PUSH通知
     */
    public static int receivePushType = -1;
    /**
     * 支付成功后，服务器端下发的提醒文案
     */
    public static String payMsg = "";

    /**
     * H5页面支付成功后的回调
     */
    private void onH5PaySuccess() {
        isPayBack = true;
        payFragment.showProgress(false);
        // 如果在返回时，已经接收到PUSH，则直接跳转
        if (isReceivePush) {
            payFragment.dismissProgress();
            // 支付返回，主界面可以开始Loop
            MainLoopActivity.isCanLoop = true;
            // 设置支付动作已完成
            payActionIsOver = true;

            Config.showToast(mContext, payMsg);

            // 其他费用支付成功PUSH
            if (receivePushType == 0) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("goto", MainActivity.GOTO_MAP);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(IntentConfig.IF_REDBAG_SHOW,"toShowRedBag");
                payFragment.getActivity().startActivity(intent);
            }
            // 充值支付成功PUSH
            else if (receivePushType == 1) {
                // 返回上一级页面
                EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_ACTIVITY_PAY_BACK));
            }
        } else {
            // 支付返回后，未收到PUSH消息，需等待30s后手动去拉取消息
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    L.i("未收到服务端下发的支付成功PUSH，需要手动去拉取消息");
                    // 手动拉取消息
                    LoopRequest.getInstance(payFragment.getContext()).sendLoopRequest();
                }
            }, WAIT_PUSH_MAX_TIME);

            // PS:此处是容错处理，防止会出现界面卡在loading中状态，无法操作的情况
            // 等待35s后，如果监测支付动作还是没有完成，可能是服务器端保存PUSH消息存在问题，没有拉取到支付完成后的PUSH消息
            // 此时不能让界面处于loading状态，此时就选择信任第三方支付的支付返回码，确定本次支付是成功的。
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!payActionIsOver) {
                        payFragment.dismissProgress();
                        // 支付返回，主界面可以开始Loop
                        MainLoopActivity.isCanLoop = true;
                        // 设置支付动作已完成
                        payActionIsOver = true;
                        Config.showToast(mContext, "支付成功");

                        // 统一回主页
                        Intent intent = new Intent(mContext, MainActivity.class);
                        intent.putExtra("goto", MainActivity.GOTO_MAP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra(IntentConfig.IF_REDBAG_SHOW,"toShowRedBag");
                        payFragment.getActivity().startActivity(intent);
                    }
                }
            }, WAIT_PUSH_MAX_TIME + 5 * 1000);
        }
    }


    /**
     * 正常订单，支付成功后的操作
     */
    public void onPaySuccess(final BaseFragment payFragment) {
        isPayBack = true;
        payFragment.showProgress(false);

        // 如果在返回时，已经接收到PUSH，则直接跳转
        if (isReceivePush) {
            payFragment.dismissProgress();
            // 支付返回，主界面可以开始Loop
            MainLoopActivity.isCanLoop = true;
            // 设置支付动作已完成
            payActionIsOver = true;
            // 支付成功，跳转到支付成功界面
            Intent intent = new Intent(payFragment.getActivity(), TripOrderDetailActivity.class);
            intent.putExtra(IntentConfig.ORDER_ID, Config.getOrderId(mContext));
            intent.putExtra(IntentConfig.KEY_PAGE_TYPE, TripOrderDetailActivity.PAGE_TYPE_TRIP_DETAIL);
            intent.putExtra(IntentConfig.IF_REDBAG_SHOW,"toShowRedBag");
            payFragment.getActivity().startActivity(intent);
        } else {
            // 如果是微信支付返回，则间隔2S后拉取订单支付状态
            /*if (isPaySuccessForWeiChat) {
                WAIT_PUSH_MAX_TIME = 2 * 1000;
            }*/
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    // 在达到PUSH接收等待时间的值后依然没有收到PUSH时，手动发送请求，验证支付是否成功
                    if (!payActionIsOver) {
                        // 支付成功-后续操作
                        checkPayIsSuccess(payFragment);
                    }
                }
            }, WAIT_PUSH_MAX_TIME);
        }


    }

    /**
     * 检查支付是否成功
     */
    public void checkPayIsSuccess(final BaseFragment payFragment) {
        if (payActionIsOver) {
            L.i("支付已完成...");
            return;
        }
        if (!Config.isNetworkConnected(payFragment.mContext)) {
            payFragment.dismissProgress();
            payFragment.showNetworkErrorSnackBarMsg();
            // 支付返回，主界面可以开始Loop
            MainLoopActivity.isCanLoop = true;
            return;
        }
        UserInterface.UserInfo.Request.Builder request = UserInterface.UserInfo.Request.newBuilder();
        request.setR((int) (System.currentTimeMillis() / 1000));
        NetworkTask task = new NetworkTask(Cmd.CmdCode.UserInfo_NL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        payFragment.showResponseCommonMsg(responseData.getResponseCommonMsg());
                        UserInterface.UserInfo.Response response = UserInterface.UserInfo.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {

                            String orderId = response.getOrderId();
                            int orderStatus = response.getOrderStatus();
                            // 此时代表支付成功
                            if (TextUtils.isEmpty(orderId) || orderStatus != 4) {
                                // 设置支付标识为：已完成
                                payActionIsOver = true;

                                // 正常租车订单的支付成功跳转
                                Intent intent = new Intent(payFragment.getActivity(), TripOrderDetailActivity.class);
                                intent.putExtra(IntentConfig.ORDER_ID, Config.getOrderId(mContext));
                                intent.putExtra(IntentConfig.KEY_PAGE_TYPE, TripOrderDetailActivity.PAGE_TYPE_TRIP_DETAIL);
                                intent.putExtra(IntentConfig.IF_REDBAG_SHOW,"toShowRedBag");
                                payFragment.getActivity().startActivity(intent);

                            } else {
                                payFragment.showSnackBarMsg("支付失败，请重新支付!");
                            }

                        } else {
                            payFragment.showSnackBarMsg("支付失败，请重新支付!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                payFragment.showSnackBarMsg("支付失败，请重新支付!");
            }

            @Override
            public void networkFinish() {
                payFragment.dismissProgress();
            }
        });

        // 支付返回，主界面可以开始Loop
        MainLoopActivity.isCanLoop = true;
    }

    // ############################# 订单支付完成回调方法 end ###############################################




    // ############################# 支付宝支付工具方法  start ###############################################


    private String getSignType() {
        return "sign_type=\"RSA\"";
    }

    private String getNewOrderInfo(PayCommon.PayInfo payInfo, float needRechargeAmount) throws NumberFormatException, JSONException {

        String PARTNER = "2088911063557823";
        String SELLER = "zhimaxinyong@uuzuche.com";
        String body = "用于支付租车过程中产生的租车，保险，充电，清洁，救援，违章，出险等费用。";

        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + PARTNER + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + payInfo.getPayId() + "\"";

        // 商品名称
        orderInfo += "&subject=" + "\"" + "友友用车" + payInfo.getPayId() + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" + body + "\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\"" + needRechargeAmount + "\"";

        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + URLEncoder.encode(payInfo.getNotifyUrl())
                + "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"30m\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url=\"m.alipay.com\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";

        return orderInfo;


    }
    // ############################# 支付宝支付工具方法  end ###############################################

    // ############################# 微信支付工具方法 start ################################################

    private String genSign(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (; i < params.size() - 1; i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        sb.append(params.get(i).getName());
        sb.append('=');
        sb.append(params.get(i).getValue());
        String sha1 = Util.sha1(sb.toString());
        return sha1;
    }

    private String genNonceStr() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }

    private long genTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }
    // ############################# 微信支付工具方法 end   ################################################
}
