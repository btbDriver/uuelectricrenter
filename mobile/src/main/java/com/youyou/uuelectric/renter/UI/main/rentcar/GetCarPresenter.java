package com.youyou.uuelectric.renter.UI.main.rentcar;

import android.app.Dialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.protobuf.InvalidProtocolBufferException;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.order.pb.bean.OrderInterface;
import com.uu.facade.order.pb.common.OrderCommon;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.UI.base.BaseFragment;
import com.youyou.uuelectric.renter.UI.main.MainActivity;
import com.youyou.uuelectric.renter.UI.main.MsgHandler;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;

import java.util.Calendar;

import de.greenrobot.event.EventBus;

/**
 * Created by aaron on 16/4/18.
 * 取车页面操作类
 */
public class GetCarPresenter extends BaseFragment{

    @Override
    public View setView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    // 超时handler消息what
    public static final int HANDLER_MSG_WHAT = 1;

    // 倒计时类型，1-免费倒计时, 2-收费倒计时
    public int countDownType = COUNT_DOWN_FREE;
    public static final int COUNT_DOWN_FREE = 1;
    public static final int COUNT_DOWN_PAY = 2;
    // 等待计费时长与等待计费开关、等待计费金额
    private long payWaitRemains;
    private boolean waitPayFlag;
    private double payWaitMoney;
    // 判断是否收费等待倒计时结束
    private boolean isWaitPayFinish = false;
    // 倒计时时间间隔
    public static final long COUNT_DOWN_INTERVAL = 1000;
    // 收费倒计时请求费用间隔
    public static final long COUNT_PAY_INTERVAL = 60 * 1000;


    /**
     * 初始化timeCount倒计时
     * @param orderDetailInfo
     * @param onCancelOrder
     * @param waitPay
     * @param time
     * @param waitFeeText
     */
    public TimeCount initCountDownTime(OrderCommon.OrderDetailInfo orderDetailInfo, OnCancelOrder onCancelOrder, LinearLayout waitPay, TextView time, TextView waitFeeText) {
        // 初始化显示倒计时信息(免费等待事件)
        long waitCarSeconds = orderDetailInfo.getFreeWaitRemains() * 1000L;
        L.i("免费等到时长：" + waitCarSeconds);
        // 等待计费时长(s)与等待计费开关
        payWaitRemains = orderDetailInfo.getPayWaitRemains() * 1000;
        waitPayFlag = orderDetailInfo.getWaitPayFlag();
        L.i("等待计费时长：" + payWaitRemains + "  等待计费开关：" + waitPayFlag);
        TimeCount timeCount = null;

        // 收费等待开关打开
        if (waitPayFlag) {
            // 判断当前免费倒计时是否结束，若未结束，则继续执行免费倒计时逻辑
            if (waitCarSeconds > 0) {
                L.i("收费等待开关打开，当前执行免费倒计时逻辑...");
                waitPay.setVisibility(View.INVISIBLE);
                time.setText(getLongTimeToStr(waitCarSeconds) + "后，开始计费");
                countDownType = COUNT_DOWN_FREE;
                timeCount = new TimeCount(waitCarSeconds, COUNT_DOWN_INTERVAL, onCancelOrder, waitPay, time, waitFeeText);
                timeCount.start();
            }
            // 计费等待时长
            else if (payWaitRemains > 0){
                L.i("收费等待开关打开，当前执行收费倒计时逻辑...");
                waitPay.setVisibility(View.VISIBLE);
                time.setText("剩余取车时间:" + getLongTimeToStr(payWaitRemains));
                countDownType = COUNT_DOWN_PAY;
                timeCount = new TimeCount(payWaitRemains, COUNT_DOWN_INTERVAL, onCancelOrder, waitPay, time, waitFeeText);
                timeCount.start();
                // 收费等待倒计时结束，更新当前费用
                loopGetPayWaitMoney(waitFeeText, onCancelOrder);
            }
            // 取消订单
            else {
                L.i("收费等待开关打开，当前执行取消订单逻辑...");
                countDownType = COUNT_DOWN_PAY;
                waitPay.setVisibility(View.INVISIBLE);
                time.setText("剩余取车时间:00分00秒");

                // 若当前用户状态为待取车,且用户倒计时直接已经结束,则先拉去用户超时费用在弹窗超时弹窗
                isWaitPayFinish = true;
                loopGetPayWaitMoney(waitFeeText, onCancelOrder);
            }
        }
        // 收费倒计时关闭
        else {
            // 判断当前免费倒计时是否结束，若未结束，则继续执行免费倒计时逻辑
            if (waitCarSeconds > 0) {
                L.i("收费等待开关关闭，当前执行免费倒计时逻辑...");
                waitPay.setVisibility(View.INVISIBLE);
                time.setText("剩余取车时间:" + getLongTimeToStr(waitCarSeconds));
                countDownType = COUNT_DOWN_FREE;
                timeCount = new TimeCount(waitCarSeconds , COUNT_DOWN_INTERVAL, onCancelOrder, waitPay, time, waitFeeText);
                timeCount.start();
            } else {
                countDownType = COUNT_DOWN_FREE;
                onCancelOrder.oncancelOrder();
            }
        }

        return timeCount;
    }




    /**
     *  定义一个倒计时的内部类
     */
    class TimeCount extends CountDownTimer {
        LinearLayout waitPay = null;
        TextView time = null;
        TextView waitFeeText = null;
        OnCancelOrder onCancelOrder = null;
        public TimeCount(long millisInFuture, long countDownInterval, OnCancelOrder onCancelOrder, LinearLayout waitPay, TextView time, TextView waitFeeText) {
            // 参数依次为总时长,和计时的时间间隔
            super(millisInFuture, countDownInterval);
            this.onCancelOrder = onCancelOrder;
            this.waitPay = waitPay;
            this.time = time;
            this.waitFeeText = waitFeeText;
        }

        /**
         * 计时完毕时触发
         */
        @Override
        public void onFinish() {
            L.i("执行timeCount.onFinish()...");
            // 等待收费打开
            if (waitPayFlag) {
                // 免费等待倒计时
                if (countDownType == COUNT_DOWN_FREE) {
                    L.i("免费等待取车倒计时结束，开始执行收费等待倒计时...");
                    time.setText("00分00秒后，开始计费");
                    // 开始执行计费倒计时
                    changeFreeToPayCountDown(onCancelOrder, this, waitPay, time, waitFeeText);
                }
                // 收费等待倒计时
                else if (countDownType == COUNT_DOWN_PAY) {
                    L.i("收费倒计时结束，请求当前费用，并执行取消订单操作...");
                    waitPay.setVisibility(View.VISIBLE);
                    time.setText("剩余取车时间:00分00秒");
                    isWaitPayFinish = true;
                    // 收费等待倒计时结束，更新当前费用
                    loopGetPayWaitMoney(waitFeeText, onCancelOrder);
                }
            }
            // 等待收费关闭
            else {
                // 免费等待倒计时
                if (countDownType == COUNT_DOWN_FREE) {
                    L.i("免费倒计时结束，执行取消订单操作...");
                    waitPay.setVisibility(View.INVISIBLE);
                    time.setText("剩余取车时间:00分00秒");
                    // 请求网络，取消订单
                    onCancelOrder.oncancelOrder();
                }
            }

        }

        @Override
        public void onTick(long millisUntilFinished) {
            // 等待收费打开
            if (waitPayFlag) {
                // 免费等待倒计时
                if (countDownType == COUNT_DOWN_FREE) {
                    time.setText(getLongTimeToStr(millisUntilFinished) + "后，开始计费");
                    if (waitPay.getVisibility() ==View.VISIBLE) {
                        waitPay.setVisibility(View.INVISIBLE);
                    }
                }
                // 收费等待倒计时
                else if (countDownType == COUNT_DOWN_PAY) {
                    time.setText("剩余取车时间:" + getLongTimeToStr(millisUntilFinished));
                    if (waitPay.getVisibility() ==View.INVISIBLE) {
                        waitPay.setVisibility(View.VISIBLE);
                    }
                    // 轮训请求服务器，获取当前费用
                    if (millisUntilFinished % COUNT_PAY_INTERVAL < 1000) {
                        // L.i("执行onTick()....,millisUntilFinished:" + millisUntilFinished);
                        loopGetPayWaitMoney(waitFeeText, onCancelOrder);
                    }
                }
            }
            // 等待收费关闭
            else {
                // 免费等待倒计时
                if (countDownType == COUNT_DOWN_FREE) {
                    time.setText("剩余取车时间:" + getLongTimeToStr(millisUntilFinished));
                }
            }
        }
    }

    /**
     * 免费倒计时结束，开始执行计费倒计时
     */
    private void changeFreeToPayCountDown(OnCancelOrder onCancelOrder, TimeCount timeCount, LinearLayout waitPay, TextView time, TextView waitFeeText) {
        if (payWaitRemains > 0){
            waitPay.setVisibility(View.VISIBLE);
            time.setText("剩余取车时间:" + getLongTimeToStr(payWaitRemains));
            timeCount = new TimeCount(payWaitRemains, COUNT_DOWN_INTERVAL, onCancelOrder, waitPay, time, waitFeeText);
            timeCount.start();
            countDownType = COUNT_DOWN_PAY;

            // 更新timeCount对象
            EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_UPDATE_TIMECOUNT, timeCount));
            // 轮训，更新当前费用
            loopGetPayWaitMoney(waitFeeText, onCancelOrder);
        }
    }

    /**
     * 轮训，请求网络获取等待取车超时金额
     */
    private void loopGetPayWaitMoney(final TextView waitFeeText, final OnCancelOrder onCancelOrder) {
        OrderInterface.QueryOnWaitOrderInfo.Request.Builder request = OrderInterface.QueryOnWaitOrderInfo.Request.newBuilder();
        String orderId = Config.getOrderId(mContext);
        if (!TextUtils.isEmpty(orderId)) {
            request.setOrderId(orderId);
        }
        if (TextUtils.isEmpty(Config.cityCode)) {
            Config.cityCode = "010";
        }
        request.setCityCode(Config.cityCode);
        NetworkTask task = new NetworkTask(Cmd.CmdCode.QueryOnWaitOrderInfo_NL_VALUE);
        task.setTag("QueryOnWaitOrderInfo");
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    showResponseCommonMsg(responseData.getResponseCommonMsg());
                    try {
                        OrderInterface.QueryOnWaitOrderInfo.Response response = OrderInterface.QueryOnWaitOrderInfo.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            // 等待支付费用
                            String waitFee = response.getWaitFee();
                            if (!TextUtils.isEmpty(waitFee)) {
                                payWaitMoney = Double.parseDouble(waitFee);
                            }
                            waitFeeText.setText("" + getTwoSN(payWaitMoney));
                        }
                    } catch (InvalidProtocolBufferException e) {
                    } finally {
                        if (isWaitPayFinish) {
                            onCancelOrder.oncancelOrder();
                        }
                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                if (isWaitPayFinish) {
                    onCancelOrder.oncancelOrder();
                }
            }

            @Override
            public void networkFinish() {
            }
        });
    }

    /**
     * 显示超时弹窗
     * @param mainActivity
     */
    public void showOverTimeDialog(MainActivity mainActivity, CancelOrderDialog mCancelOrderDialog, Dialog openDoorDialog) {
        L.i("开始执行超时显示弹窗......   countDownType:" + countDownType);
        String showOverTimeString = getShowOverTimeString();

        // 判断当前是否有运营弹窗
        if (mCancelOrderDialog != null) {
            mCancelOrderDialog.dismissCancelOrderDialog();
        }
        // 判断开车门弹窗是否打开
        if (openDoorDialog != null && openDoorDialog.isShowing()) {
            openDoorDialog.dismiss();
        }
        // 若当前为免费等待倒计时，则不显示超时费用，否则显示超时费用
        if (countDownType == COUNT_DOWN_PAY) {
            // 发送通知，当前为支付等待
            EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_UPDATE_WAIT_TYPE, COUNT_DOWN_PAY));
        } else {
            // 发送通知，当前为免费等待
            EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_UPDATE_WAIT_TYPE, COUNT_DOWN_FREE));
        }
        Config.showTiplDialog(mContext, null, showOverTimeString, "我知道了", mainActivity.cancelOrderListener);
    }

    /**
     * 获取超时费用
     * @return
     */
    public String getShowOverTimeString() {
        StringBuffer sb = new StringBuffer("由于您未在规定时间内取车，行程已取消。");
        // 若当前为免费等待倒计时，则不显示超时费用，否则显示超时费用
        if (countDownType == COUNT_DOWN_PAY) {
            sb.append("\n产生取车超时费用¥");
            sb.append(getTwoSN(payWaitMoney));
        }

        return sb.toString();
    }

    /**
     * 取消订单回调函数
     */
    interface OnCancelOrder {
        public void oncancelOrder();
    }


    /**
     * double类型数据保留两位小时
     * @param value
     * @return
     */
    public String getTwoSN(double value) {
        return String.format("%.2f", value);
    }






    // ####################  异步操作超时相关逻辑 ############################
    /**
     * 判断异步操作是否超时
     */
    public Thread overTimeThread = null;
    /**
     * 开关车门寻车等异步操作是否超时
     */
    public void isOverTime() {
        Config.timeout = true;
        overTimeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                L.i("当前时间：" + Calendar.getInstance().getTime());
                SystemClock.sleep(Config.timeouttime);

                // 如果已经超时
                if (Config.timeout && Config.loadingDialog != null && Config.loadingDialog.isShowing()) {
                    L.i("超时30秒发送handler消息");
                    handler.sendEmptyMessage(HANDLER_MSG_WHAT);
                }
            }
        });
        overTimeThread.start();
    }

    /**
     * 开关车门寻车，等异步操作是否超时
     */
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            L.i("重新请求服务器获取消息");
            // 轮询去掉，超时后自动重新拉取，不走轮询
            MsgHandler.sendLoopRequest(mContext);
        }
    };


    /**
     * 将Long行数据转换为字符串
     * @param time
     * @return
     */
    private String getLongTimeToStr(long time) {
        int hour = (int)(time / 1000 / 60 / 60);
        int minute = (int)(time / 1000 / 60 % 60);
        int second = (int)(time / 1000 % 60);
        StringBuffer sb = new StringBuffer();

        // 显示小时,若为0小时,则不再显示小时
        if (hour > 0) {
            if (hour < 10) {
                sb.append("0");
            }
            sb.append(hour).append("小时");
        }
        if (minute < 10) {
            sb.append("0").append(minute).append("分");
        } else {
            sb.append(minute).append("分");
        }
        if (second < 10) {
            sb.append("0").append(second).append("秒");
        } else {
            sb.append(second).append("秒");
        }

        L.i("GetCarPresenter:" + sb.toString());
        return sb.toString();
    }

}
