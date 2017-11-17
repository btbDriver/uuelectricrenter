package com.youyou.uuelectric.renter.UI.order;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mingle.sweetpick.CustomDelegate;
import com.mingle.sweetpick.DimEffect;
import com.mingle.sweetpick.SweetSheet;
import com.umeng.analytics.MobclickAgent;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.base.common.UuCommon;
import com.uu.facade.order.pb.bean.OrderInterface;
import com.uu.facade.order.pb.common.OrderCommon;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Network.listen.OnClickFastListener;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.UI.main.MainActivity;
import com.youyou.uuelectric.renter.UI.main.rentcar.CommentCarDialog;
import com.youyou.uuelectric.renter.UI.web.H5Activity;
import com.youyou.uuelectric.renter.UI.web.H5Constant;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.DisplayUtil;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.UMCountConstant;
import com.youyou.uuelectric.renter.Utils.share.ShareInfo;
import com.youyou.uuelectric.renter.Utils.share.ShareSnsUtils;
import com.youyou.uuelectric.renter.Utils.view.DashedLine;
import com.youyou.uuelectric.renter.Utils.view.RippleView;
import com.youyou.uuelectric.renter.Utils.view.TipRecorderLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import me.grantland.widget.AutofitTextView;

/**
 * 行程详情页和订单详情页，两个页面界面一样，所以公用
 */
public class TripOrderDetailActivity extends BaseActivity {

    /**
     * 页面类型：行程详情页(已完成订单)
     */
    public static final int PAGE_TYPE_TRIP_DETAIL = 0;
    /**
     * 页面类型：订单详情页（已取消订单）
     */
    public static final int PAGE_TYPE_ORDER_DETAIL = 1;

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
    @InjectView(R.id.redbox)
    ImageView redBox;
    @InjectView(R.id.rv_car_record)
    RippleView mRvCarRecord;
    @InjectView(R.id.dl_detail)
    DashedLine mDlDetail;
    String shareTitle, shareContent, shareUrl, iconUrl;
    //弹层部分。取消和弹层分享按钮
    View dialogView;
    Button mRedbagCancle, mShareToFriends;
    TextView mRedBagCount;

    @OnClick(R.id.redbox)
    public void redboxClick() {


        dialog.show();
        redBox.setVisibility(View.GONE);

    }

    @InjectView(R.id.rv_car_comment)
    RippleView mRvCarComment;

    /**
     * 当前页面类型
     */
    private int currentPageType = PAGE_TYPE_TRIP_DETAIL;


    /**
     * 订单ID
     */
    private String orderId;
    /**
     * 红包弹层dialog
     */
    private Dialog dialog;
    /**
     * 车况反馈URL
     */
    private UuCommon.WebUrl carConditionListUrl;

    private String toShowRedBag;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_detail_page);
        ButterKnife.inject(this);
        rl = (RelativeLayout) findViewById(R.id.rl);
        mProgressLayout.showLoading();

        initArgs();

        initView();

        initDialogView();

        loadData();
        setupCustomView();

    }

    /**
     * 是否是从行程列表进入
     */
    private boolean isFromTrip = false;
    private boolean isFirstDefaultShow = true;

    private void initArgs() {
        Intent intent = getIntent();
        currentPageType = intent.getIntExtra(IntentConfig.KEY_PAGE_TYPE, PAGE_TYPE_TRIP_DETAIL);
        orderId = intent.getStringExtra(IntentConfig.ORDER_ID);
        isFromTrip = intent.getBooleanExtra(IntentConfig.KEY_FROM_TRIP, false);
        toShowRedBag = intent.getStringExtra(IntentConfig.IF_REDBAG_SHOW);
    }


    /**
     * 红包弹层
     */
    private void initDialogView() {
        // 初始化dialog
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        dialogView = layoutInflater.inflate(R.layout.acivity_redbag_dialog, null);
        dialog = new Dialog(mContext, R.style.edit_AlertDialog_style);
        mShareToFriends = (Button) dialogView.findViewById(R.id.btn_sharetofriends);
        mRedBagCount = (TextView) dialogView.findViewById(R.id.redbag_count);
        mRedbagCancle = (Button) dialogView.findViewById(R.id.btn_redbag_cancle);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && isFirstDefaultShow) {
            dialog.setContentView(dialogView);
            mShareToFriends.setOnClickListener(new OnClickFastListener() {
                @Override
                public void onFastClick(View v) {
                        dialog.dismiss();
                        dialog.setCanceledOnTouchOutside(true);
                        mSweetSheet3.toggle();
                        redBox.setVisibility(View.VISIBLE);
                        MobclickAgent.onEvent(mContext, UMCountConstant.RED_ENVELOPE_DIALOG_SHARE);

                }
            });

            mRedbagCancle.setOnClickListener(new OnClickFastListener() {
                @Override
                public void onFastClick(View v) {
                    dialog.dismiss();
                    redBox.setVisibility(View.VISIBLE);
                    MobclickAgent.onEvent(mContext, UMCountConstant.RED_ENVELOPE_DIALOG_SHARE);
                }
            });
            // 判断是否是支付页面跳转的
            if (toShowRedBag!=null && toShowRedBag.equals("toShowRedBag") ) {
                //此处逻辑放到支付完成后判断。
                //dialog.show();

                redBox.setVisibility(View.GONE);
            } else {
                redBox.setVisibility(View.VISIBLE);
            }
            isFirstDefaultShow = false;
        }
        super.onWindowFocusChanged(hasFocus);
        //对弹层消失监听
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                redBox.setVisibility(View.VISIBLE);
            }
        });
    }



    /**
     * 根据页面类型的不同动态显示布局
     */
    private void initView() {
        setTitle(getResources().getString(R.string.title_activity_trip_detail));
        if (currentPageType == PAGE_TYPE_TRIP_DETAIL) {
            // 产品说去除右侧回首页按钮，只保留左侧返回箭头
           /* if (!isFromTrip) {
                setRightOptBtnInfo(getResources().getString(R.string.opt_menu_back_main), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        onBackPressed();

                    }
                });
            }*/
        } else if (currentPageType == PAGE_TYPE_ORDER_DETAIL) {
            // 隐藏底部取还车布局
            mLlAddressContainer.setVisibility(View.GONE);
        }

        mTotalCost.setTextColor(getResources().getColor(R.color.c3));
        mRvCoupon.setEnabled(false);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mTvDetailCouponPrice.getLayoutParams();
        layoutParams.rightMargin = 0;
        mIvNext.setVisibility(View.GONE);

        // 车况记录点击处理
        mRvCarRecord.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {

                if (carConditionListUrl != null) {
                    Intent intent = new Intent(mContext, H5Activity.class);
                    intent.putExtra(H5Constant.TITLE, carConditionListUrl.getTitle());
                    intent.putExtra(H5Constant.MURL, carConditionListUrl.getUrl());
                    startActivity(intent);
                }

            }
        });

        mRvCarComment.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                CommentCarDialog commentCarDialog = new CommentCarDialog(mContext);
                commentCarDialog.showCommentCarDialog(orderId);
            }
        });

    }

    @Override
    public void onBackPressed() {


        if (mSweetSheet3.isShow()) {
            mSweetSheet3.dismiss();
        } else {
            super.onBackPressed();

            if (!isFromTrip) {
                // 返回首页地图逻辑
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("goto", MainActivity.GOTO_MAP);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);

                Config.clearOrderId(mContext);
                finish();
            }
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home || id == 0) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }


    private void loadData() {

        if (Config.isNetworkConnected(mContext)) {

            if (TextUtils.isEmpty(orderId)) {
                L.d("orderId不能为null...");
                mProgressLayout.showError(errorListener);
                return;
            }
            OrderInterface.QueryUnderWayOrder.Request.Builder builder = OrderInterface.QueryUnderWayOrder.Request.newBuilder();
            builder.setOrderId(orderId);
            NetworkTask task = new NetworkTask(Cmd.CmdCode.QueryUnderWayOrderInfo_NL_VALUE);
            task.setBusiData(builder.build().toByteArray());
            NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
                @Override
                public void onSuccessResponse(UUResponseData responseData) {
                    if (responseData.getRet() == 0) {
                        try {
                            showResponseCommonMsg(responseData.getResponseCommonMsg());
                            OrderInterface.QueryUnderWayOrder.Response response = OrderInterface.QueryUnderWayOrder.Response.parseFrom(responseData.getBusiData());

                            L.i("response.getMsg():" + response.getMsg());
                            if (response.getRet() == 0) {

                                L.i("订单信息请求成功...");
                                OrderCommon.UnderWayOrderInfo underWayOrderInfo = response.getUnderWayOrderInfo();

                                // 获取车况反馈Url
                                carConditionListUrl = underWayOrderInfo.getCarConditionListUrl();

                                // 获取订单详细信息
                                OrderCommon.OrderDetailInfo orderDetailInfo = underWayOrderInfo.getOrderDetailInfo();
                                updateView4OrderDetailInfo(orderDetailInfo);

                                // 费用合计
                                mTotalCost.setText(underWayOrderInfo.getOrderPayCost());

                                // 获取费用项
                                List<OrderCommon.OrderAccountItem> orderAccountItemListList = underWayOrderInfo.getOrderAccountItemListList();
                                updateView4OrderAccountItem(orderAccountItemListList);

                                // 优惠券抵扣默认值
                                mTvDetailCouponPrice.setText(underWayOrderInfo.getCouponsCost());

                                // 红包分发个数


                                // 第三方支付
                                OrderCommon.ThirdPayOrderInfo thirdPayOrderInfo = underWayOrderInfo.getThirdPayOrderInfo();
                                if (thirdPayOrderInfo != null) {
                                    mTvDetailNeedPrice.setText(thirdPayOrderInfo.getAmount());
                                    int type = thirdPayOrderInfo.getThirdPayType().getNumber();
                                    if (type == UuCommon.ThirdPayType.ALIPAY_VALUE) {
                                        mTvDetailNeedTitle.setText(getResources().getString(R.string.pay_type_alipay));
                                    } else if (type == UuCommon.ThirdPayType.WECHAT_VALUE) {
                                        mTvDetailNeedTitle.setText(getResources().getString(R.string.pay_type_wechat));
                                    } else {
                                        mTvDetailNeedTitle.setText("已支付");
                                        mTvDetailNeedPrice.setText(underWayOrderInfo.getOrderPayCost());
                                    }
                                }

                                // 取消支付的界面，底部的费用文案始终显示还需支付
                                if (currentPageType == PAGE_TYPE_ORDER_DETAIL) {
                                    mTvDetailNeedTitle.setText("已支付");
                                    mTvDetailNeedPrice.setText(underWayOrderInfo.getOrderPayCost());
                                }

                                // 余额支付
                                mTvDetailBalancePrice.setText(underWayOrderInfo.getBalance());


                                // 预约历史信息列表
                                List<OrderCommon.OrderStatusHistory> orderStatusHistoryLIstList = underWayOrderInfo.getOrderStatusHistoryLIstList();
                                updateView4OrderStatusHistory(orderStatusHistoryLIstList);

                                //红包相关
                                if (orderDetailInfo.hasIsDisplayWallet() && orderDetailInfo.getIsDisplayWallet() == 1 && orderDetailInfo.getWalletNum() != 0) {
                                    // redBox.setVisibility(View.VISIBLE);
                                    shareContent = orderDetailInfo.getShareContent();
                                    shareTitle = orderDetailInfo.getShareTitle();
                                    shareUrl = orderDetailInfo.getShareUrl();
                                    iconUrl = orderDetailInfo.getIconUrl();
                                    mRedBagCount.setText(""+orderDetailInfo.getWalletNum());


                                    if(toShowRedBag!=null && toShowRedBag.equals("toShowRedBag")) {

                                    dialog.show();
                                    redBox.setVisibility(View.GONE);
                                }



                                    if (orderDetailInfo.getWalletNum() == 0 && orderDetailInfo.getIsDisplayWallet() == 0) {
                                        dialog.dismiss();
                                        redBox.setVisibility(View.GONE);
                                    }



                                } else {
                                    redBox.setVisibility(View.GONE);
                                }
                                mProgressLayout.showContent();

                            } else {
                                mProgressLayout.showError(errorListener);
                            }

                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    } else {
                        mProgressLayout.showError(errorListener);
                    }

                }

                @Override
                public void onError(VolleyError errorResponse) {
                    mProgressLayout.showError(errorListener);
                }

                @Override
                public void networkFinish() {
                }
            });

        } else {
            mProgressLayout.showError(errorListener);
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
     *
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
     * 显示预约历史信息
     *
     * @param orderStatusHistoryLIstList
     */
    private void updateView4OrderStatusHistory(List<OrderCommon.OrderStatusHistory> orderStatusHistoryLIstList) {
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

    private SweetSheet mSweetSheet3;

    private RelativeLayout rl;


    /**
     * 发红包
     */
    private void setupCustomView() {
        rl = (RelativeLayout) findViewById(R.id.rl);
        mSweetSheet3 = new SweetSheet(rl);
        mSweetSheet3.setBackgroundEffect(new DimEffect(0.7f));
        CustomDelegate customDelegate = new CustomDelegate(false,
                CustomDelegate.AnimationType.DuangLayoutAnimation);
        View view = LayoutInflater.from(mContext).inflate(R.layout.coupon_dialog_layout, null, false);
        customDelegate.setCustomView(view);
        customDelegate.setSweetSheetColor(Color.WHITE);
        mSweetSheet3.setDelegate(customDelegate);
        mSweetSheet3.setBackgroundClickEnable(true);
        view.findViewById(R.id.circle_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ShareInfo shareInfo = new ShareInfo();
                shareInfo.setContext(shareContent);
                shareInfo.setShareTitle(shareTitle);
                shareInfo.setShareUrlFriend(shareUrl);
                shareInfo.setImage(iconUrl);
                new ShareSnsUtils(mContext).shareByFriends(shareInfo);
                mSweetSheet3.dismiss();
            }
        });
        view.findViewById(R.id.friend_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareInfo shareInfo = new ShareInfo();
                shareInfo.setContext(shareContent);
                shareInfo.setShareTitle(shareTitle);
                shareInfo.setImage(iconUrl);
                shareInfo.setShareUrlWechart(shareUrl);
                new ShareSnsUtils(mContext).shareByWeixin(shareInfo);
                mSweetSheet3.dismiss();
            }
        });
    }
}
