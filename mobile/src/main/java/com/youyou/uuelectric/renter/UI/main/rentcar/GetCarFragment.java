package com.youyou.uuelectric.renter.UI.main.rentcar;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.navi.model.NaviLatLng;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.google.protobuf.InvalidProtocolBufferException;
import com.umeng.analytics.MobclickAgent;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.base.common.UuCommon;
import com.uu.facade.order.pb.bean.OrderInterface;
import com.uu.facade.order.pb.common.OrderCommon;
import com.uu.facade.usecar.protobuf.iface.UsercarInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Network.listen.OnClickNormalListener;
import com.youyou.uuelectric.renter.Network.user.SPConstant;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.main.MainActivity;
import com.youyou.uuelectric.renter.UI.main.StartNaviActivity;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.DialogUtil;
import com.youyou.uuelectric.renter.Utils.UMCountConstant;
import com.youyou.uuelectric.renter.Utils.layer.GetcarLayerUtil;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.Support.LocationListener;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;
import com.youyou.uuelectric.renter.Utils.map.NavMapUtil;
import com.youyou.uuelectric.renter.Utils.popupwindow.PopupMenuManager;
import com.youyou.uuelectric.renter.Utils.view.RippleView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 取车页面Fragment
 */
public class GetCarFragment extends GetCarPresenter {

    View rootView;
    @InjectView(R.id.img)
    ImageView img;
    @InjectView(R.id.text1)
    TextView text1;
    @InjectView(R.id.text2)
    TextView text2;
    @InjectView(R.id.park_number)
    TextView parkNumber;
    @InjectView(R.id.car_img)
    NetworkImageView carImg;
    @InjectView(R.id.car_name)
    TextView carName;
    @InjectView(R.id.car_nubmer)
    TextView carNubmer;
    @InjectView(R.id.time)
    TextView time;
    @InjectView(R.id.open_car_door)
    LinearLayout openCarDoor;
    @InjectView(R.id.find_car)
    RippleView mFindCar;
    @InjectView(R.id.getcar_wait_pay_linear)
    LinearLayout waitPay;
    @InjectView(R.id.getcar_wait_fee)
    TextView waitFeeText;

    private OrderCommon.ParkingInfo parkingInfo;
    private String checkLocationInfoMessage = "";
    public String orderId;
    private TimeCount timeCount;
    private boolean isOnStop = false;

    // menu菜单
    MenuItem menuItem = null;
    UuCommon.WebUrl carGuideUrl = null;// 用车指南...
    UuCommon.WebUrl carConditionUrl = null;// 车辆反馈
    UuCommon.WebUrl carSafeUrl = null;// 保险故障


    // 取消订单的原因
    private List<String> cancelReasonList = new ArrayList<>();
    // 取消订单弹窗
    private CancelOrderDialog mCancelOrderDialog;
    // 开车门弹窗
    private Dialog openDoorDialog = null;


    // ################  按钮点击事件 start #######################
    /**
     * 开车门按钮点击事件
     */
    @OnClick(R.id.open_car_door)
    public void openCarDorClick() {
        openCarDoor.setEnabled(false);
        checkLocationInfoMessage = getResources().getString(R.string.getcar_open_gps);
        if (Config.checkLocationInfo(mContext, checkLocationInfoMessage)) {
            String showMsg = "打开车门后，系统将开始计费。\n请勿提前开车门，以免被他人开走！";
            if (countDownType == COUNT_DOWN_PAY) {
                showMsg = "请勿提前开车门，以免被他人开走!";
            }
            openDoorDialog = Config.showMaterialDialog(mContext, null, showMsg,
                    "取消", "开车门", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openCarDoor.setEnabled(true);
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openCarDoorRequest();
                        }
                    });
        } else {
            openCarDoor.setEnabled(true);
        }
    }

    /**
     * 鸣笛找车按钮点击时间
     */
    @OnClick(R.id.find_car)
    public void findCarClick() {
        mFindCar.setEnabled(false);
        checkLocationInfoMessage = getResources().getString(R.string.getcar_findcar_gps);
        if (Config.checkLocationInfo(mContext, checkLocationInfoMessage)) {
            operateCar(OrderCommon.CarOperationType.SEARCH_CAR);
        } else {
            mFindCar.setEnabled(true);
        }
    }

    /**
     * 客服热线按钮点击事件
     */
    @OnClick(R.id.call_center)
    public void callCenterClick() {
        Config.callCenter(mContext);
    }

    /**
     * 取车路线按钮点击时间
     */
    @OnClick(R.id.navigation)
    public void navClick() {
        if (!Config.isNetworkConnected(mContext)) {
            showNetworkErrorSnackBarMsg();
            return;
        }
        MobclickAgent.onEvent(mContext, UMCountConstant.TAKE_CAR_NAVI);
        if (parkingInfo != null) {
            // 判断是否安装了百度和高德地图
            L.i("百度地图是否安装：" + Config.isAvilible(mContext, NavMapUtil.BAIDU_PACKAGE));
            L.i("高德地图书否安装：" + Config.isAvilible(mContext, NavMapUtil.GAODE_PACKAGE));
            boolean isBaiduOk = Config.isAvilible(mContext, NavMapUtil.BAIDU_PACKAGE);
            boolean isGaodeOk = Config.isAvilible(mContext, NavMapUtil.GAODE_PACKAGE);
            if (isBaiduOk || isGaodeOk) {
                NavMapUtil.showNavSelectDialog(mContext, parkingInfo, "选择导航", isBaiduOk, isGaodeOk, NavMapUtil.TYPE_WALK);
                return;
            }
            Intent intent = new Intent(mContext, StartNaviActivity.class);
            String lat = parkingInfo.getLatlon().getLat();
            String lon = parkingInfo.getLatlon().getLon();
            NaviLatLng endLatLng = new NaviLatLng(Double.parseDouble(lat), Double.parseDouble(lon));
            intent.putExtra(IntentConfig.KEY_END_LAT_LNG, endLatLng);
            intent.putExtra(IntentConfig.KEY_NAV_TYPE, StartNaviActivity.TYPE_WALK);
            startActivity(intent);
        }
    }

    // #############  按钮点击事件 end #####################




    // ############   生命周期方法 start #####################
    @Override
    public View setView(LayoutInflater inflater, ViewGroup container,
                        Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_get_car, container, false);
        ButterKnife.inject(this, rootView);
        orderId = Config.getOrderId(mContext);

        mCancelOrderDialog = new CancelOrderDialog(mContext);
        // 第一次进入取车页面进行打点
        MobclickAgent.onEvent(mContext, UMCountConstant.ENTER_TAKE_CAR_PAGE);
        // 初始化蒙层
        GetcarLayerUtil.initLayer(mContext, isAdded(), SPConstant.SPNAME_GETCAR_FIRST, SPConstant.SPKEY_GETCAR_FIRST);
        // 获取网络数据
        getData();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        isOnStop = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        isOnStop = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        L.i("开始执行。。。GetCarFragment.onDestroyView方法");
        // 取消timeCount
        cancelTimeCount();

        if (overTimeThread != null) {
            overTimeThread.interrupt();
            overTimeThread = null;
        }

        if (handler != null) {
            handler.removeMessages(HANDLER_MSG_WHAT);
        }

        // 若当前选择导航弹出框还在弹出状态，则关闭
        if (NavMapUtil.dialog != null && NavMapUtil.dialog.isShowing()) {
            NavMapUtil.dialog.dismiss();
            DialogUtil.getInstance(mContext).closeDialog();
        }
        // 关闭弹出层
        GetcarLayerUtil.removeLayer(mContext);
        // 关闭menu菜单选择
        PopupMenuManager.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Config.SETTINGGPS) {
            Config.checkLocationInfo(mContext, checkLocationInfoMessage);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_get_car, menu);
        menuItem = menu.findItem(R.id.getcar_menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem mItem = menu.findItem(R.id.getcar_menu);
        if (mItem != null) {
            if (carConditionUrl != null) {
                mItem.setVisible(true);
            } else {
                mItem.setVisible(false);
            }
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.getcar_menu) {
            View view = mContext.findViewById(R.id.getcar_menu);
            // 初始化menu显示
            PopupMenuManager.initPupopWindow(mContext, PopupMenuManager.GETCAR_SHOW,
                    view, carConditionUrl, carGuideUrl, carSafeUrl, mCancelOrderDialog);
        }
        return super.onOptionsItemSelected(item);
    }

    // ################## 生命周期方法结束 #########################


    /**
     * 网络请求失败，点击重新加载
     */
    private OnClickNormalListener onClickNormalListener = new OnClickNormalListener() {
        @Override
        public void onNormalClick(View v) {
            getData();
        }
    };

    /**
     * 处理eventbus请求
     * @param event
     */
    @Override
    public void onEventMainThread(BaseEvent event) {
        super.onEventMainThread(event);
        if (event == null)
            return;
        if (EventBusConstant.EVENT_TYPE_ASYNC_RESULT.equals(event.getType())) {
            int result = (int) event.getExtraData();
            if (result == EventBusConstant.EVENT_TYPE_RESULT_FAILIE) {
                Config.showToast(mContext, "操作失败，请重试");
            }

            // 拉取消息成功之后超时标示设置为false（开关车门等异步操作需要此逻辑）
            Config.timeout = false;
            resetButtonEnabled();
        }
        // 接收取消订单事件
        else if (EventBusConstant.EVENT_TYPE_CANCEL_ORDER.equals(event.getType())) {
            Object extraData = event.getExtraData();
            if (extraData != null && extraData instanceof List) {
                cancelReasonList.clear();
                cancelReasonList.addAll((List<String>) extraData);
            }
            cancelOrder(false, 1);
        }
        // 关闭取消订单弹窗事件
        else if (EventBusConstant.EVENT_TYPE_CLOSE_CANCEL_DIALOG.equals(event.getType())) {
            if (mCancelOrderDialog != null) {
                mCancelOrderDialog.dismissCancelOrderDialog();
            }
        }
        // 更新timer对象
        else if (EventBusConstant.EVENT_TYPE_UPDATE_TIMECOUNT.equals(event.getType())) {
            timeCount = (TimeCount) event.getExtraData();
        }
        // 取消timer
        else if (EventBusConstant.EVENT_TYPE_CANCEL_TIMECOUNT.equals(event.getType())) {
            L.i("接收到取消timer的关闭,执行取消timer的操作...");
            cancelTimeCount();
        }
    }


    // ################## 请求网络获取数据 #########################

    /**
     * 请求网络获取初始化数据
     */
    private void getData() {
        mProgressLayout.showLoading();
        OrderInterface.QueryUnderWayOrder.Request.Builder request = OrderInterface.QueryUnderWayOrder.Request.newBuilder();
        String orderId = Config.getOrderId(mContext);
        if (!TextUtils.isEmpty(orderId)) {
            request.setOrderId(orderId);
        }
        if (TextUtils.isEmpty(Config.cityCode)) {
            Config.cityCode = "010";
        }
        request.setCityCode(Config.cityCode);

        NetworkTask task = new NetworkTask(Cmd.CmdCode.QueryUnderWayOrderInfo_NL_VALUE);
        task.setTag("QueryUnderWayOrder");
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    showResponseCommonMsg(responseData.getResponseCommonMsg());
                    try {
                        OrderInterface.QueryUnderWayOrder.Response response = OrderInterface.QueryUnderWayOrder.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            if (!isAdded()) {
                                return;
                            }
                            if (menuItem != null) {
                                menuItem.setVisible(true);
                            }
                            // 请求成功，显示menu菜单，保存车况反馈和用车指南URL
                            carGuideUrl = response.getUnderWayOrderInfo().getCarGuideUrl();
                            carConditionUrl = response.getUnderWayOrderInfo().getCarConditionUrl();
                            carSafeUrl = response.getUnderWayOrderInfo().getFailSafeUrl();

                            OrderCommon.OrderDetailInfo orderDetailInfo = response.getUnderWayOrderInfo().getOrderDetailInfo();
                            UUApp.getInstance().display(orderDetailInfo.getCarImgUrl(), carImg, R.mipmap.ic_car_unload_details);
                            parkingInfo = orderDetailInfo.getGetParkingInfo();
                            if (parkingInfo.getCarParkingName() != null && text1 != null) {
                                text1.setText(parkingInfo.getCarParkingName());
                            }
                            if (parkingInfo.getParkingAddress() != null && text2 != null) {
                                text2.setText(parkingInfo.getParkingAddress());
                            }
                            if (parkingInfo.getCarDetailAddress() != null && parkNumber != null) {
                                parkNumber.setText(parkingInfo.getCarDetailAddress());
                            }
                            img.setBackgroundResource(R.mipmap.ic_location_addressbar);
                            String gearbox = "";
                            if (orderDetailInfo.getAutomaticTransmission() == 0) {
                                gearbox = "自动档";
                            } else {
                                gearbox = "手动档";
                            }
                            carName.setText(orderDetailInfo.getBrandName() + orderDetailInfo.getModelName() + " " + gearbox);
                            carNubmer.setText(orderDetailInfo.getPlateNumbe());
                            mProgressLayout.showContent();
                            // 初始化倒计时
                            timeCount = initCountDownTime(orderDetailInfo, new OnCancelOrder() {
                                @Override
                                public void oncancelOrder() {
                                    cancelOrder(true, 2);
                                }
                            }, waitPay, time, waitFeeText);

                        } else {
                            if (menuItem != null) {
                                menuItem.setVisible(false);
                            }
                            mProgressLayout.showError(onClickNormalListener);
                        }
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        if (menuItem != null) {
                            menuItem.setVisible(false);
                        }
                        mProgressLayout.showError(onClickNormalListener);
                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                if (menuItem != null) {
                    menuItem.setVisible(false);
                }
                mProgressLayout.showError(onClickNormalListener);
            }

            @Override
            public void networkFinish() {
            }
        });
    }


    /**
     * 执行网络请求，取消订单
     * @param showDialog
     * @param type
     */
    private void cancelOrder(final boolean showDialog, final int type) {
        L.i("开始执行请求取消订单操作......");
        final MainActivity mainActivity = (MainActivity) getActivity();
        if (!Config.isNetworkConnected(mContext)) {
            if (type == 2) {
                if (Config.outApp(mContext) || mainActivity == null || isOnStop) {
                    String showOverTimeStr = getShowOverTimeString();
                    EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_CANCEL_DIALOG, showOverTimeStr));
                    return;
                }
                // 显示超时dialog
                showOverTimeDialog(mainActivity, mCancelOrderDialog, openDoorDialog);
                return;
            }
        }
        showProgress(false);
        OrderInterface.CancelOrder.Request.Builder request = OrderInterface.CancelOrder.Request.newBuilder();
        request.setOrderId(orderId);
        request.setCancelType(type);
        request.addAllReasons(cancelReasonList);
        NetworkTask task = new NetworkTask(Cmd.CmdCode.CancelOrder_NL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {

            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    showResponseCommonMsg(responseData.getResponseCommonMsg());
                    try {
                        OrderInterface.CancelOrder.Response response = OrderInterface.CancelOrder.Response.parseFrom(responseData.getBusiData());
                        L.i("请求取消订单：type:" + type + " ret:" + response.getRet());
                        if (type == 2) {
                            L.i("outApp:" + Config.outApp(mContext) + "   mainAcitivity:" + (mainActivity == null) + "  isOnStop:" + isOnStop);
                            if (Config.outApp(mContext) || mainActivity == null || isOnStop) {
                                String showOverTimeStr = getShowOverTimeString();
                                EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_CANCEL_DIALOG, showOverTimeStr));
                                return;
                            }
                            // 显示超时dialog
                            showOverTimeDialog(mainActivity, mCancelOrderDialog, openDoorDialog);
                            return;
                        } else if (response.getRet() == 0) {
                            // 取消timeCount
                            cancelTimeCount();

                            if (!showDialog) {
                                Intent intent = new Intent(mContext, MainActivity.class);
                                intent.putExtra("goto", MainActivity.GOTO_MAP);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                            } else {
                                if (Config.outApp(mContext) || mainActivity == null || isOnStop) {
                                    String showOverTimeStr = getShowOverTimeString();
                                    EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_CANCEL_DIALOG, showOverTimeStr));
                                    return;
                                }
                                // 显示超时dialog
                                showOverTimeDialog(mainActivity, mCancelOrderDialog, openDoorDialog);
                            }
                        }

                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                showNetworkErrorSnackBarMsg();
            }

            @Override
            public void networkFinish() {
                dismissProgress();
            }
        });
    }

    /**
     * 开车门网络请求
     */
    private void openCarDoorRequest() {
        // 友盟bug，倒计时结束开始跳转，这时候点击开车门可能会发生not attached to activity的崩溃...
        if (isAdded()) {
            showProgress(false, getResources().getString(R.string.getcar_opening_car));
            Config.getCoordinates(mContext, new LocationListener() {
                @Override
                public void locationSuccess(double lat, double lng, String addr) {
                    if (lat != 0 && lng != 0) {
                        UsercarInterface.OpenTheDoor.Request.Builder request = UsercarInterface.OpenTheDoor.Request.newBuilder();
                        request.setOrderId(orderId);
                        UuCommon.LatLon.Builder builder = UuCommon.LatLon.newBuilder();
                        builder.setLon("" + lng);
                        builder.setLat("" + lat);
                        request.setLatLong(builder.build());
                        NetworkTask task = new NetworkTask(Cmd.CmdCode.OpenTheDoor_NL_VALUE);
                        task.setBusiData(request.build().toByteArray());
                        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
                            @Override
                            public void onSuccessResponse(UUResponseData responseData) {
                                if (responseData.getRet() == 0) {
                                    try {
                                        showResponseCommonMsg(responseData.getResponseCommonMsg());
                                        UsercarInterface.OpenTheDoor.Response response = UsercarInterface.OpenTheDoor.Response.parseFrom(responseData.getBusiData());
                                        if (response.getRet() == 0) {
                                            // 添加超时逻辑判断
                                            isOverTime();
                                            // 开车门请求服务器成功之后暂时不在取消倒计时
                                            // 取消timeCount
                                            // cancelTimeCount();
                                        } else {
                                            resetButtonEnabled();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        resetButtonEnabled();
                                    }
                                } else {
                                    resetButtonEnabled();
                                }
                            }

                            @Override
                            public void onError(VolleyError errorResponse) {
                                showNetworkErrorSnackBarMsg();
                                resetButtonEnabled();
                            }

                            @Override
                            public void networkFinish() {
                            }
                        });
                    } else {
                        showNetworkErrorSnackBarMsg();
                        resetButtonEnabled();
                    }
                }
            });
        }
    }


    /**
     * 请求网络，鸣笛找车
     * @param type
     */
    private void operateCar(final OrderCommon.CarOperationType type) {
        showProgress(false, getResources().getString(R.string.getcar_operatoring_car));
        // 鸣笛打点
        MobclickAgent.onEvent(mContext, UMCountConstant.HONKING);
        Config.getCoordinates(mContext, new LocationListener() {
            @Override
            public void locationSuccess(double lat, double lng, String addr) {

                UsercarInterface.FindCar.Request.Builder request = UsercarInterface.FindCar.Request.newBuilder();
                UuCommon.LatLon.Builder latLon = UuCommon.LatLon.newBuilder();
                latLon.setLat(lat + "");
                latLon.setLon(lng + "");
                request.setLatLong(latLon);
                request.setOrderId(orderId);
                NetworkTask task = new NetworkTask(Cmd.CmdCode.FindCar_NL_VALUE);
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
                                UsercarInterface.FindCar.Response response = UsercarInterface.FindCar.Response.parseFrom(responseData.getBusiData());

                                if (response.getRet() == 0) {
                                    // 添加超时逻辑判断
                                    isOverTime();
                                } else {
                                    resetButtonEnabled();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                showNetworkErrorSnackBarMsg();
                                resetButtonEnabled();
                            }
                        } else {

                            resetButtonEnabled();
                        }
                    }

                    @Override
                    public void onError(VolleyError errorResponse) {
                        showNetworkErrorSnackBarMsg();
                        resetButtonEnabled();
                    }

                    @Override
                    public void networkFinish() {
                    }
                });
            }
        });
    }

    /**
     * 初始化timeCount对象
     */
    private void cancelTimeCount() {
        if (timeCount != null) {
            L.i("执行初始化timeCount，清空timeCount操作...");
            timeCount.cancel();
            timeCount = null;
        }
    }

    /**
     * 重置按钮可点状态
     */
    private void resetButtonEnabled() {
        dismissProgress();
        openCarDoor.setEnabled(true);
        mFindCar.setEnabled(true);
    }
}
