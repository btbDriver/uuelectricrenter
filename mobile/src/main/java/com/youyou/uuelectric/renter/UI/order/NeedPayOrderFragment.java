package com.youyou.uuelectric.renter.UI.order;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rey.material.widget.Button;
import com.rey.material.widget.RadioButton;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.base.common.UuCommon;
import com.uu.facade.order.pb.bean.OrderInterface;
import com.uu.facade.order.pb.common.OrderCommon;
import com.uu.facade.pay.pb.bean.PayCommon;
import com.uu.facade.pay.pb.iface.PayInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Network.listen.OnClickFastListener;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseFragment;
import com.youyou.uuelectric.renter.UI.main.MainLoopActivity;
import com.youyou.uuelectric.renter.UI.pay.BasePayFragmentUtils;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.DisplayUtil;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.view.DashedLine;
import com.youyou.uuelectric.renter.Utils.view.RippleView;
import com.youyou.uuelectric.renter.Utils.view.TipRecorderLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.grantland.widget.AutofitTextView;

/**
 * User: qing
 * Date: 2015/9/12 10:07
 * Desc: 待支付订单详情页面
 */
public class NeedPayOrderFragment extends BaseFragment {

    @InjectView(R.id.car_img)
    NetworkImageView mCarImg;
    @InjectView(R.id.car_name)
    TextView mCarName;
    @InjectView(R.id.car_number)
    TextView mCarNumber;
    @InjectView(R.id.car_location)
    AutofitTextView mCarLocation;
    @InjectView(R.id.total_cost)
    TextView mTotalCost;
    @InjectView(R.id.ll_cost_detail)
    LinearLayout mLlCostDetail;
    @InjectView(R.id.ll_activity_detail)
    LinearLayout mLlActivityDetail;
    @InjectView(R.id.iv_next)
    ImageView mIvNext;
    @InjectView(R.id.tv_detail_coupon_price)
    TextView mTvDetailCouponPrice;
    @InjectView(R.id.rv_coupon)
    RippleView mRvCoupon;
    @InjectView(R.id.tv_detail_balance_price)
    TextView mTvDetailBalancePrice;
    @InjectView(R.id.tv_detail_need_title)
    TextView mTvDetailNeedTitle;
    @InjectView(R.id.tv_detail_need_price)
    TextView mTvDetailNeedPrice;
    @InjectView(R.id.ll_pay_container)
    LinearLayout mLlPayContainer;
    @InjectView(R.id.tv_center)
    TextView mTvCenter;
    @InjectView(R.id.ll_record_container)
    LinearLayout mLlRecordContainer;
    @InjectView(R.id.tv_getcar_address)
    TextView mTvGetcarAddress;
    @InjectView(R.id.tv_return_address)
    TextView mTvReturnAddress;
    @InjectView(R.id.ll_address_container)
    LinearLayout mLlAddressContainer;
    @InjectView(R.id.btn_confirm)
    Button mBtnConfirm;
    @InjectView(R.id.dl_detail)
    DashedLine mDlDetail;

    /**
     * 选择优惠券code
     */
    private static final int SELECT_COUPON = 10;

    /**
     * 当前选择的支付方式
     */
    private int currentPayType;
    /**
     * 优惠券ID
     */
    private int couponId;
    /**
     * 所有的支付方式RadioButton
     */
    private List<RadioButton> radioButtonList = new ArrayList<>();
    /**
     * 网络异常提示信息
     */
    private String netWorkErrorTip;
    /**
     * 是否第一次请求接口
     */
    private boolean isFirstRequest = true;

    private BasePayFragmentUtils payFragmentUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        payFragmentUtils = new BasePayFragmentUtils(this, BasePayFragmentUtils.ORDER_TYPE_COMMON);
    }

    @Nullable
    @Override
    public View setView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_need_pay_order, null);
        ButterKnife.inject(this, rootView);

        netWorkErrorTip = getResources().getString(R.string.network_error_tip);

        mProgressLayout.showLoading();
        initView();
        isFirstRequest = true;
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {

        if (Config.isNetworkConnected(mContext)) {
            if (!mProgressLayout.isLoading()) {
                showProgress(false);
            }
            OrderInterface.QueryUnderWayOrder.Request.Builder builder = OrderInterface.QueryUnderWayOrder.Request.newBuilder();
            String orderId = Config.getOrderId(mContext);
            if (!TextUtils.isEmpty(orderId)) {
                builder.setOrderId(orderId);
            }
            // 如果是第一次进入，不传递优惠券Id
            if (isFirstRequest) {
                builder.setCouponId("");
            } else {
                builder.setCouponId("" + couponId);
            }
            NetworkTask task = new NetworkTask(Cmd.CmdCode.QueryUnderWayOrderInfo_NL_VALUE);
            task.setBusiData(builder.build().toByteArray());
            NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
                @Override
                public void onSuccessResponse(UUResponseData responseData) {
                    if (responseData.getRet() == 0) {
                        try {
                            showResponseCommonMsg(responseData.getResponseCommonMsg());
                            OrderInterface.QueryUnderWayOrder.Response response = OrderInterface.QueryUnderWayOrder.Response.parseFrom(responseData.getBusiData());

                            if (response.getRet() == 0) {

                                if (!isAdded()) {
                                    return;
                                }

                                L.i("订单信息请求成功...");
                                OrderCommon.UnderWayOrderInfo underWayOrderInfo = response.getUnderWayOrderInfo();

                                // 获取默认优惠券ID
                                couponId = underWayOrderInfo.getCouponId();

                                // 获取订单详细信息
                                OrderCommon.OrderDetailInfo orderDetailInfo = underWayOrderInfo.getOrderDetailInfo();
                                updateView4OrderDetailInfo(orderDetailInfo);
                                // 检测是否跳转行程详情
                                goToTripDetail(orderDetailInfo);

                                // 费用合计
                                mTotalCost.setText(underWayOrderInfo.getOrderPayCost());

                                // 获取费用项
                                List<OrderCommon.OrderAccountItem> orderAccountItemListList = underWayOrderInfo.getOrderAccountItemListList();
                                updateView4OrderAccountItem(orderAccountItemListList);

                                // 优惠券抵扣默认值
                                if (!"无可用优惠券".equals(underWayOrderInfo.getCouponsCost())) {
                                    mTvDetailCouponPrice.setTextColor(getResources().getColor(R.color.c1));
                                }
                                mTvDetailCouponPrice.setText(underWayOrderInfo.getCouponsCost());
                                updateView4Coupon(underWayOrderInfo.getIsDisplayCopun() == 0);

                                // 还需支付
                                mTvDetailNeedPrice.setText(underWayOrderInfo.getOrderPayCost());

                                // 余额支付
                                mTvDetailBalancePrice.setText(underWayOrderInfo.getBalance());

                                // 默认的支付方式
                                UuCommon.ThirdPayType defaultThirdPayType = underWayOrderInfo.getDefaultThirdPayType();
                                // 第三方支付列表
                                List<UuCommon.ThirdPayType> thirdPayTypeListList = underWayOrderInfo.getThirdPayTypeListList();

                                // 添加支付项
                                updateView4ThirdPayType(thirdPayTypeListList, defaultThirdPayType);

                                // 预约历史信息列表
                                List<OrderCommon.OrderStatusHistory> orderStatusHistoryLIstList = underWayOrderInfo.getOrderStatusHistoryLIstList();
                                updateView4OrderStatusHistory(orderStatusHistoryLIstList);

                                mProgressLayout.showContent();

                                isFirstRequest = false;

                            } else {
                                mProgressLayout.showError(errorListener);
                            }

                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                            mProgressLayout.showError(errorListener);
                        }
                    } else {
                        mProgressLayout.showError(errorListener);
                    }
                    dismissProgress();
                }

                @Override
                public void onError(VolleyError errorResponse) {
                    mProgressLayout.showError(errorListener);
                    dismissProgress();
                }

                @Override
                public void networkFinish() {
                    dismissProgress();
                }
            });

        } else {
            mProgressLayout.showError(errorListener);
        }
    }


    /**
     * 检测是否跳转行程详情
     * @param orderDetailInfo
     */
    private void goToTripDetail(OrderCommon.OrderDetailInfo orderDetailInfo) {

        if (orderDetailInfo.getStatus() != OrderCommon.OrderFormStatus.wait_pay) {
            // 支付返回，主界面可以开始Loop
            MainLoopActivity.isCanLoop = true;
            // 设置支付动作已完成
            BasePayFragmentUtils.payActionIsOver = true;
            // 支付成功，跳转到支付成功界面
            Intent intent = new Intent(mContext, TripOrderDetailActivity.class);
            intent.putExtra(IntentConfig.ORDER_ID, Config.getOrderId(mContext));
            intent.putExtra(IntentConfig.KEY_PAGE_TYPE, TripOrderDetailActivity.PAGE_TYPE_TRIP_DETAIL);
            intent.putExtra(IntentConfig.IF_REDBAG_SHOW,"toShowRedBag");
            mContext.startActivity(intent);
        } else {
            payFragmentUtils.onPayResume();
        }

    }

    /**
     * 优惠券是否可点
     *
     * @param isDisplayCoupon 是否可点
     */
    private void updateView4Coupon(boolean isDisplayCoupon) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTvDetailCouponPrice.getLayoutParams();
        // 优惠券是否可以点击选择
        if (isDisplayCoupon) {
            mRvCoupon.setEnabled(true);
            mIvNext.setVisibility(View.VISIBLE);
            params.rightMargin = DisplayUtil.dip2px(mContext, 13);
        } else {
            mRvCoupon.setEnabled(true);
            mIvNext.setVisibility(View.VISIBLE);
            params.rightMargin = DisplayUtil.dip2px(mContext, 13);
        }
    }

    /**
     * 更新包含订单详细信息中内容的数据到界面展示
     *
     * @param orderDetailInfo
     */
    private void updateView4OrderDetailInfo(OrderCommon.OrderDetailInfo orderDetailInfo) {
        if (orderDetailInfo != null) {

            // 车辆图片
            UUApp.getInstance().display(orderDetailInfo.getCarImgUrl(), mCarImg, R.mipmap.ic_car_unload_details);
            // 品牌型号
            mCarName.setText(orderDetailInfo.getBrandName() + orderDetailInfo.getModelName());
            // 车牌
            mCarNumber.setText(orderDetailInfo.getPlateNumbe());

            // 车位地址
            OrderCommon.ParkingInfo getParkingInfo = orderDetailInfo.getGetParkingInfo();
            mCarLocation.setText(getParkingInfo.getCarParkingName());

            // 取车地点
            mTvGetcarAddress.setText("取车地点：" + getParkingInfo.getCarParkingName());

            // 还车地点
            OrderCommon.ParkingInfo backParkingInfo = orderDetailInfo.getBackParkingInfo();
            mTvReturnAddress.setText("还车地点：" + backParkingInfo.getCarParkingName());

        }
    }

    /**
     * 显示服务端下发的费用项和活动项
     *
     * @param orderAccountItemListList
     */
    private void updateView4OrderAccountItem(List<OrderCommon.OrderAccountItem> orderAccountItemListList) {
        mLlActivityDetail.removeAllViews();
        mLlCostDetail.removeAllViews();
        boolean hasActivityItem = false;
        if (orderAccountItemListList != null && orderAccountItemListList.size() > 0) {
            for (OrderCommon.OrderAccountItem item : orderAccountItemListList) {
                // 订单费用项
                if (item.getOrderFeeType() == OrderCommon.OrderFeeType.order) {
                    mLlCostDetail.addView(createItemView(R.layout.pay_order_item, item));

                    // 添加订单费用项中包含的子项
                    List<OrderCommon.OrderAccountItem> itemListList = item.getItemListList();
                    if (itemListList != null && itemListList.size() > 0) {
                        for (OrderCommon.OrderAccountItem subItem : itemListList) {
                            mLlCostDetail.addView(createItemView(R.layout.pay_order_sub_item, subItem));
                        }
                    }
                }
                // 活动项
                else if (item.getOrderFeeType() == OrderCommon.OrderFeeType.activity) {
                    mLlActivityDetail.addView(createItemView(R.layout.pay_order_activity_detail_item, item), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(mContext, 44)));
                    hasActivityItem = true;
                }
            }
            if (!hasActivityItem) {
                mDlDetail.setVisibility(View.GONE);
            } else {
                mDlDetail.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 创建ItemView
     * @param itemViewId
     * @param item
     * @return
     */
    private View createItemView(int itemViewId, OrderCommon.OrderAccountItem item) {
        View view = LayoutInflater.from(mContext).inflate(itemViewId, null);
        TextView key = (TextView) view.findViewById(R.id.tv_key);
        TextView value = (TextView) view.findViewById(R.id.tv_value);
        key.setText(item.getItemName());
        value.setText(item.getAmount());
        return view;
    }

    /**
     * 显示服务端下发的支付方式
     *
     * @param thirdPayTypeListList
     * @param defaultThirdPayType
     */
    private void updateView4ThirdPayType(List<UuCommon.ThirdPayType> thirdPayTypeListList, UuCommon.ThirdPayType defaultThirdPayType) {
        mLlPayContainer.removeAllViews();
        if (defaultThirdPayType != null && defaultThirdPayType.getNumber() != UuCommon.ThirdPayType.UNSET_VALUE) {
            addPayType(defaultThirdPayType, true);
            if (thirdPayTypeListList != null && thirdPayTypeListList.size() > 1) {
                addLine();
            }
        }
        if (thirdPayTypeListList != null && thirdPayTypeListList.size() > 0) {
            int size = thirdPayTypeListList.size();
            for (int i = 0; i < size; i++) {
                UuCommon.ThirdPayType payType = thirdPayTypeListList.get(i);
                if (payType.getNumber() != defaultThirdPayType.getNumber()) {
                    addPayType(payType, false);
                }
                if (i < size - 1) {
                    addLine();
                }
            }
            // 去除最后一个线
            int count = mLlPayContainer.getChildCount();
            View child = mLlPayContainer.getChildAt(count - 1);
            if ("line".equals(child.getTag())) {
                child.setVisibility(View.GONE);
            }
        } else {
            currentPayType = -1;
        }

    }

    /**
     * 添加分隔线
     */
    private void addLine() {
        View line = new View(mContext);
        line.setTag("line");
        LinearLayout.LayoutParams lineParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        lineParam.leftMargin = (int) getResources().getDimension(R.dimen.s4);
        line.setBackgroundColor(getResources().getColor(R.color.c5));
        mLlPayContainer.addView(line, lineParam);
    }

    /**
     * 添加支付方式的Item
     *
     * @param payType
     * @param isDefault 是否是默认选中
     */
    private void addPayType(UuCommon.ThirdPayType payType, boolean isDefault) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.pay_radio_item, null);
        ImageView icon = (ImageView) view.findViewById(R.id.iv_pay_icon);
        TextView name = (TextView) view.findViewById(R.id.tv_pay_name);
        RadioButton rb = (RadioButton) view.findViewById(R.id.radio_button);
        rb.setTag(payType.getNumber());
        rb.setOnCheckedChangeListener(listener);
        rb.setChecked(isDefault);

        if (payType.getNumber() == UuCommon.ThirdPayType.ALIPAY_VALUE) {
            name.setText("支付宝");
            icon.setImageResource(R.mipmap.ic_alipay_payment);
        } else if (payType.getNumber() == UuCommon.ThirdPayType.WECHAT_VALUE) {
            name.setText("微信");
            icon.setImageResource(R.mipmap.ic_wechat_payment);
        }
        radioButtonList.add(rb);
        view.setOnClickListener(rbClickListener);
        mLlPayContainer.addView(view);
    }

    /**
     * 显示预约历史信息
     *
     * @param orderStatusHistoryLIstList
     */
    private void updateView4OrderStatusHistory(List<OrderCommon.OrderStatusHistory> orderStatusHistoryLIstList) {
        mLlRecordContainer.removeAllViews();
        if (orderStatusHistoryLIstList != null && orderStatusHistoryLIstList.size() > 0) {
            int size = orderStatusHistoryLIstList.size();
            for (int i = 0; i < size; i++) {
                OrderCommon.OrderStatusHistory orderStatusHistory = orderStatusHistoryLIstList.get(i);
                // 初始化行程记录
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.tip_item_height));
                TipRecorderLayout tp = new TipRecorderLayout(mContext);
                long intTime = orderStatusHistory.getCreateTime() * 1000L;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String strTime = sdf.format(new Date(intTime));
                if (i == 0) {
                    tp.setTypeAndInfo(TipRecorderLayout.START_TYPE, strTime, orderStatusHistory.getCurStatus());
                } else if (i > 0 && i < size - 1) {
                    tp.setTypeAndInfo(TipRecorderLayout.MIDDLE_TYPE, strTime, orderStatusHistory.getCurStatus());
                } else {
                    tp.setTypeAndInfo(TipRecorderLayout.END_TYPE, strTime, orderStatusHistory.getCurStatus());
                }
                mLlRecordContainer.addView(tp, params);
            }
        }
    }


    /**
     * 加载出错时，重试按钮点击逻辑
     */
    private View.OnClickListener errorListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mProgressLayout.showLoading();
            loadData();
        }
    };

    /**
     * 支付方式Radio选择监听
     */
    private CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                Integer tag = (Integer) buttonView.getTag();
                currentPayType = tag;
                for (RadioButton rb : radioButtonList) {
                    rb.setChecked(rb == buttonView);
                }
            }
        }

    };

    /**
     * 支付方式布局点击监听
     */
    private View.OnClickListener rbClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RadioButton rb = (RadioButton) view.findViewById(R.id.radio_button);
            listener.onCheckedChanged(rb, true);
        }
    };

    /**
     * 确认支付按钮点击逻辑
     */
    public void confirmClick() {

        if (Config.isNetworkConnected(mContext)) {
            showProgress(false);

            PayInterface.Pay.Request.Builder builder = PayInterface.Pay.Request.newBuilder();

            String orderId = Config.getOrderId(mContext);
            if (!TextUtils.isEmpty(orderId)) {
                builder.setOrderId(orderId);
            }
            if (currentPayType != -1) {
                // 设置支付方式
                if (currentPayType == UuCommon.ThirdPayType.ALIPAY_VALUE) {
                    builder.setPayType(UuCommon.ThirdPayType.ALIPAY);
                } else if (currentPayType == UuCommon.ThirdPayType.WECHAT_VALUE) {
                    if (!Config.isWXAppInstalled(mContext)) {
                        Config.showTiplDialog(mContext, "温馨提示", "您未安装微信，不能进行微信支付，请选择其他支付方式。", "知道了", null);
                        dismissProgress();
                        return;
                    }
                    builder.setPayType(UuCommon.ThirdPayType.WECHAT);
                }
            }
            NetworkTask task = new NetworkTask(Cmd.CmdCode.Pay_NL_VALUE);
            task.setBusiData(builder.build().toByteArray());
            NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {

                @Override
                public void onSuccessResponse(UUResponseData responseData) {

                    if (responseData.getRet() == 0) {

                        try {
                            showResponseCommonMsg(responseData.getResponseCommonMsg());
                            PayInterface.Pay.Response response = PayInterface.Pay.Response.parseFrom(responseData.getBusiData());
                            int ret = response.getRet();
                            // 0：成功(余额扣款)
                            if (ret == 0) {

                                // 余额支付成功，去支付成功页面
                                Intent intent = new Intent(mContext, TripOrderDetailActivity.class);
                                intent.putExtra(IntentConfig.ORDER_ID, Config.getOrderId(mContext));
                                intent.putExtra(IntentConfig.KEY_PAGE_TYPE, TripOrderDetailActivity.PAGE_TYPE_TRIP_DETAIL);
                                intent.putExtra(IntentConfig.IF_REDBAG_SHOW,"toShowRedBag");
                                mContext.startActivity(intent);

                            }
                            // 1 : 成功（需根据payInfo跳转第三方支付）
                            else if (ret == 1) {
                                PayCommon.PayInfo payInfo = response.getPayInfo();
                                if (payInfo != null) {
                                    List<String> orderList = new ArrayList<String>();
                                    orderList.add(Config.getOrderId(mContext));
                                    // 执行支付操作
                                    payFragmentUtils.queryPayOrderInfo(payInfo, orderList);
                                }
                            }
                            // 失败
                            else if (ret == -1) {
                                showSnackBarMsg("支付失败，请重试!");
                            }
                            // 需充值（request中payType为空并且余额不足支付，会返回此状态）
                            else if (ret == -2) {
                                // 存在余额不足的情况，需要重新加载订单详情，选择第三方支付方式后，重新提交支付
                                Config.showTiplDialog(mContext, null, "由于您的余额减少，本次订单支付金额不足，请继续支付", "继续支付", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        showProgress(false);
                                        loadData();
                                    }
                                });

                            }
                            //-3：订单状态有冲突，需要刷新页面，以便拿到订单的最新状态；
                            else if (ret == -3) {

                                showProgress(false);
                                loadData();
                            }
                            // -4: 为避免重复付款，请稍候支付
                            else if (ret == -4) {
                                // 通过commonMsg提示
                                // Config.showToast(mContext, "为避免重复付款，请稍候支付");
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onError(VolleyError errorResponse) {
                    showSnackBarMsg(netWorkErrorTip);
                }

                @Override
                public void networkFinish() {
                    dismissProgress();
                }
            });

        } else {
            showSnackBarMsg(netWorkErrorTip);
        }
    }


    private void initView() {

        mRvCoupon.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                if (!Config.isNetworkConnected(mContext)) {
                    showNetworkErrorSnackBarMsg();
                    return;
                }
                Intent intent = new Intent(mContext, FavourSelectActivity.class);
                intent.putExtra(IntentConfig.ORDER_ID, Config.getOrderId(mContext));
                intent.putExtra(IntentConfig.COUPON_ID, couponId);
                startActivityForResult(intent, SELECT_COUPON);
            }
        });

        // 屏蔽确认支付多次点击
        mBtnConfirm.setOnClickListener(new OnClickFastListener() {
            @Override
            public void onFastClick(View v) {
                confirmClick();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RESULT_OK==-1
        if (resultCode == -1) {
            if (requestCode == SELECT_COUPON) {
                if (data != null) {
                    int result = data.getIntExtra(IntentConfig.COUPON_ID, 0);
                    couponId = result;
                    showProgress(false);
                    // 重新请求数据
                    loadData();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
