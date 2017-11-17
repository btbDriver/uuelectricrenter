package com.youyou.uuelectric.renter.UI.main.rentcar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.android.volley.VolleyError;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rey.material.widget.Button;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.usecar.protobuf.iface.UsecarCommon;
import com.uu.facade.usecar.protobuf.iface.UsercarInterface;
import com.uu.facade.user.protobuf.bean.UserInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Network.listen.OnClickLoginedListener;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.UI.license.ValidateLicenseActivity;
import com.youyou.uuelectric.renter.UI.main.MainActivity;
import com.youyou.uuelectric.renter.UI.nearstation.NearStationActivity;
import com.youyou.uuelectric.renter.Utils.AnimDialogUtils;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
import com.youyou.uuelectric.renter.Utils.Support.LocationListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import me.grantland.widget.AutofitTextView;

public class ConfirmCarActivity extends BaseActivity {

    /**
     * 地图比例尺
     */
    public static int INTZOOM = 17;
    @InjectView(R.id.map)
    MapView mMapView;
    @InjectView(R.id.poi_name)
    AutofitTextView mPoiName;
    @InjectView(R.id.poi_desc)
    TextView mPoiDesc;
    @InjectView(R.id.viewPager)
    ViewPager mViewPager;
    @InjectView(R.id.doing_desc)
    TextView mDoingDesc;
    @InjectView(R.id.doing_title)
    TextView mDoingTitle;
    @InjectView(R.id.price_mileage)
    TextView mPriceMileage;
    @InjectView(R.id.price_time)
    TextView mPriceTime;
    @InjectView(R.id.price_root)
    LinearLayout mPriceRoot;
    @InjectView(R.id.left_button)
    Button mLeftButton;
    @InjectView(R.id.b3_button)
    Button mB3Button;
    @InjectView(R.id.buttom_root)
    LinearLayout mButtomRoot;
    @InjectView(R.id.img)
    ImageView mImg;
    @InjectView(R.id.tip_name)
    TextView mTipName;
    @InjectView(R.id.tip_desc)
    TextView mTipDesc;
    @InjectView(R.id.b5_button)
    Button mB5Button;
    @InjectView(R.id.b4_button)
    Button mB4Button;
    @InjectView(R.id.tip_other)
    TextView mTipOther;
    @InjectView(R.id.nocar_root)
    RelativeLayout mNocarRoot;
    @InjectView(R.id.rl)
    RelativeLayout mRl;

    @OnClick(R.id.left_page)
    public void leftPageClick() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
    }

    @OnClick(R.id.right_page)
    public void rightPageClick() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
    }

    private UsercarInterface.RentConfirmDetail.Response response;

    private CarInfoAdapter adapter;


    public View.OnClickListener hasCarTipClick = new OnClickLoginedListener(ConfirmCarActivity.this) {
        @Override
        public void onLoginedClick(View v) {
            if (!Config.isNetworkConnected(ConfirmCarActivity.this)) {
                showDefaultNetworkSnackBar();
                return;
            }
            showProgress(true);
            Config.getCoordinates(mContext, new LocationListener() {
                @Override
                public void locationSuccess(double lat, double lng, String addr) {
                    UserInterface.UserReportDot.Request.Builder request = UserInterface.UserReportDot.Request.newBuilder();
                    request.setUserLat(lat);
                    request.setUserLn(lng);
                    request.setReportAddress(addr);
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
            });
        }
    };
    public View.OnClickListener createDotClick = new OnClickLoginedListener(ConfirmCarActivity.this) {
        @Override
        public void onLoginedClick(View v) {
            if (!Config.isNetworkConnected(mContext)) {
                showDefaultNetworkSnackBar();
                return;
            }
            showProgress(true);
            Config.getCoordinates(mContext, new LocationListener() {
                @Override
                public void locationSuccess(double lat, double lng, String addr) {
                    UserInterface.UserReportDot.Request.Builder request = UserInterface.UserReportDot.Request.newBuilder();
                    request.setUserLat(lat);
                    request.setUserLn(lng);
                    request.setReportAddress(addr);
                    request.setDotId("-2");

                    request.setReqType(1);
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
                                    } else {
                                        showDefaultNetworkSnackBar();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();

                                }
                            } else {

                                showDefaultNetworkSnackBar();
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
            });

        }
    };

    @OnClick(R.id.b4_button)
    public void checkOtherClick() {
        if (!Config.isNetworkConnected(mContext)) {
            showDefaultNetworkSnackBar();
            return;
        }
        Intent intent = new Intent(mContext, NearStationActivity.class);
        intent.putExtra(IntentConfig.NEAR_STATION_NEED_GET_DATA, true);
        startActivity(intent);

    }

    private AMap aMap;
    public String carID = "";
    public String dotID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_car);
        ButterKnife.inject(this);
        mMapView.onCreate(savedInstanceState);

        initIntentData();
        getData();
        initView();
        initListener();

//        setupCustomView();
    }


    public void initIntentData() {
        if (getIntent().hasExtra(IntentConfig.DOT_ID) && !getIntent().getStringExtra(IntentConfig.DOT_ID).equals("")) {
            dotID = getIntent().getStringExtra(IntentConfig.DOT_ID);
        }
    }

    public void initView() {
        mB3Button.setText(getString(R.string.title_activity_confirm_car));
        mB5Button.setText(getString(R.string.has_car_notice_button));
        mB4Button.setText(getString(R.string.has_car_notice_outher_button));
        initMap();

        adapter = new CarInfoAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {


            @Override

            public boolean onTouch(View v, MotionEvent event) {

                if (carInfoList.size() == 0 || carInfoList.size() == 1)
                    return true;
                else
                    return false;

            }

        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

//                position = position % carInfoList.size();
//                if (carInfoList.size() > 1) { //多于1，才会循环跳转
//                    if (position < 1) { //首位之前，跳转到末尾（N）
//                        position = carInfoList.size() - 2;
//                        mViewPager.setCurrentItem(position, false);
//                    } else if (position > carInfoList.size() - 2) { //末位之后，跳转到首位（1）
//                        mViewPager.setCurrentItem(1, false); //false:不显示跳转过程的动画
//                        position = 1;
//                    }
//                }
//                currentCarBaseInfo = carInfoList.get(position);
//                upatePriceInfo();


            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void initListener() {
        mB3Button.setOnClickListener(new OnClickLoginedListener(mContext) {
            @Override
            public void onLoginedClick(View v) {
//                mSweetSheet3.toggle();
                rentCar();
            }
        });
    }

    public void initMap() {
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        aMap.getUiSettings().setZoomControlsEnabled(false); //地图是否允许显示缩放按钮
        aMap.getUiSettings().setScaleControlsEnabled(true); // 设置显示比例尺
        aMap.getUiSettings().setRotateGesturesEnabled(false);// 禁止3D旋转
        // 设置为true表示显示定位层并可触发定位
        aMap.setMyLocationEnabled(false);
    }

    public void addMarker(double lat, double lng) {
        LatLng latAndLng = new LatLng(lat, lng);
        MarkerOptions options = new MarkerOptions();
        options.anchor(0.5f, 1.0f);
        options.position(latAndLng);
        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_normal));
        Marker marker = aMap.addMarker(options);
        marker.setObject(latAndLng);

        aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latAndLng, INTZOOM, 0, 0)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();

        if (Config.cityCode == null || Config.cityCode.equals("")) {
            Config.getCoordinates(mContext, new LocationListener() {
                @Override
                public void locationSuccess(double lat, double lng, String addr) {

                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }


    /**
     * 加载错误时点击重试处理逻辑
     */
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            getData();
        }
    };

    private List<UsecarCommon.CarBaseInfo> carInfoList = new ArrayList<>();
    private UsecarCommon.CarBaseInfo currentCarBaseInfo;

    public void getData() {
        mProgressLayout.showLoading();
        if (Config.isNetworkConnected(mContext)) {
            Config.getCoordinates(mContext, new LocationListener() {
                @Override
                public void locationSuccess(double lat, double lng, String addr) {
                    UsercarInterface.RentConfirmDetail.Request.Builder request = UsercarInterface.RentConfirmDetail.Request.newBuilder();
                    if (!dotID.equals("")) {
                        request.setDotId(dotID);
                    }
                    request.setCurrentPositionLat(lat);
                    request.setCurrentPositionLon(lng);
                    request.setCityCode(Config.cityCode);
                    NetworkTask task = new NetworkTask(Cmd.CmdCode.RentConfirmDetail_NL_VALUE);
                    task.setBusiData(request.build().toByteArray());
                    NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
                        @Override
                        public void onSuccessResponse(UUResponseData responseData) {
                            if (responseData.getRet() == 0) {
                                try {
                                    showResponseCommonMsg(responseData.getResponseCommonMsg());
                                    response = UsercarInterface.RentConfirmDetail.Response.parseFrom(responseData.getBusiData());
                                    mProgressLayout.showContent();
                                    if (response.getRet() == 0) {
                                        addMarker(response.getDotLat(), response.getDotLon());
                                        mPoiName.setText(response.getDotName());
                                        mPoiDesc.setText(response.getDotDesc());
                                        if (response.getCarInfoList() != null && response.getCarInfoList().size() > 0) {
                                            carInfoList.clear();
//                                            carInfoList.add(response.getCarInfoList().get(response.getCarInfoList().size() - 1));
                                            carInfoList.addAll(response.getCarInfoList());
//                                            carInfoList.add(response.getCarInfoList().get(0));
                                            currentCarBaseInfo = carInfoList.get(0);
//                                            adapter.notifyDataSetChanged();
                                            mViewPager.setCurrentItem((carInfoList.size()) * 100);
                                        } else {
                                            //如果列表是0 提示有车提醒
                                            mNocarRoot.setVisibility(View.VISIBLE);
                                            mImg.setImageResource(R.mipmap.ic_nocar_reminder_pic);
                                            mB5Button.setOnClickListener(hasCarTipClick);
                                            setTitle(getResources().getString(R.string.has_car_notice_button));
                                        }
                                        upatePriceInfo();
                                    } else if (response.getRet() == -2) {
                                        mNocarRoot.setVisibility(View.VISIBLE);
                                        mImg.setImageResource(R.mipmap.ic_nocar_reminder_pic);
                                        mB5Button.setOnClickListener(hasCarTipClick);
                                        setTitle(getResources().getString(R.string.has_car_notice_button));
                                    } else if (response.getRet() == -3) {
                                        mNocarRoot.setVisibility(View.VISIBLE);
                                        mTipDesc.setText(getString(R.string.create_dot_notice_desc));
                                        mImg.setImageResource(R.mipmap.ic_buildnew_reminder);
                                        setTitle(getString(R.string.create_dot_button));
                                        mB5Button.setText(getString(R.string.create_dot_button));
                                        mB5Button.setOnClickListener(createDotClick);
                                    } else {
                                        mProgressLayout.showError(onClickListener);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onError(VolleyError errorResponse) {
                            mProgressLayout.showError(onClickListener);

                        }

                        @Override
                        public void networkFinish() {
                        }
                    });
                }
            });

        } else {
            mProgressLayout.showError(onClickListener);
        }
    }

    /**
     * 更新费用价格信息
     */
    private void upatePriceInfo() {
        if (currentCarBaseInfo != null) {
            if (currentCarBaseInfo.getActivityDesc() == null || currentCarBaseInfo.getActivityDesc().equals("")) {
                mDoingDesc.setVisibility(View.GONE);
            }

            mDoingDesc.setText(currentCarBaseInfo.getActivityDesc());
            if (currentCarBaseInfo.getActivityIconDesc() == null || currentCarBaseInfo.getActivityIconDesc().equals("")) {
                mDoingTitle.setVisibility(View.GONE);
            }
            mDoingTitle.setText(currentCarBaseInfo.getActivityIconDesc());
            mPriceMileage.setText("￥" + String.format("%.2f", currentCarBaseInfo.getPricePerKm()));
            mPriceTime.setText("￥" + String.format("%.2f", currentCarBaseInfo.getPricePerMinute()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == null)
            return true;
        if (item.getItemId() == android.R.id.home || item.getItemId() == 0) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);


    }

    String newestVersion = "";
    String commitMentMsg = "";
    AnimDialogUtils dialog;
    UsercarInterface.RentConfirm.Response rentConfirmResponse;

    public void rentCar() {

        showProgress(false);
        UsercarInterface.RentConfirm.Request.Builder request = UsercarInterface.RentConfirm.Request.newBuilder();
        request.setDotId(dotID);
        if (currentCarBaseInfo != null) {
            request.setCarId(currentCarBaseInfo.getCarId());
        }
        // cityCode有可能为空
        if (!TextUtils.isEmpty(Config.cityCode)) {
            Config.cityCode = "010";
        }
        request.setRentCity(Config.cityCode);

        NetworkTask task = new NetworkTask(Cmd.CmdCode.RentConfirm_NL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        showResponseCommonMsg(responseData.getResponseCommonMsg());
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
                                    finish();
                                }
                            });

                        }
                        //没有同意提醒规则
                        else if (rentConfirmResponse.getRet() == -5) {
                            newestVersion = rentConfirmResponse.getNewestVersionCode();
                            commitMentMsg = rentConfirmResponse.getCommitmentMsg();
                            dialog = AnimDialogUtils.getInstance(mContext);
                            View dialogView = getLayoutInflater().inflate(R.layout.confirm_car_dialog, null);
                            TextView msg = (TextView) dialogView.findViewById(R.id.msg);
                            Button ok = (Button) dialogView.findViewById(R.id.ok_btn);
                            if (commitMentMsg.indexOf("html") != -1) {
                                msg.setText(Html.fromHtml(commitMentMsg));
                            } else {
                                msg.setText(commitMentMsg);
                            }
                            ok.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showProgress(false);
                                    UsercarInterface.OpratorUserCommitment.Request.Builder request = UsercarInterface.OpratorUserCommitment.Request.newBuilder();
                                    request.setNewestVersionCode(newestVersion);
                                    NetworkTask task = new NetworkTask(Cmd.CmdCode.OpratorUserCommitment_NL_VALUE);
                                    task.setBusiData(request.build().toByteArray());
                                    NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
                                        @Override
                                        public void onSuccessResponse(UUResponseData responseData) {
                                            if (responseData.getRet() == 0) {
                                                showResponseCommonMsg(responseData.getResponseCommonMsg());
                                                try {
                                                    UsercarInterface.OpratorUserCommitment.Response response = UsercarInterface.OpratorUserCommitment.Response.parseFrom(responseData.getBusiData());
                                                    if (response.getRet() == 0) {
                                                        dialog.dismiss();
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
                                            Config.showFiledToast(mContext);
                                        }

                                        @Override
                                        public void networkFinish() {
                                            dismissProgress();
                                        }
                                    });
                                }
                            });
                            dialog.initView(dialogView, null);
                            dialog.show();
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

    @OnClick(R.id.left_button)
    public void leftBtnClick() {
        if (response != null && response.getRet() == 0) {
            Intent intent = new Intent(mContext, CostAssessActivity.class);
            intent.putExtra(IntentConfig.DOT_ID, dotID);
            intent.putExtra(IntentConfig.KEY_RENT_CONFIRM_DETAIL, currentCarBaseInfo);
            intent.putExtra(IntentConfig.KEY_DOTLAT, response.getDotLat());
            intent.putExtra(IntentConfig.KEY_DOTLON, response.getDotLon());
            intent.putExtra(IntentConfig.KEY_DOTNAME, response.getDotName());
            startActivityForResult(intent, 1);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.hasExtra("nocar")) {
            mNocarRoot.setVisibility(View.VISIBLE);
        }
    }

    private int maxPage = 0;//最大页码

    class CarInfoAdapter extends FragmentPagerAdapter {

        public CarInfoAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            int currentPosition = 0;
            try {
                currentPosition = position % carInfoList.size();
            } catch (Exception e) {
            }
            ConfirmCarFragment fragment = ConfirmCarFragment.getInstance();
            if (carInfoList.size() > 0) {
                UsecarCommon.CarBaseInfo carBaseInfo = carInfoList.get(currentPosition);
                fragment = ConfirmCarFragment.getInstance();
                Bundle bundle = new Bundle();
                bundle.putSerializable(IntentConfig.KEY_CARBASEINFO, carBaseInfo);
                fragment.setArguments(bundle);
            }
            return fragment;
        }

        @Override
        public int getCount() {
//            return carInfoList.size();
//            return Integer.MAX_VALUE;
            //设置成最大值以便循环滑动
            int cont = ((carInfoList == null) ? 0 : Integer.MAX_VALUE);
            maxPage = cont;
            return cont;
        }

//        @Override
//        public boolean isViewFromObject(View arg0, Object arg1) {
//
//            return arg0 == arg1;
//
//        }


//        @Override
//        public void destroyItem(View container, int position, Object object) {
//
//            if (carInfoList.size() == 1)
//                ((ViewPager) container).removeView(mImageViews[position/ carInfoList.length % 2][0]);
//
//            else
//                ((ViewPager) container).removeView(mImageViews[position/ carInfoList.length % 2][position % carInfoList.length]);
//
//        }
    }
}
