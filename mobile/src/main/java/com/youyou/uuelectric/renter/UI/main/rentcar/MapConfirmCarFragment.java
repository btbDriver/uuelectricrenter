package com.youyou.uuelectric.renter.UI.main.rentcar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rey.material.widget.Button;
import com.umeng.analytics.MobclickAgent;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.usecar.protobuf.iface.UsecarCommon;
import com.uu.facade.usecar.protobuf.iface.UsercarInterface;
import com.uu.facade.user.protobuf.bean.UserInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Network.listen.OnClickFastListener;
import com.youyou.uuelectric.renter.Network.listen.OnClickLoginedListener;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseFragment;
import com.youyou.uuelectric.renter.UI.license.ValidateLicenseActivity;
import com.youyou.uuelectric.renter.UI.main.MainActivity;
import com.youyou.uuelectric.renter.UI.main.bean.CarBaseBean;
import com.youyou.uuelectric.renter.UI.web.H5Activity;
import com.youyou.uuelectric.renter.UI.web.H5Constant;
import com.youyou.uuelectric.renter.UI.web.url.URLConfig;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.AnimDialogUtils;
import com.youyou.uuelectric.renter.Utils.DisplayUtil;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.Support.LocationListener;
import com.youyou.uuelectric.renter.Utils.UMCountConstant;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;
import com.youyou.uuelectric.renter.Utils.view.UUViewPager;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import me.grantland.widget.AutofitTextView;

/**
 * 地图页底部车辆详情Fragment
 */
public class MapConfirmCarFragment extends BaseFragment {

    @InjectView(R.id.car_root)
    RelativeLayout carRoot;
    @InjectView(R.id.viewPager)
    UUViewPager mViewPager;
    @InjectView(R.id.nocar_root)
    RelativeLayout mNocarRoot;
    @InjectView(R.id.b5_button)
    Button mB5Button;
    @InjectView(R.id.left_page)
    ImageView mLeftPage;
    @InjectView(R.id.right_page)
    ImageView mRightPage;
    @InjectView(R.id.b3_button)
    Button mB3Button;
    View rootView;


    private String dotID = "";
    private int dotCarNumber = 0;
    private List<UsecarCommon.CarBaseInfo> carInfoList = new ArrayList<>();
    private UsecarCommon.CarBaseInfo currentCarBaseInfo;
    private UsercarInterface.RentConfirmDetail.Response response;
    private UsercarInterface.RentConfirm.Response rentConfirmResponse;
    private boolean isDestory = false;
    private AnimDialogUtils dialog;
    /**
     * 访问入口标识   1-立即用车   2-我知道了
     */
    private int accessType;
    /**
     * 公里价（每公里 XX 元） ---- 用来和服务器端的价格进行比对
     */
    private float pricePerKm;
    /**
     * 分钟价 (每分钟 XX 元) ---- 用来和服务器端的价格进行比对
     */
    private float pricePerMinute;


    @OnClick(R.id.left_page)
    public void leftPageClick() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
    }

    @OnClick(R.id.right_page)
    public void rightPageClick() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
    }

    // @OnClick(R.id.left_button)
    public void leftBtnClick() {
        if (response != null && response.getRet() == 0) {
            MobclickAgent.onEvent(mContext, UMCountConstant.COST_ESTIMATE);
            Intent intent = new Intent(mContext, CostAssessActivity.class);
            intent.putExtra(IntentConfig.CAR_NAME, currentCarBaseInfo.getBrand() + currentCarBaseInfo.getModel());
            intent.putExtra(IntentConfig.CAR_NUMBER,currentCarBaseInfo.getCarLicense());
            intent.putExtra(IntentConfig.CAR_IMGURL,currentCarBaseInfo.getCarImgUrl());
            intent.putExtra(IntentConfig.DOT_ID, dotID);
            intent.putExtra(IntentConfig.CAR_MILEAGE,currentCarBaseInfo.getEndurance()+"");
            intent.putExtra(IntentConfig.KEY_RENT_CONFIRM_DETAIL, CarBaseBean.getBeanFromInfo(currentCarBaseInfo));
            intent.putExtra(IntentConfig.KEY_DOTLAT, response.getDotLat());
            intent.putExtra(IntentConfig.KEY_DOTLON, response.getDotLon());
            intent.putExtra(IntentConfig.KEY_DOTNAME, response.getDotName());
            intent.putExtra(IntentConfig.KEY_ISALLDOTSA2B, response.getIsAllDotsA2B());//传递网点是否可以任意a2b
            // 传递网点地址
            intent.putExtra(IntentConfig.KEY_DOTADDR, response.getDotDesc());
            startActivity(intent);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            dotID = bundle.getString(IntentConfig.DOT_ID);
            dotCarNumber = bundle.getInt(IntentConfig.KEY_DOT_CAR_NUMBER, 0);
        }
    }


    @Override
    public View setView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isDestory = false;
        mProgressLayout.setCornerResId(R.drawable.map_confirm_bg);
        rootView = inflater.inflate(R.layout.map_confirm_car, null);
        ButterKnife.inject(this, rootView);
        mB3Button.setText(getString(R.string.rent_car));
        // 屏蔽多次点击事件
        mB3Button.setOnClickListener(new OnClickFastListener() {
            @Override
            public void onFastClick(View v) {
                accessType = 1;
                rentCar();
            }
        });
        // 屏蔽多次点击事件
        ((Button) rootView.findViewById(R.id.left_button)).setOnClickListener(new OnClickFastListener() {
            @Override
            public void onFastClick(View v) {
                leftBtnClick();
            }
        });

        mProgressLayout.showLoading("努力找车中...");
        getData();

        return rootView;
    }

    /**
     * 重新加载车辆详情数据
     *
     * @param dotID
     * @param dotCarNumber
     */
    public void loadNewData(String dotID, int dotCarNumber) {
        this.dotID = dotID;
        this.dotCarNumber = dotCarNumber;
        mProgressLayout.showLoading("努力找车中...");
        getData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        isDestory = true;
    }

    public boolean isDestroy() {
        return isDestory;
    }

    public void onEventMainThread(BaseEvent event) {
        if (EventBusConstant.EVENT_TYPE_REFRESH_CARDETAIL.equals(event.getType())) {
            L.i("准备刷新车辆详情");
            mProgressLayout.showLoading("努力找车中...");
            getData();
        }
        // 如果规则弹窗显示，则关闭规则弹窗
        else if (EventBusConstant.EVENT_TYPE_CLOSE_RULE_DIALOG.equals(event.getType())) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }


    private CarInfoAdapter adapter;

    public void initViewPager() {
        adapter = new CarInfoAdapter();
        mViewPager.setAdapter(adapter);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (carInfoList.size() <= 1) {
                    return true;
                }
                return false;
            }
        });
        mViewPager.addOnPageChangeListener(new UUViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentCarBaseInfo = carInfoList.get(position);
                initServerPrice(currentCarBaseInfo);
                if (position == 0) {
                    mLeftPage.setVisibility(View.INVISIBLE);
                    mRightPage.setVisibility(View.VISIBLE);
                } else if (position == carInfoList.size() - 1) {
                    mLeftPage.setVisibility(View.VISIBLE);
                    mRightPage.setVisibility(View.INVISIBLE);
                } else {
                    mLeftPage.setVisibility(View.VISIBLE);
                    mRightPage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }


    public void getData() {
        if (Config.isNetworkConnected(mContext)) {
            // 未拿到经纬度以及城市ID时，进行一次定位然后再加载数据
            if (!Config.locationIsSuccess()) {
                Config.getCoordinates(mContext, new LocationListener() {
                    @Override
                    public void locationSuccess(double lat, double lng, String addr) {
                        getRentConfirmDetail();
                    }
                });
            } else {
                getRentConfirmDetail();
            }
        } else {
            mProgressLayout.showErrorSmall(onClickListener, "咦，与总部联系不上了...", "点击重试");
        }
    }

    /**
     * 加载网点车辆数据
     */
    private void getRentConfirmDetail() {
        UsercarInterface.RentConfirmDetail.Request.Builder request = UsercarInterface.RentConfirmDetail.Request.newBuilder();
        if (!dotID.equals("")) {
            request.setDotId(dotID);
        }
        request.setCurrentPositionLat(Config.lat);
        request.setCurrentPositionLon(Config.lng);
        if (TextUtils.isEmpty(Config.cityCode)) {
            Config.cityCode = "010";
        }
        request.setCityCode(Config.cityCode);
        NetworkTask task = new NetworkTask(Cmd.CmdCode.RentConfirmDetail_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
                    @Override
                    public void onSuccessResponse(UUResponseData responseData) {
                        if (responseData.getRet() == 0) {
                            try {
                                if (!isAdded()) {
                                    return;
                                }
                                showResponseCommonMsg(responseData.getResponseCommonMsg());
                                response = UsercarInterface.RentConfirmDetail.Response.parseFrom(responseData.getBusiData());

                                rootView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        showCarDetailInfo();
                                    }
                                }, 100);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(VolleyError errorResponse) {
                        mProgressLayout.showErrorSmall(onClickListener, "咦，与总部联系不上了...", "点击重试");
                    }

                    @Override
                    public void networkFinish() {
                    }
                }

        );
    }

    /**
     * 初始化价格信息
     *
     * @param currentCarBaseInfo
     */
    private void initServerPrice(UsecarCommon.CarBaseInfo currentCarBaseInfo) {

        pricePerKm = currentCarBaseInfo.getPricePerKm();
        pricePerMinute = currentCarBaseInfo.getPricePerMinute();
    }

    private void showCarDetailInfo() {
        if (!isAdded()) {
            return;
        }
        mProgressLayout.showContent();
        if (response.getRet() == 0) {
            if (response.getCarInfoList() != null && response.getCarInfoList().size() > 0) {
                carInfoList.clear();
                carInfoList.addAll(response.getCarInfoList());
                currentCarBaseInfo = carInfoList.get(0);
                initServerPrice(currentCarBaseInfo);
                mLeftPage.setVisibility(View.INVISIBLE);

                if (carInfoList.size() > 1) {
                    mRightPage.setVisibility(View.VISIBLE);
                    initViewPager();
                } else {
                    mRightPage.setVisibility(View.INVISIBLE);
                    initViewPager();
                }
            } else {
                //如果列表是0 提示有车提醒
                carRoot.setVisibility(View.GONE);
                mNocarRoot.setVisibility(View.VISIBLE);
                mB5Button.setText(getString(R.string.has_car_tip));
                mB5Button.setOnClickListener(new OnClickLoginedListener(mContext) {
                    @Override
                    public void onLoginedClick(View v) {
                        hasCarTip();
                    }
                });
            }
            carRoot.setVisibility(View.VISIBLE);
            mNocarRoot.setVisibility(View.GONE);
            // 拉取到的可用车辆与传递的网点可用车辆不一致时，需要刷新地图页网点
            if (dotCarNumber != carInfoList.size()) {
                // 发送刷新地图页网点数据消息
                EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_REFRESH_MAP_DOT));
                dotCarNumber = carInfoList.size();
            }
        } else if (response.getRet() == -2) {
            carRoot.setVisibility(View.GONE);
            mNocarRoot.setVisibility(View.VISIBLE);
            mB5Button.setOnClickListener(new OnClickLoginedListener(mContext) {
                @Override
                public void onLoginedClick(View v) {
                    hasCarTip();
                }
            });
            mB5Button.setText(getString(R.string.has_car_tip));
            if (dotCarNumber != 0) {
                // 发送刷新地图页网点数据消息
                EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_REFRESH_MAP_DOT));
                dotCarNumber = 0;
            }
        } else {
            mProgressLayout.showErrorSmall(onClickListener, "咦，与总部联系不上了...", "点击重试");
        }
    }


    /**
     * 加载错误时点击重试处理逻辑
     */
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mProgressLayout.showLoading("努力找车中...");
            getData();
        }
    };

    public void rentCar() {
        if (!UserConfig.isPassLogined()) {
            UserConfig.goToLoginDialog(mContext);
            return;
        }

        showProgress(false);
        UsercarInterface.RentConfirm.Request.Builder request = UsercarInterface.RentConfirm.Request.newBuilder();
        request.setDotId(dotID);
        if (currentCarBaseInfo != null) {
            request.setCarId(currentCarBaseInfo.getCarId());
        }
        // cityCode有可能为空
        if (TextUtils.isEmpty(Config.cityCode)) {
            Config.cityCode = "010";
        }
        request.setRentCity(Config.cityCode);
        request.setAccessType(accessType);
        request.setPricePerKm(pricePerKm);
        request.setPricePerMinute(pricePerMinute);

        NetworkTask task = new NetworkTask(Cmd.CmdCode.RentConfirm_NL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        rentConfirmResponse = UsercarInterface.RentConfirm.Response.parseFrom(responseData.getBusiData());
                        if (rentConfirmResponse.getRet() == 0) {
                            Intent intent = new Intent(mContext, MainActivity.class);
                            intent.putExtra("goto", MainActivity.GOTO_GET_CAR);
                            intent.putExtra(IntentConfig.ORDER_ID, rentConfirmResponse.getOrderId());

                            Config.setOrderId(mContext, rentConfirmResponse.getOrderId());
                            /*intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);*/
                            // 此处新开一个MainActivity跳转到取车页面，避免替换操作导致地图闪一次黑屏
                            startActivity(intent);
                            getActivity().finish();
                        }
                        //驾照认证不通过或者没认证
                        else if (rentConfirmResponse.getRet() == -3) {
                            startActivity(new Intent(mContext, ValidateLicenseActivity.class));
                        } else if (rentConfirmResponse.getRet() == -4) {

                            Config.showTiplDialog(mContext, null, "手慢啦，已被其他小伙伴抢先用车，再看看其他车辆吧", "好的", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // 发送刷新地图页网点数据消息
                                    EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_REFRESH_MAP_DOT));
                                    // 刷新底部卡片
                                    EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_REFRESH_CARDETAIL));
                                }
                            });

                        }
                        // 规则弹窗提示
                        else if (rentConfirmResponse.getRet() == -6) {
                            showRuleDialog(rentConfirmResponse);
                        } else {
                            if (responseData.getResponseCommonMsg() != null && !TextUtils.isEmpty(responseData.getResponseCommonMsg().getMsg())) {
                                showResponseCommonMsg(responseData.getResponseCommonMsg());
                            } else {
                                Config.showFiledToast(mContext);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                Config.showFiledToast(mContext);
            }

            @Override
            public void networkFinish() {
                dismissProgress();
            }
        });
    }

    /**
     * 显示规则弹窗提示用户用车之前的一些规则文案
     *
     * @param rentConfirmResponse
     */
    private void showRuleDialog(final UsercarInterface.RentConfirm.Response rentConfirmResponse) {
        pricePerKm = rentConfirmResponse.getPricePerKm();
        pricePerMinute = rentConfirmResponse.getPricePerMinute();

        List<String> popupMessageList = rentConfirmResponse.getPopupMessageList();

        dialog = AnimDialogUtils.getInstance(mContext);
        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.confirm_car_dialog, null);
        LinearLayout dialogContentView = (LinearLayout) dialogView.findViewById(R.id.ll_content);
        for (int i = 0; i < popupMessageList.size(); i++) {
            String msg = popupMessageList.get(i);
            msg = msg.replace("<br/>", "\n");
            View itemView = getActivity().getLayoutInflater().inflate(R.layout.confirm_car_dialog_content_item, null);
            ((TextView) itemView.findViewById(R.id.msg)).setText(msg);
            if (i == 0) {
                itemView.findViewById(R.id.line).setVisibility(View.GONE);
            }
            dialogContentView.addView(itemView);
        }

        android.widget.Button ok = (android.widget.Button) dialogView.findViewById(R.id.ok_btn);
        ok.setOnClickListener(new RuleDialogButtonClickListener(rentConfirmResponse.getPopupVersionList()));

        dialog.initView(dialogView, dialogCloseClickListener);
        dialogView.post(new Runnable() {
            @Override
            public void run() {

                // 当弹窗高度大于428dp时，手动将其设置为428dp这是最大高度
                int measuredHeight = dialogView.getMeasuredHeight();
                int maxHeight = DisplayUtil.dip2px(mContext, 428);
                if (measuredHeight > maxHeight) {
                    ViewGroup.LayoutParams layoutParams = dialogView.getLayoutParams();
                    layoutParams.height = maxHeight;
                    dialogView.requestLayout();
                }
            }
        });
        dialog.show();
    }

    private View.OnClickListener dialogCloseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            // 价格发生变化，点击关闭规则弹窗后，需要重新刷新底部车辆卡片的价格
            if (pricePerKm != currentCarBaseInfo.getPricePerKm() || pricePerMinute != currentCarBaseInfo.getPricePerMinute()) {
                EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_REFRESH_CARDETAIL));
            }

        }
    };

    /**
     * 规则提醒弹窗点击【我知道了】的执行逻辑
     */
    private class RuleDialogButtonClickListener implements View.OnClickListener {

        List<String> popupVersionList;

        public RuleDialogButtonClickListener(List<String> popupVersionList) {
            this.popupVersionList = popupVersionList;
        }

        @Override
        public void onClick(View v) {
            UsercarInterface.SubmitUserCommitment.Request.Builder request = UsercarInterface.SubmitUserCommitment.Request.newBuilder();
            request.addAllPopupVersion(popupVersionList);
            NetworkTask task = new NetworkTask(Cmd.CmdCode.SubmitUserCommitment_VALUE);
            task.setBusiData(request.build().toByteArray());
            NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {

                @Override
                public void onSuccessResponse(UUResponseData responseData) {
                    if (responseData.getRet() == 0) {
                        showResponseCommonMsg(responseData.getResponseCommonMsg());
                        try {
                            UsercarInterface.SubmitUserCommitment.Response response = UsercarInterface.SubmitUserCommitment.Response.parseFrom(responseData.getBusiData());
                            if (response.getRet() == 0) {
                                dialog.dismiss();
                                accessType = 2;
                                rentCar();
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    } else {
                        showDefaultNetworkSnackBar();
                    }
                }

                @Override
                public void onError(VolleyError errorResponse) {
                }

                @Override
                public void networkFinish() {
                }
            });

        }
    }


    /**
     * 有车提醒点击后的处理逻辑
     */
    private void hasCarTip() {
        if (!Config.isNetworkConnected(mContext)) {
            showDefaultNetworkSnackBar();
            return;
        }
        if (Config.lat != 0 && Config.lng != 0 && !TextUtils.isEmpty(Config.currentAddress)) {
            doUserReportDot();
        } else {
            showProgress(true);
            Config.getCoordinates(mContext, new LocationListener() {
                @Override
                public void locationSuccess(double lat, double lng, String addr) {
                    doUserReportDot();
                }
            });
        }
    }

    /**
     * 执行有车提醒上报
     */
    private void doUserReportDot() {
        UserInterface.UserReportDot.Request.Builder request = UserInterface.UserReportDot.Request.newBuilder();
        request.setUserLat(Config.lat);
        request.setUserLn(Config.lng);
        request.setReportAddress(Config.currentAddress);
        request.setReqType(2);
        if (dotID.equals("")) {
            request.setDotId("-1");
        } else {
            request.setDotId(dotID);
        }


        NetworkTask task = new NetworkTask(Cmd.CmdCode.ReportDot_NL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        showResponseCommonMsg(responseData.getResponseCommonMsg());
                        UserInterface.UserReportDot.Response response = UserInterface.UserReportDot.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                showDefaultNetworkSnackBar();
            }

            @Override
            public void networkFinish() {
                dismissProgress();
            }
        });
    }

    /**
     * 默认的网络错误SnackBar
     */
    public void showDefaultNetworkSnackBar() {
        String msg = getResources().getString(R.string.network_error_tip);
        Config.showToast(getActivity(), msg);
    }

    class CarInfoAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // 填充数据
            UsecarCommon.CarBaseInfo carBaseInfo = carInfoList.get(position);
            String activityDetails = carBaseInfo.getActivityDetails();
            View rootView;
            if (!TextUtils.isEmpty(activityDetails)) {
                rootView = LayoutInflater.from(mContext).inflate(R.layout.fragment_confirm_car, null);
            } else {
                rootView = LayoutInflater.from(mContext).inflate(R.layout.fragment_confirm_car2, null);
            }
            container.addView(rootView);
            CarInfoViewHolder viewHolder = new CarInfoViewHolder(rootView);
            UUApp.getInstance().display(carBaseInfo.getCarImgUrl(), viewHolder.mCarImg, R.mipmap.ic_car_unload_details);
            viewHolder.mCarName.setText(carBaseInfo.getBrand() + carBaseInfo.getModel() + " ");
            viewHolder.mCarNumber.setText(carBaseInfo.getCarLicense());
            viewHolder.mCarMileage.setText(carBaseInfo.getEndurance() + "");
            viewHolder.mPriceMileage.setText("￥" + String.format("%.2f", carBaseInfo.getPricePerKm()));
            viewHolder.mPriceTime.setText("￥" + String.format("%.2f", carBaseInfo.getPricePerMinute()));
            viewHolder.mIvTipIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, H5Activity.class);
                    intent.putExtra(H5Constant.TITLE, URLConfig.getUrlInfo().getBillingRule().getTitle());
                    intent.putExtra(H5Constant.MURL, URLConfig.getUrlInfo().getBillingRule().getUrl());
                    startActivity(intent);
                }
            });

            String labelColor = carBaseInfo.getLabelColor();
            if (!TextUtils.isEmpty(labelColor)) {
                viewHolder.mTvFavorable.setVisibility(View.VISIBLE);
                viewHolder.mTvPriceIcon.setVisibility(View.GONE);

                int color = Color.parseColor(labelColor);
                GradientDrawable gd = (GradientDrawable) viewHolder.mTvFavorable.getBackground();
                gd.setStroke(1, color);
                viewHolder.mTvFavorable.setText(carBaseInfo.getLabelContent());
                viewHolder.mTvFavorable.setTextColor(color);
            } else {
                viewHolder.mTvFavorable.setVisibility(View.GONE);
                viewHolder.mTvPriceIcon.setVisibility(View.VISIBLE);
            }

            if (!TextUtils.isEmpty(activityDetails)) {
                viewHolder.mTvTip.setText(activityDetails);
            }

            return rootView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return carInfoList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    class CarInfoViewHolder {
        @InjectView(R.id.car_img)
        NetworkImageView mCarImg;
        @InjectView(R.id.car_name)
        AutofitTextView mCarName;
        @InjectView(R.id.car_number)
        TextView mCarNumber;
        @InjectView(R.id.car_mileage)
        TextView mCarMileage;
        @InjectView(R.id.price_mileage)
        TextView mPriceMileage;
        @InjectView(R.id.price_time)
        TextView mPriceTime;
        @InjectView(R.id.iv_tip_icon)
        ImageView mIvTipIcon;
        @InjectView(R.id.tv_favorable)
        TextView mTvFavorable;
        @InjectView(R.id.tv_tip)
        TextView mTvTip;
        @InjectView(R.id.tv_price_icon)
        ImageView mTvPriceIcon;

        public CarInfoViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }


    /**
     * 重写onSaveInstanceState方法，防止Fragment not attached to Activity
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
    }
}
