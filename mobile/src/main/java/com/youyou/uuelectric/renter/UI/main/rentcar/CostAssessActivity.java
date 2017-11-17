package com.youyou.uuelectric.renter.UI.main.rentcar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rey.material.widget.Button;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.dot.protobuf.iface.DotInterface;
import com.uu.facade.order.pb.bean.OrderInterface;
import com.uu.facade.usecar.protobuf.iface.UsercarInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.UI.license.ValidateLicenseActivity;
import com.youyou.uuelectric.renter.UI.main.MainActivity;
import com.youyou.uuelectric.renter.UI.main.bean.CarBaseBean;
import com.youyou.uuelectric.renter.UI.mapsearch.AddressSelectForMapActivity;
import com.youyou.uuelectric.renter.UI.web.H5Activity;
import com.youyou.uuelectric.renter.UI.web.H5Constant;
import com.youyou.uuelectric.renter.UI.web.url.URLConfig;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.AnimDialogUtils;
import com.youyou.uuelectric.renter.Utils.DisplayUtil;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;
import com.youyou.uuelectric.renter.Utils.view.RippleView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import me.grantland.widget.AutofitTextView;

/**
 * 费用预估Activity
 */
public class CostAssessActivity extends BaseActivity {

    /**
     * 进入到选择地址页的请求码
     */
    private static final int SELECT_ADDRESS_CODE = 1;
    @InjectView(R.id.ll_address_icon_container)
    LinearLayout mLlAddressIconContainer;
    @InjectView(R.id.tv_start_location)
    TextView mTvStartLocation;

    @InjectView(R.id.ll_address_container)
    LinearLayout mLlAddressContainer;
    @InjectView(R.id.tv_time_info)
    TextView mTvTimeInfo;
    @InjectView(R.id.rv_time_container)
    RippleView mRvTimeContainer;
    @InjectView(R.id.tv_favorable)
    TextView mTvFavorable;
    @InjectView(R.id.price_mileage)
    TextView mPriceMileage;
    @InjectView(R.id.price_time)
    TextView mPriceTime;
    @InjectView(R.id.b3_button)
    Button mB3Button;
    @InjectView(R.id.show_filter_container)
    FrameLayout mShowFilterContainer;
    @InjectView(R.id.tv_cost)
    TextView mTvCost;
    @InjectView(R.id.tv_price_icon)
    ImageView mTvPriceIcon;
    @InjectView(R.id.tv_location_item)
    TextView mLocation0Item;
    @InjectView(R.id.ll_price)
    LinearLayout mLlPrice;
    @InjectView(R.id.rl_desc_container)
    RelativeLayout mRlDescContainer;
    @InjectView(R.id.tv_key_distance)
    TextView mTvKeyDistance;
    @InjectView(R.id.tv_value_distance)
    TextView mTvValueDistance;
    @InjectView(R.id.tv_key_time)
    TextView mTvKeyTime;
    @InjectView(R.id.tv_value_time)
    TextView mTvValueTime;
    @InjectView(R.id.sv_container)
    ScrollView mSvContainer;
    @InjectView(R.id.tv_price_deatil)
    TextView tvPriceDeatil;
    @InjectView(R.id.img_price_deatil)
    ImageView imgPriceDeatil;
    @InjectView(R.id.layout_pirce_deatil)
    RelativeLayout layoutPirceDeatil;
    @InjectView(R.id.tv_prompt)
    TextView tvPrompt;
    @InjectView(R.id.trip_plan)
    RippleView tripPlan;
    @InjectView(R.id.trip_cross)
    TextView tripCross;
    @InjectView(R.id.tv_value_difprice)
    TextView tvValueDifprice;
    @InjectView(R.id.tv_key_difprice)
    TextView tvKeyDifprice;


    @InjectView(R.id.car_img)
    NetworkImageView mCarImg;
    @InjectView(R.id.car_name)
    AutofitTextView mCarName;
    @InjectView(R.id.car_number)
    TextView mCarNumber;
    @InjectView(R.id.price_time_trip)
    TextView mCarTime;
    @InjectView(R.id.price_mileage_trip)
    TextView mCarMileageTrip;
    @InjectView(R.id.car_mileage)
    TextView tvCarMileage;
    @InjectView(R.id.iv_tip_icon)
    ImageView mIvTipIcon;
    @InjectView(R.id.rl_desc_container)
    RelativeLayout containerLayout;
    @InjectView(R.id.tv_price_icon_trip)
    ImageView tvPriceIconTrip;
    @InjectView(R.id.tv_favorable_trip)
    TextView tvFavorableTrip;
    @InjectView(R.id.end_dot_bottom_view)
    View endDotBottomView;
    @InjectView(R.id.tv_price_desc)
    TextView tvPriceDesc;
    @InjectView(R.id.ll_location0_info)
    RelativeLayout llLocation0Info;
    @InjectView(R.id.iv_select_doticon)
    ImageView ivSelectDoticon;

    /**
     * 起点和终点坐标位置
     */
    private RouteSearch.FromAndTo fromAndTo;
    /**
     * 输入地址后的文字颜色
     */
    private int inputColor;
    /**
     * 根据目的地计算出来的总距离
     */
    private float totalDistance = 0f;
    /**
     * 最后一个目的地到起点的路线总距离
     */
    private float backTotalDistance = 0f;
    /**
     * 是否显示了时间选择器
     */
    private boolean isShowTimeContainer = false;

    /**
     * 选择的时间
     */

    private Date selectTime;

    private CarBaseBean carBaseInfo;
    /**
     * 网点信息
     */
    private DotInterface.DotInfo dotInfo;

    /**
     * 起点终点地址坐标
     */
    private LatLonPoint startAddress;
    private LatLonPoint endAddress;

    private String carId;

    /**
     * 取车网点id 和还车网点ID
     */
    private String startDotId;
    private String endDotId;

    /**
     * A-B 的还车时间和总距离
     */
    private long stepEndTime;
    private double stepDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_assess);
        ButterKnife.inject(this);

        initArgs();

        initView();
    }


    private void initView() {

        inputColor = getResources().getColor(R.color.c3);
        mB3Button.setText(getResources().getString(R.string.rent_car));
        //setRightOptBtnInfo(getResources().getString(R.string.price_rule), rightOptClick);
        mRvTimeContainer.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                showTimeSelector();
            }
        });

        //是否全网开放异地还车:1-支持，2-不支持
        int flag = getIntent().getIntExtra(IntentConfig.KEY_ISALLDOTSA2B, 0);
        if (flag == 0 || flag == 1) {
            llLocation0Info.setClickable(true);
            llLocation0Info.setFocusable(true);
            llLocation0Info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    location0Click();
                }
            });
            ivSelectDoticon.setVisibility(View.VISIBLE);
        } else if (flag == 2) {
            //不支持a2b
            llLocation0Info.setClickable(false);
            llLocation0Info.setFocusable(false);
            ivSelectDoticon.setVisibility(View.GONE);

            tvPrompt.setVisibility(View.VISIBLE);//便于费用预估TextView
            tripPlan.setVisibility(View.VISIBLE);//途径地点显示
            mRvTimeContainer.setVisibility(View.VISIBLE);//还车时间显示
            hiddenPrice();
            //默认费用详情收起
            imgPriceDeatil.setBackgroundResource(R.mipmap.ic_estimate_normal);
            containerLayout.setVisibility(View.GONE);
            //A-A不显示异地还车费
            tvKeyDifprice.setVisibility(View.GONE);
            tvValueDifprice.setVisibility(View.GONE);
            // A2A模式下A网点下的下划线显示
            endDotBottomView.setVisibility(View.VISIBLE);
            tripPlan.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
                @Override
                public void onComplete(RippleView rippleView) {
                    selectAddress();
                }
            });

            //填充换车点为取车点
            mLocation0Item.setText(getIntent().getStringExtra(IntentConfig.KEY_DOTNAME));
            endAddress = startAddress;
            endDotId = startDotId;
        }

    }

    /**
     * 获取头部数据
     */
    private void initArgs() {
        Intent intent = getIntent();
        mIvTipIcon.setOnClickListener(helpImage);//为帮助按钮添加点击事件
        startDotId = intent.getStringExtra(IntentConfig.DOT_ID);
        carBaseInfo = intent.getParcelableExtra(IntentConfig.KEY_RENT_CONFIRM_DETAIL);
        String carImgUrl = intent.getStringExtra(IntentConfig.CAR_IMGURL);
        String carName = intent.getStringExtra(IntentConfig.CAR_NAME);
        String carNumber = intent.getStringExtra(IntentConfig.CAR_NUMBER);
        String carMileage = intent.getStringExtra(IntentConfig.CAR_MILEAGE);
        if (carBaseInfo != null) {
            startAddress = new LatLonPoint(intent.getDoubleExtra(IntentConfig.KEY_DOTLAT, 0.0), intent.getDoubleExtra(IntentConfig.KEY_DOTLON, 0.0));
            String dotName = intent.getStringExtra(IntentConfig.KEY_DOTNAME);
            mTvStartLocation.setText(dotName);
            mCarMileageTrip.setText("¥" + String.format("%.2f", carBaseInfo.getPricePerKm()));
            mCarTime.setText("¥" + String.format("%.2f", carBaseInfo.getPricePerMinute()));
            UUApp.getInstance().display(carImgUrl, mCarImg, R.mipmap.ic_car_unload_details);
            tvCarMileage.setText(carMileage);
            mCarName.setText(carName);
            mCarNumber.setText(carNumber);
            carId = carBaseInfo.getCarId();
            accessType = 1;
            pricePerKm = carBaseInfo.getPricePerKm();
            pricePerMinute = carBaseInfo.getPricePerMinute();

            // 判断显示价格还是图标
            if (TextUtils.isEmpty(carBaseInfo.getLabelContent())) {
                tvPriceIconTrip.setVisibility(View.VISIBLE);
                tvFavorableTrip.setVisibility(View.GONE);
            } else {
                tvPriceIconTrip.setVisibility(View.GONE);
                tvFavorableTrip.setVisibility(View.VISIBLE);
                tvFavorableTrip.setText(carBaseInfo.getLabelContent());
            }
        }
    }


    /**
     * 显示时间选择器
     */
    public void showTimeSelector() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        Fragment fragment = new SelectTimeFragment();
        transaction.replace(R.id.show_filter_container, fragment);
        transaction.commit();

        isShowTimeContainer = true;
        updateContainerShowStatus();
    }

    private int getScreenHeight() {
        DisplayMetrics dm = new DisplayMetrics();
        //获取屏幕信息
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 更新加载时间控件、综合排序、筛选的布局容器的显示隐藏状态，并执行相应动画
     */
    private void updateContainerShowStatus() {
        int height = getScreenHeight() - mDefaultToolBar.getHeight();
        if (isShowTimeContainer) {
            mShowFilterContainer.setVisibility(View.VISIBLE);
            ValueAnimator valueAnimator = ValueAnimator.ofInt(height, 0);
            valueAnimator.setTarget(mShowFilterContainer);
            valueAnimator.setInterpolator(new AccelerateInterpolator());
            valueAnimator.setDuration(200).start();
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mShowFilterContainer.setTranslationY((int) animation.getAnimatedValue());
                }
            });

        } else {
            ValueAnimator valueAnimator = ValueAnimator.ofInt(0, height);
            valueAnimator.setTarget(mShowFilterContainer);
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.setDuration(200).start();
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mShowFilterContainer.setTranslationY((int) animation.getAnimatedValue());
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mShowFilterContainer.setVisibility(View.GONE);
                }
            });

        }
    }

    /**
     * 隐藏条件容器
     */
    public void hideFilterContainer() {
        if (isShowTimeContainer) {
            isShowTimeContainer = false;
            updateContainerShowStatus();
        }
    }

    /**
     * 更新界面时间展示
     *
     * @param timeStr
     * @param time
     */
    public void updateSelectTime(String timeStr, Date time) {
        mTvTimeInfo.setText(timeStr);
        mTvTimeInfo.setTextColor(inputColor);
        selectTime = time;
        sendRequestCalculatePrice();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_ADDRESS_CODE) {
                if (data != null) {
                    LocalPointItem localPointItem = data.getParcelableExtra("localPointItem");
                    if (localPointItem != null) {
                        acceptResult(localPointItem);
                    }
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0 || item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private Map<String, LatLonPoint> selectPoint = new TreeMap<>();
    private List<LatLonPoint> latLonPointList = new ArrayList<>();

    /**
     * 接收选择的地址结果
     *
     * @param localPointItem
     */
    private void acceptResult(LocalPointItem localPointItem) {
        String address = localPointItem.getAddress();


        double lat = localPointItem.getLat();
        double lng = localPointItem.getLng();

        tripCross.setText(address);
        tripCross.setTextColor(inputColor);

        calculateDistance(lat, lng);


    }

    /**
     * 帮助按钮点击事件
     */
    private View.OnClickListener helpImage = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, H5Activity.class);
            intent.putExtra(H5Constant.TITLE, URLConfig.getUrlInfo().getBillingRule().getTitle());
            intent.putExtra(H5Constant.MURL, URLConfig.getUrlInfo().getBillingRule().getUrl());
            startActivity(intent);
        }
    };


    /**
     * 新的A-B费用预估逻辑 包含异地还车
     */
    private void newPriceForceCast() {

        String arrivePlace = dotInfo.getDotName();
        if (!TextUtils.isEmpty(arrivePlace)) {
            LatLonPoint endAddress = new LatLonPoint(dotInfo.getDotLat(), dotInfo.getDotLon());

            fromAndTo = new RouteSearch.FromAndTo(startAddress, endAddress);
        } else {
            L.i("未找到正确的终点地址...");
            mLlPrice.setVisibility(View.GONE);
            mRlDescContainer.setVisibility(View.GONE);
            dismissProgress();
            return;
        }
        if (fromAndTo == null) {
            L.i("未设置起点和终点坐标...");
            mLlPrice.setVisibility(View.GONE);
            mRlDescContainer.setVisibility(View.GONE);
            dismissProgress();
            return;
        }
        showProgress(false);
        RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DrivingDefault, null, null, "");
        RouteSearch routeSearch = new RouteSearch(mContext);
        routeSearch.calculateDriveRouteAsyn(query);
        routeSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
            @Override
            public void onBusRouteSearched(BusRouteResult busRouteResult, int rCode) {

            }

            @Override
            public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int rCode) {
                if (rCode == 0) {
                    if (driveRouteResult != null && driveRouteResult.getPaths() != null
                            && driveRouteResult.getPaths().size() > 0) {
                        DrivePath path = driveRouteResult.getPaths().get(0);

                        stepDistance = (int) path.getDistance();//驾车距离
                        long time = path.getDuration();//驾车时间
                        long current = System.currentTimeMillis();
                        stepEndTime = current / 1000 + time;//还车时间
                        checkAtoBSendRequest(stepDistance, stepEndTime, carId);//进行A-B费用预估请求

                    } else {
                        dismissProgress();
                        Config.showFiledToast(mContext);
                    }
                } else {
                    dismissProgress();
                    Config.showFiledToast(mContext);
                }

            }

            @Override
            public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int rCode) {

            }
        });
    }

    /**
     * ATOBd请求  进行费用预估
     */
    private void checkAtoBSendRequest(final double stepDistance, long stepEndTime, String carId) {

        if (!Config.isNetworkConnected(mContext)) {
            showDefaultNetworkSnackBar();
            dismissProgress();
            return;
        }
        if (stepEndTime == 0) {
            L.i("未获取还车时间");
            dismissProgress();
            return;
        }
        if (stepDistance == 0) {
            L.i("未计算出总距离");
            mLlPrice.setVisibility(View.VISIBLE);
            mRlDescContainer.setVisibility(View.VISIBLE);
            dismissProgress();
            return;
        }
        if (TextUtils.isEmpty(carId)) {
            L.i("carId为null，不能发送请求");
            dismissProgress();
            return;
        }

        if (!Config.loadingDialogIsShowing()) {
            showProgress(true);
        }
        L.i("可以发送请求----总距离：" + stepDistance + "\t 时间:" + stepEndTime);
        OrderInterface.PriceEstimate.Request.Builder builder = OrderInterface.PriceEstimate.Request.newBuilder();
        builder.setDistance(stepDistance);
        builder.setEndTime((int) stepEndTime);
        builder.setCarId(carId);
        // A to B 返回距离为0
        builder.setBackDistance(0);
        if (TextUtils.isEmpty(Config.cityCode)) {
            Config.cityCode = "010";
        }
        builder.setCityCode(Config.cityCode);
        builder.setStartParkingId(startDotId);
        builder.setEndParkingId(dotInfo.getDotId());
        NetworkTask task = new NetworkTask(Cmd.CmdCode.PriceEstimateUnLogin_VALUE);
        task.setBusiData(builder.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {

            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    showResponseCommonMsg(responseData.getResponseCommonMsg());
                    try {
                        OrderInterface.PriceEstimate.Response response = OrderInterface.PriceEstimate.Response.parseFrom(responseData.getBusiData());
                        if (response != null && response.getRet() == 0) {
                            double cost = response.getCost();
                            String result = String.format("%.2f", cost);
                            mTvCost.setText(result);
                            mTvCost.setVisibility(View.VISIBLE);
                            mLlPrice.setVisibility(View.VISIBLE);
                            tvPriceDeatil.setVisibility(View.VISIBLE);
                            imgPriceDeatil.setVisibility(View.VISIBLE);
                            tvPriceDesc.setVisibility(View.VISIBLE);
                            layoutPirceDeatil.setVisibility(View.VISIBLE);
                            // 展示或者隐藏费用详情
                            layoutPirceDeatil.setOnClickListener(priceDetailOnClickListener);
                            mLlPrice.setOnClickListener(priceDetailOnClickListener);


                            double distance = stepDistance;
                            String keyDistance;

                            if (distance / 1000 > 0) {
                                keyDistance = String.format("%.1f", distance / 1000) + "公里";
                            } else {
                                keyDistance = String.format("%.1f", distance) + "米";
                            }
                            mTvKeyDistance.setText("里程(" + keyDistance + ")");
                            mTvValueDistance.setText(String.format("%.2f", response.getMileagePrice()) + "元");
                            mTvKeyTime.setText("时长(" + response.getTimeSource() + ")");
                            mTvValueTime.setText(String.format("%.2f", response.getTimePrice()) + "元");
                            // 设置异地还车费
                            if (response.getBackCarFee() >= 0) {
                                tvValueDifprice.setText(String.format("%.2f", response.getBackCarFee()) + "元");
                                tvKeyDifprice.setVisibility(View.VISIBLE);
                            } else {
                                tvKeyDifprice.setVisibility(View.GONE);
                                tvValueDifprice.setVisibility(View.GONE);
                            }
                            mSvContainer.post(new Runnable() {
                                @Override
                                public void run() {
                                    mSvContainer.fullScroll(View.FOCUS_DOWN);
                                }
                            });

                            if (!TextUtils.isEmpty(response.getTimeFee())) {
                                pricePerMinute = Float.valueOf(response.getTimeFee());
                            }
                            if (!TextUtils.isEmpty(response.getMileageFee())) {
                                pricePerKm = Float.valueOf(response.getMileageFee());
                            }

                            carBaseInfo.setLabelContent(response.getLabelContent());
                            carBaseInfo.setLabelColor(response.getLabelColor());

                            checkPriceChange();

                        }
                    } catch (InvalidProtocolBufferException e) {
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


    @Override
    protected void onResume() {
        super.onResume();
        imgPriceDeatil.clearAnimation();
    }
    /**
     * 展示或者隐藏费用详情
     */
    View.OnClickListener priceDetailOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (containerLayout.getVisibility() == View.GONE) {
                //上下箭头动画
                RotateAnimation animation_rotate_top = new RotateAnimation(0, 180,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f);
                animation_rotate_top.setFillAfter(true);
                animation_rotate_top.setDuration(300);
                imgPriceDeatil.startAnimation(animation_rotate_top);


                containerLayout.setVisibility(View.VISIBLE);
                mSvContainer.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSvContainer.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                },300);
                /*//费用详情内容动画
                //, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f
                ScaleAnimation animation_scale = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f);
                AlphaAnimation animation_alpha = new AlphaAnimation(0.0f, 1.0f);
                AnimationSet animationSet = new AnimationSet(true);
                animationSet.addAnimation(animation_alpha);//透明度
                animationSet.addAnimation(animation_scale);
                animationSet.setDuration(300);
                animationSet.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        containerLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mSvContainer.post(new Runnable() {
                            @Override
                            public void run() {
                                mSvContainer.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                containerLayout.startAnimation(animationSet);*/

            } else {
                //上下箭头动画
                RotateAnimation animation_rotate = new RotateAnimation(180, 360,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f);
//                animation_rotate.setFillAfter(true);
                animation_rotate.setDuration(300);
                imgPriceDeatil.startAnimation(animation_rotate);

                containerLayout.setVisibility(View.GONE);
                /*//费用详情内容动画
                ScaleAnimation animation_scale = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f);
                AlphaAnimation animation_alpha = new AlphaAnimation(1.0f, 0.0f);
                AnimationSet animationSet = new AnimationSet(true);
                animationSet.addAnimation(animation_alpha);//透明度
                animationSet.addAnimation(animation_scale);
                animationSet.setDuration(300);
                animationSet.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        containerLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                containerLayout.startAnimation(animationSet);*/

            }
        }
    };


    /**
     * 总距离路线计算是否完成
     */
    private boolean totalDistanceIsFinish = false;

    /**
     * 计算A-途经点+返回距离  ！
     */
    public void calculateDistance(double lat, double lng) {
        if (!Config.isNetworkConnected(mContext)) {
            showDefaultNetworkSnackBar();
            return;
        }
        if (selectTime != null) {
            showProgress(true);
        }

        fromAndTo = null;
        totalDistance = 0;
        backTotalDistance = 0;
        totalDistanceIsFinish = false;
        latLonPointList.clear();
        latLonPointList.add(new LatLonPoint(lat, lng));
        // 根据起点和终点坐标构建路线查询的FromAndTo对象.....
        endAddress = startAddress;

        if (endAddress != null && startAddress != null) {
            fromAndTo = new RouteSearch.FromAndTo(startAddress, endAddress);
        } else {
            L.i("未找到正确的终点地址...");
        }
        if (fromAndTo == null) {
            L.i("未设置起点和终点坐标...");
            mLlPrice.setVisibility(View.GONE);
            mRlDescContainer.setVisibility(View.GONE);
            dismissProgress();
            return;
        }

        L.i("开始计算路线，途径点个数：" + latLonPointList.size());

        // 构建总距离路线查询
        RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DrivingMultiStrategy, latLonPointList, null, "");
        RouteSearch routeSearch = new RouteSearch(mContext);
        routeSearch.calculateDriveRouteAsyn(query);
        routeSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
            @Override
            public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

            }

            @Override
            public void onDriveRouteSearched(DriveRouteResult result, int rCode) {
                if (rCode == 0) {
                    if (result != null && result.getPaths() != null
                            && result.getPaths().size() > 0) {
                        DrivePath path = result.getPaths().get(0);
                        List<DriveStep> steps = path.getSteps();

                        if (steps != null && steps.size() > 0) {
                            for (DriveStep step : steps) {
                                totalDistance += step.getDistance();
                            }
                        }
                        L.i("totalDistance:" + totalDistance);

                        totalDistanceIsFinish = true;
                        checkIsCanSendRequest();
                    } else {
                        dismissProgress();
                        showDefaultNetworkSnackBar();
                        // 计算距离失败为途径地点设置默认文案
                        tripCross.setText(getResources().getString(R.string.cost_select_address));
                    }
                } else {
                    dismissProgress();
                    showDefaultNetworkSnackBar();
                    // 计算距离失败为途径地点设置默认文案
                    tripCross.setText(getResources().getString(R.string.cost_select_address));
                }
            }

            @Override
            public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

            }
        });


    }


    /**
     * 之前A-A的费用预估逻辑
     */
    private void checkIsCanSendRequest() {
        // 两个路线都计算完成时，才发送费用预算请求
        if (totalDistanceIsFinish && startDotId.equals(endDotId)) {

            sendRequestCalculatePrice();
        } else {
            newPriceForceCast();
        }
    }

    /**
     * 发送请求到服务，根据总距离和时间计算评估的费用
     */
    private void sendRequestCalculatePrice() {
        if (!Config.isNetworkConnected(mContext)) {
            showDefaultNetworkSnackBar();
            return;
        }
        if (selectTime == null) {
            L.i("未选择还车时间");
            return;
        }
        if (totalDistance == 0) {
            L.i("未计算出总距离");
            if (tripCross.getText().toString() != null && tripCross.getText().toString().equals(getResources().getString(R.string.cost_select_address))) {
                Config.showToast(mContext, "请选择途径地点");

                mLlPrice.setVisibility(View.GONE);
                mRlDescContainer.setVisibility(View.GONE);
                return;
            } else {
                showProgress(false);
                mLlPrice.setVisibility(View.GONE);
                mRlDescContainer.setVisibility(View.GONE);
            }

        }
        if (TextUtils.isEmpty(carId)) {
            L.i("carId为null，不能发送请求");
            return;
        }

        if (!Config.loadingDialogIsShowing()) {
            showProgress(true);
        }

        L.i("可以发送请求----总距离：" + totalDistance + "\t 返回距离：" + backTotalDistance + "\t 时间:" + selectTime.getTime());
        hiddenPrice();//清空数据
        OrderInterface.PriceEstimate.Request.Builder builder = OrderInterface.PriceEstimate.Request.newBuilder();
        builder.setDistance(totalDistance);//驾车总计距离
        builder.setBackDistance(backTotalDistance);//返回距离
        builder.setEndTime((int) (selectTime.getTime() / 1000L));
        builder.setCarId(carId);//车辆id
        if (TextUtils.isEmpty(Config.cityCode)) {
            Config.cityCode = "010";
        }
        builder.setCityCode(Config.cityCode);//判断城市信息
        builder.setStartParkingId(startDotId);//取车还车网点
        builder.setEndParkingId(endDotId);
        NetworkTask task = new NetworkTask(Cmd.CmdCode.PriceEstimateUnLogin_VALUE);
        task.setBusiData(builder.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {

            @Override
            public void onSuccessResponse(UUResponseData responseData) {

                if (responseData.getRet() == 0) {
                    showResponseCommonMsg(responseData.getResponseCommonMsg());
                    try {
                        OrderInterface.PriceEstimate.Response response = OrderInterface.PriceEstimate.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            double cost = response.getCost();
                            String result = String.format("%.2f", cost);
                            // 费用金额
                            mTvCost.setText(result);
                            // 费用金额布局文件
                            mLlPrice.setVisibility(View.VISIBLE);
                            // 里程布局
                            mRlDescContainer.setVisibility(View.VISIBLE);
                            // 时常布局
                            tvPriceDeatil.setVisibility(View.VISIBLE);
                            // 费用图标详情
                            imgPriceDeatil.setVisibility(View.VISIBLE);
                            // 费用描述
                            tvPriceDesc.setVisibility(View.VISIBLE);
                            // 费用详情布局
                            layoutPirceDeatil.setVisibility(View.VISIBLE);
                            containerLayout.setVisibility(View.GONE);
                            // 展示或者隐藏费用详情
                            layoutPirceDeatil.setOnClickListener(priceDetailOnClickListener);
                            mLlPrice.setOnClickListener(priceDetailOnClickListener);

                            double distance = totalDistance;
                            String keyDistance;
                            if (distance / 1000 > 0) {
                                keyDistance = String.format("%.1f", distance / 1000) + "公里";
                            } else {
                                keyDistance = String.format("%.1f", distance) + "米";
                            }
                            mTvKeyDistance.setText("里程(" + keyDistance + ")");
                            mTvValueDistance.setText(String.format("%.2f", response.getMileagePrice()) + "元");

                            mTvKeyTime.setText("时长(" + response.getTimeSource() + ")");
                            mTvValueTime.setText(String.format("%.2f", response.getTimePrice()) + "元");
                            mSvContainer.post(new Runnable() {
                                @Override
                                public void run() {
                                    mSvContainer.fullScroll(View.FOCUS_DOWN);
                                }
                            });

                            if (!TextUtils.isEmpty(response.getTimeFee())) {
                                pricePerMinute = Float.valueOf(response.getTimeFee());
                            }
                            if (!TextUtils.isEmpty(response.getMileageFee())) {
                                pricePerKm = Float.valueOf(response.getMileageFee());
                            }

                            carBaseInfo.setLabelContent(response.getLabelContent());
                            carBaseInfo.setLabelColor(response.getLabelColor());

                            checkPriceChange();
                        }
                    } catch (InvalidProtocolBufferException e) {
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
     * 右侧的删除地址Icon点击逻辑
     */
    private View.OnClickListener delIconClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String tag = (String) view.getTag();
            L.i("delIconClick---点击的是：" + tag);
            deleteAddressByTag(tag);
        }
    };


    /**
     * 删除选择的目的地对应的坐标点，并发送费用预估请求
     *
     * @param deleteTag
     */
    private void deleteItemPoint(String deleteTag) {
        if (selectPoint.containsKey(deleteTag)) {
            selectPoint.remove(deleteTag);
        }
    }

    /**
     * 根据tag删除添加的目的地地址，及Icon
     *
     * @param tag
     */
    private void deleteAddressByTag(String tag) {
        View iconView = mLlAddressIconContainer.findViewWithTag(tag);
        mLlAddressIconContainer.removeView(iconView);
        View addressView = mLlAddressContainer.findViewWithTag(tag);
        mLlAddressContainer.removeView(addressView);

        deleteItemPoint(tag);
    }

    /**
     * 网点选择点击事件
     */
    //@OnClick(R.id.ll_location0_info)
    public void location0Click() {


        selectParkInfo();
    }

    /**
     * 立即用车点击事件
     */
    @OnClick(R.id.b3_button)
    public void confirmUseCar() {
        rentCar();
    }

    /**
     * 获取网点数据操作
     *
     * @param event
     */
    @Override
    public void onEventMainThread(BaseEvent event) {
        super.onEventMainThread(event);

        // 处理网点列表选择后事件(常用网点， 附近网点，地图选择网点，地图列表网点)
        if (EventBusConstant.EVENT_TYPE_SELECTED_DOTINFO2.equals(event.getType())) {
            dotInfo = (DotInterface.DotInfo) event.getExtraData();
            endDotId = dotInfo.getDotId();
            mLocation0Item.setText(dotInfo.getDotName());

            //A取A还
            if (startDotId.equals(endDotId)) {
                //进行页面的初始化操作
                tvPrompt.setVisibility(View.VISIBLE);//便于费用预估TextView
                tripPlan.setVisibility(View.VISIBLE);//途径地点显示
                mRvTimeContainer.setVisibility(View.VISIBLE);//还车时间显示
                hiddenPrice();
                //默认费用详情收起
                imgPriceDeatil.setBackgroundResource(R.mipmap.ic_estimate_normal);
                containerLayout.setVisibility(View.GONE);
                //A-A不显示异地还车费
                tvKeyDifprice.setVisibility(View.GONE);
                tvValueDifprice.setVisibility(View.GONE);
                // A2A模式下A网点下的下划线显示
                endDotBottomView.setVisibility(View.VISIBLE);
                tripPlan.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
                    @Override
                    public void onComplete(RippleView rippleView) {
                        selectAddress();
                    }
                });
                sendRequestCalculatePrice();
            } else {
                //A-B
                //判断异地还车费初始化
                tvValueDifprice.setVisibility(View.VISIBLE);
                tvValueDifprice.setText("");
                tvPriceDesc.setVisibility(View.GONE);
                hiddenPrice();
                //默认费用详情收起
                imgPriceDeatil.setBackgroundResource(R.mipmap.ic_estimate_normal);

                //默认显示A-B UI
                tvPrompt.setVisibility(View.GONE);
                tripPlan.setVisibility(View.GONE);
                mRvTimeContainer.setVisibility(View.GONE);

                // A2B模式下B网点下的下划线不显示
                endDotBottomView.setVisibility(View.GONE);
                newPriceForceCast();
            }


        }
    }

    /**
     * 费用预估跳转清空
     */
    private void hiddenPrice() {
        mLlPrice.setVisibility(View.GONE);//费用预估隐藏
        layoutPirceDeatil.setVisibility(View.GONE);
        containerLayout.setVisibility(View.GONE);
        tvPriceDesc.setVisibility(View.GONE);
    }


    /**
     * 进入到地址选择页
     */
    private void selectAddress() {
        startActivityForResult(new Intent(this, AddressSelectActivity.class), SELECT_ADDRESS_CODE);
        hiddenPrice();
    }

    /**
     * 进入网点选择
     */
    private void selectParkInfo() {
        Intent intent = new Intent(CostAssessActivity.this, AddressSelectForMapActivity.class);
        intent.putExtra(IntentConfig.DOT_ID, startDotId);
        intent.putExtra(IntentConfig.KEY_DOTLAT, getIntent().getDoubleExtra(IntentConfig.KEY_DOTLAT, 0.0));
        intent.putExtra(IntentConfig.KEY_DOTLON, getIntent().getDoubleExtra(IntentConfig.KEY_DOTLON, 0.0));
        intent.putExtra(IntentConfig.KEY_DOTNAME, getIntent().getStringExtra(IntentConfig.KEY_DOTNAME));
        intent.putExtra(IntentConfig.KEY_DOTADDR, getIntent().getStringExtra(IntentConfig.KEY_DOTADDR));
        intent.putExtra(IntentConfig.GET_DOT_TYPE, 1);
        startActivity(intent);
    }

    UsercarInterface.RentConfirm.Response rentConfirmResponse;

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

    public void rentCar() {
        if (!UserConfig.isPassLogined()) {
            UserConfig.goToLoginDialog(mContext);
            return;
        }

        showProgress(true);
        UsercarInterface.RentConfirm.Request.Builder request = UsercarInterface.RentConfirm.Request.newBuilder();
        request.setDotId(getIntent().getStringExtra(IntentConfig.DOT_ID));
        if (!TextUtils.isEmpty(endDotId)) {
            request.setReturnDotId(endDotId);
        }
        request.setCarId(carId);
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
//                        showResponseCommonMsg(responseData.getResponseCommonMsg());
                        rentConfirmResponse = UsercarInterface.RentConfirm.Response.parseFrom(responseData.getBusiData());
                        if (rentConfirmResponse.getRet() == 0) {
                            Intent intent = new Intent(mContext, MainActivity.class);
                            intent.putExtra("goto", MainActivity.GOTO_GET_CAR);
                            intent.putExtra(IntentConfig.ORDER_ID, rentConfirmResponse.getOrderId());

                            Config.setOrderId(mContext, rentConfirmResponse.getOrderId());
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();
                        }
                        //驾照认证不通过或者没认证
                        else if (rentConfirmResponse.getRet() == -3) {
                            startActivity(new Intent(mContext, ValidateLicenseActivity.class));
                        } else if (rentConfirmResponse.getRet() == -4) {

                            Config.showTiplDialog(mContext, null, "手慢啦，已被其他小伙伴抢先用车，再看看其他车辆吧", "好的", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(mContext, MainActivity.class);
                                    intent.putExtra("goto", MainActivity.GOTO_MAP);
                                    Config.isAgainRequestDotList = true;
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);

                                    // 发送刷新底部车详情卡片数据事件
                                    EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_UPDATE_CAR_DETAIL_BY_LOGIN));

                                    finish();
                                }
                            });

                        }
                        // 显示规则提醒弹窗
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
        carBaseInfo.setLabelContent(rentConfirmResponse.getLabelContent());
        carBaseInfo.setLabelColor(rentConfirmResponse.getLabelColor());

        List<String> popupMessageList = rentConfirmResponse.getPopupMessageList();

        AnimDialogUtils dialog = AnimDialogUtils.getInstance(mContext);
        final View dialogView = getLayoutInflater().inflate(R.layout.confirm_car_dialog, null);
        LinearLayout dialogContentView = (LinearLayout) dialogView.findViewById(R.id.ll_content);
        for (int i = 0; i < popupMessageList.size(); i++) {
            String msg = popupMessageList.get(i);
            msg = msg.replace("<br/>", "\n");
            View itemView = getLayoutInflater().inflate(R.layout.confirm_car_dialog_content_item, null);
            ((TextView) itemView.findViewById(R.id.msg)).setText(msg);
            if (i == 0) {
                itemView.findViewById(R.id.line).setVisibility(View.GONE);
            }
            dialogContentView.addView(itemView);
        }

        android.widget.Button ok = (android.widget.Button) dialogView.findViewById(R.id.ok_btn);
        ok.setOnClickListener(new RuleDialogButtonClickListener(dialog, rentConfirmResponse.getPopupVersionList()));

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

            checkPriceChange();

        }
    };

    /**
     * 核查价格变化
     */
    private void checkPriceChange() {
        if (pricePerKm != carBaseInfo.getPricePerKm() || pricePerMinute != carBaseInfo.getPricePerMinute()) {
            mPriceMileage.setText("¥" + String.format("%.2f", pricePerKm));
            mPriceTime.setText("¥" + String.format("%.2f", pricePerMinute));

            carBaseInfo.setPricePerKm(pricePerKm);
            carBaseInfo.setPricePerMinute(pricePerMinute);

        }
    }


    /**
     * 规则提醒弹窗点击【我知道了】的执行逻辑
     */
    private class RuleDialogButtonClickListener implements View.OnClickListener {

        AnimDialogUtils dialog;
        List<String> popupVersionList;

        public RuleDialogButtonClickListener(AnimDialogUtils dialog, List<String> popupVersionList) {
            this.dialog = dialog;
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
}
