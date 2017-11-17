package com.youyou.uuelectric.renter.UI.main.rentcar;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.navi.model.NaviLatLng;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rey.material.widget.Button;
import com.umeng.analytics.MobclickAgent;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.base.common.UuCommon;
import com.uu.facade.dot.protobuf.iface.DotInterface;
import com.uu.facade.order.pb.bean.OrderInterface;
import com.uu.facade.order.pb.common.OrderCommon;
import com.uu.facade.usecar.protobuf.bean.BoxControlOrder;
import com.uu.facade.usecar.protobuf.iface.UsercarInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Network.listen.OnClickFastListener;
import com.youyou.uuelectric.renter.Network.listen.OnClickNormalListener;
import com.youyou.uuelectric.renter.Network.user.SPConstant;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.main.MsgHandler;
import com.youyou.uuelectric.renter.UI.main.StartNaviActivity;
import com.youyou.uuelectric.renter.UI.mapsearch.AddressSelectForMapActivity;
import com.youyou.uuelectric.renter.UI.web.H5FragmentDialogFactory;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.DialogUtil;
import com.youyou.uuelectric.renter.Utils.SharedPreferencesUtil;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.Support.LocationListener;
import com.youyou.uuelectric.renter.Utils.UMCountConstant;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;
import com.youyou.uuelectric.renter.Utils.map.NavMapUtil;
import com.youyou.uuelectric.renter.Utils.popupwindow.PopupMenuManager;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 当前行程页面
 */
public class CurrentStrokeFragment extends CurrentStrokePresenter implements View.OnClickListener {

    View rootView;
    @InjectView(R.id.car_img)
    NetworkImageView carImg;
    @InjectView(R.id.rl)
    RelativeLayout rl;
    @InjectView(R.id.car_name)
    TextView carName;
    @InjectView(R.id.gear_box)
    TextView gearBox;
    @InjectView(R.id.car_name_root)
    LinearLayout carNameRoot;
    @InjectView(R.id.user_mileage_text)
    TextView userMileageText;
    @InjectView(R.id.user_mileage)
    TextView userMileage;
    @InjectView(R.id.user_time_text)
    TextView userTimeText;
    @InjectView(R.id.user_time)
    TextView userTime;
    @InjectView(R.id.user_price_text)
    TextView userPriceText;
    @InjectView(R.id.user_price)
    TextView userPrice;
    @InjectView(R.id.info_root)
    LinearLayout infoRoot;
    @InjectView(R.id.car_root)
    RelativeLayout carRoot;
    @InjectView(R.id.tv_change_return_car_address)
    TextView changeCarAddressText;
    @InjectView(R.id.ll_change_return_car_address)
    LinearLayout llChangeCarAddressText;
    @InjectView(R.id.navigation)
    LinearLayout navigation;
    @InjectView(R.id.img)
    ImageView img;
    @InjectView(R.id.text1)
    TextView text1;
    @InjectView(R.id.text2)
    TextView text2;
    @InjectView(R.id.park_number)
    TextView parkNumber;
    @InjectView(R.id.remote_park_price)
    TextView remoteParkPrice;
    @InjectView(R.id.park_desc)
    LinearLayout parkDesc;
    @InjectView(R.id.b3_button)
    Button b3Button;
    @InjectView(R.id.call_center)
    TextView callCenter;
    @InjectView(R.id.car_nubmer)
    TextView carNumber;
    @InjectView(R.id.remote_park_price)
    TextView remoteParkPriceText;
    @InjectView(R.id.img_car_tips)
    ImageView carTips;
    @InjectView(R.id.folding_car_control)
    LinearLayout mFoldingCarControl;
    @InjectView(R.id.car_control_lock)
    ImageView mCarControlLock;
    @InjectView(R.id.car_control_unlock)
    ImageView mCarControlUnlock;
    @InjectView(R.id.car_control_voice)
    ImageView mCarControlVoice;
    @InjectView(R.id.car_control_key)
    ImageView mCarControlKey;
    public String orderId;
    // 超时handler消息what
    public static final int HANDLER_MSG_WHAT = 1;
    private String checkLocationInfoMessage = "";
    @InjectView(R.id.current_top_tip)
    RelativeLayout currentToptip;
    @InjectView(R.id.current_top_tip_btn)
    TextView currentToptipBtn;
    @InjectView(R.id.current_top_tip_msg)
    TextView currentToptipMsg;
    private OrderCommon.ParkingInfo parkingInfo;
    //车辆唯一标识 carId，从行程中接口获取，在提交评价时使用。
    private String carId;


    MenuItem menuItem = null;// menu菜单
    UuCommon.WebUrl carGuideUrl = null;// 用车指南...
    UuCommon.WebUrl carConditionUrl = null;// 车辆反馈
    UuCommon.WebUrl carSafeUrl = null;// 车况故障

    public Timer timer;
    public Task timerTask;
    public boolean isFirst = true;
    private boolean isShow=false;

    // ################### 按钮点击事件start #############################
    /**
     * 客服热线点击事件
     */
    @OnClick(R.id.call_center)
    public void callCenterClick() {
        Config.callCenter(mContext);
    }

    /**
     * 导航按钮点击事件
     */
    @OnClick(R.id.navigation)
    public void navClick() {
        if (!Config.isNetworkConnected(mContext)) {
            showNetworkErrorSnackBarMsg();
            return;
        }
        MobclickAgent.onEvent(mContext, UMCountConstant.RETURN_CAR_NAVI);
        if (parkingInfo != null) {
            // 判断是否安装了百度和高德地图
            boolean isBaiduOk = Config.isAvilible(mContext, NavMapUtil.BAIDU_PACKAGE);
            boolean isGaodeOk = Config.isAvilible(mContext, NavMapUtil.GAODE_PACKAGE);
            if (isBaiduOk || isGaodeOk) {
                NavMapUtil.showNavSelectDialog(mContext, parkingInfo, "选择导航", isBaiduOk, isGaodeOk, NavMapUtil.TYPE_DRIVE);
                return;
            }
            Intent intent = new Intent(mContext, StartNaviActivity.class);
            String lat = parkingInfo.getLatlon().getLat();
            String lon = parkingInfo.getLatlon().getLon();
            NaviLatLng endLatLng = new NaviLatLng(Double.parseDouble(lat), Double.parseDouble(lon));
            intent.putExtra(IntentConfig.KEY_END_LAT_LNG, endLatLng);
            intent.putExtra(IntentConfig.KEY_NAV_TYPE, StartNaviActivity.TYPE_DRIVE);
            startActivity(intent);
        }
    }

    /**
     * 更改还车地点点击事件
     */
    @OnClick(R.id.tv_change_return_car_address)
    public void changeCarAddressClick() {
        if (parkingInfo != null) {
            Intent intent = new Intent(mContext, AddressSelectForMapActivity.class);
            intent.putExtra(IntentConfig.GET_DOT_TYPE, 2);
            startActivity(intent);
        }

    }

    /**
     * 我要换车按钮点击事件
     */
    public void returnCarClick() {
        Intent intent = new Intent(mContext, ReturnCarActivity.class);
        if (carGuideUrl != null) {
            intent.putExtra(IntentConfig.CAR_GUIDE_TITLE, carGuideUrl.getTitle());
            intent.putExtra(IntentConfig.CAR_GUIDE_URL, carGuideUrl.getUrl());
        }
        if (carConditionUrl != null) {
            intent.putExtra(IntentConfig.CAR_FEEDBACK_TITLE, carConditionUrl.getTitle());
            intent.putExtra(IntentConfig.CAR_FEEDBACK_URL, carConditionUrl.getUrl());
        }
        if (carSafeUrl != null) {
            intent.putExtra(IntentConfig.CAR_SAFE_TITLE, carSafeUrl.getTitle());
            intent.putExtra(IntentConfig.CAR_SAFE_URL, carSafeUrl.getUrl());
        }
        startActivity(intent);
    }


    // #################### 按钮点击事件end ##############################




    // #################### 生命周期方法start ################################
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.inject(this, rootView);

        return rootView;
    }

    @Override
    public View setView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_current_stroke, null);
        ButterKnife.inject(this, rootView);
        b3Button.setText(getString(R.string.return_car_button));
        orderId = Config.getOrderId(mContext);
        cancelGetCarTimeCount();
        img.setBackgroundResource(R.mipmap.ic_location_addressbar);
        initmFoldingCarControl();
        carTips.setOnClickListener(new OnClickFastListener() {
            @Override
            public void onFastClick(View v) {
                H5FragmentDialogFactory.createFactory((FragmentActivity) mContext).showH5Dialog(carGuideUrl.getUrl());
            }
        });

        rl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mCarControlLock.setVisibility(View.GONE);
                mCarControlUnlock.setVisibility(View.GONE);
                mCarControlVoice.setVisibility(View.GONE);
                isShow=false;
                return false;
            }
        });
        initView();

        return rootView;
    }


    /**
     * 若取车页面倒计时没有结束,则发送广播结束timer
     */
    private void cancelGetCarTimeCount() {
        BaseEvent baseEvent = new BaseEvent(EventBusConstant.EVENT_TYPE_CANCEL_TIMECOUNT, "");
        EventBus.getDefault().post(baseEvent);
    }

    /**
     * 折叠汽车控制菜单初始化
     */
    private void initmFoldingCarControl() {

        mCarControlKey.setOnClickListener(this);
        mCarControlLock.setOnClickListener(this);
        mCarControlUnlock.setOnClickListener(this);
        mCarControlVoice.setOnClickListener(this);
        mFoldingCarControl.setOnClickListener(new OnClickFastListener() {
          @Override
          public void onFastClick(View v) {
              if(!isShow){
                  mCarControlLock.setVisibility(View.VISIBLE);
                  mCarControlUnlock.setVisibility(View.VISIBLE);
                  mCarControlVoice.setVisibility(View.VISIBLE);
                  isShow=true;
              }else {
                  mCarControlLock.setVisibility(View.GONE);
                  mCarControlUnlock.setVisibility(View.GONE);
                  mCarControlVoice.setVisibility(View.GONE);
                  isShow=false;
              }
          }
      });
    }




    /**
     *  折叠图片点击事件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.car_control_key:
                if(!isShow){
                    mCarControlLock.setVisibility(View.VISIBLE);
                    mCarControlUnlock.setVisibility(View.VISIBLE);
                    mCarControlVoice.setVisibility(View.VISIBLE);
                    isShow=true;
                }else {
                    mCarControlLock.setVisibility(View.GONE);
                    mCarControlUnlock.setVisibility(View.GONE);
                    mCarControlVoice.setVisibility(View.GONE);
                    isShow=false;
                }
                break;
            case R.id.car_control_unlock:
                tripOpenCarDoor();

                break;
            case R.id.car_control_lock:
                tripLockCarDoor();
                break;
            case R.id.car_control_voice:
                tripFindCarClick();
                break;
        }
    }

    /**
     * 行程中关车门
     */
    private void tripLockCarDoor(){
        if (!Config.isNetworkConnected(mContext)) {
            Config.showToast(mContext, getResources().getString(R.string.network_error_tip));
            mCarControlLock.setEnabled(true);
            dismissProgress();
           return;
        }
        Config.showProgressDialog(mContext, false, null, "正在锁车门，请稍候");
        Config.getCoordinates(mContext, new LocationListener() {
            @Override
            public void locationSuccess(double lat, double lng, String addr) {
              if(lat !=0 && lng != 0){
                  BoxControlOrder.CloseCarDoorDuringOrderMessage.Request.Builder request = BoxControlOrder.CloseCarDoorDuringOrderMessage.Request.newBuilder();
                  request.setOrderId(orderId);
                  UuCommon.LatLon.Builder builder = UuCommon.LatLon.newBuilder();
                  builder.setLat(""+ lat);
                  builder.setLon(""+ lng);
                  request.setLatLong(builder.build());
                  NetworkTask task = new NetworkTask(Cmd.CmdCode.CloseDoorDuringOrder_NL_VALUE);
                  task.setBusiData(request.build().toByteArray());
                  NetworkUtils.executeNetwork(task ,new HttpResponse.NetWorkResponse<UUResponseData>(){

                      @Override 
                      public void onSuccessResponse(UUResponseData responseData) {
                           if(responseData.getRet() == 0){
                               try{
                                   BoxControlOrder.CloseCarDoorDuringOrderMessage.Response response = BoxControlOrder.CloseCarDoorDuringOrderMessage.Response.parseFrom(
                                           responseData.getBusiData()
                                   );
                                   if(response.getRetStatus() == 0){
                                       //添加超时判断
                                       isOverTime();
                                   }else {
                                       showResponseCommonMsg(responseData.getResponseCommonMsg());
                                       dismissProgress();
                                   }
                               }catch (Exception e){
                                   e.printStackTrace();
                                   showNetworkErrorSnackBarMsg();
                                   dismissProgress();
                               }

                           }
                      }

                      @Override
                      public void onError(VolleyError errorResponse) {
                          showNetworkErrorSnackBarMsg();
                          dismissProgress();
                      }

                      @Override
                      public void networkFinish() {
                          // dismissProgress();

                      }
                  });
              }
            }
        });

    }

    // 开车门弹窗
    private Dialog openDoorDialog = null;
    /**
     *    行程中 开车门
     */
    private void  tripOpenCarDoor(){
        checkLocationInfoMessage = getResources().getString(R.string.getcar_open_gps);
        if (Config.checkLocationInfo(mContext, checkLocationInfoMessage)) {
            tripOpenCarDoorRequest();
        }

    }


    /**
     * 行程中鸣笛找车事件
      */
    private void tripFindCarClick(){

        //核查定位信息，是否开启GPS，是否使用了模拟定位
        checkLocationInfoMessage = getResources().getString(R.string.getcar_findcar_gps);
        if(Config.checkLocationInfo(mContext,checkLocationInfoMessage)){
            TripoperateCar(OrderCommon.CarOperationType.SEARCH_CAR);
        }
    }

    /**
     * 行程中，开车门请求
     */
    private  void  tripOpenCarDoorRequest(){
        // 友盟bug，倒计时结束开始跳转，这时候点击开车门可能会发生not attached to activity的崩溃..
        if (isAdded()){
            showProgress(false,getResources().getString(R.string.getcar_opening_car));
            Config.getCoordinates(mContext, new LocationListener() {
                @Override
                public void locationSuccess(double lat, double lng, String addr) {
                    if(lat != 0 && lng != 0){
                        BoxControlOrder.OpenCarDoorDuringOrderMessage.Request.Builder request = BoxControlOrder.OpenCarDoorDuringOrderMessage.Request.newBuilder();
                        request.setOrderId(orderId);
                        UuCommon.LatLon.Builder latLon = UuCommon.LatLon.newBuilder();
                        latLon.setLat(lat + "");
                        latLon.setLon(lng + "");
                        request.setLatLong(latLon.build());
                        NetworkTask task = new NetworkTask(Cmd.CmdCode.OpenDoorDuringOrder_NL_VALUE);
                        task.setBusiData(request.build().toByteArray());
                        NetworkUtils.executeNetwork(task,new HttpResponse.NetWorkResponse<UUResponseData>() {

                            @Override
                            public void onSuccessResponse(UUResponseData responseData) {
                                        if(responseData.getRet() == 0){
                                    try{
                                        BoxControlOrder.OpenCarDoorDuringOrderMessage.Response response = BoxControlOrder.OpenCarDoorDuringOrderMessage.Response.parseFrom(
                                           responseData.getBusiData()
                                        );
                                        if(response.getRetStatus() == 0){
                                            isOverTime();//超时逻辑判断
                                        }else {
                                            showResponseCommonMsg(responseData.getResponseCommonMsg());
                                            dismissProgress();
                                        }
                                    } catch (Exception e){
                                        e.printStackTrace();
                                        showNetworkErrorSnackBarMsg();
                                        dismissProgress();
                                    }
                                 }
                            }

                            @Override
                            public void onError(VolleyError errorResponse) {
                                showNetworkErrorSnackBarMsg();
                                dismissProgress();
                            }

                            @Override
                            public void networkFinish() {
                                    // dismissProgress();
                            }
                        });

                    }
                }
            });
        }
    }

    /**
     * 当前行程，
     * 请求网络，鸣笛找车
     * @param
     */
    private void TripoperateCar(final OrderCommon.CarOperationType type){
       showProgress(false,getResources().getString(R.string.getcar_operatoring_car));
        // 鸣笛打点
        MobclickAgent.onEvent(mContext, UMCountConstant.HONKING);
        Config.getCoordinates(mContext, new LocationListener() {
            @Override
            public void locationSuccess(double lat, double lng, String addr) {
                BoxControlOrder.FindCarDuringOrderMessage.Request.Builder request = BoxControlOrder.FindCarDuringOrderMessage.Request.newBuilder();
                UuCommon.LatLon.Builder latLon = UuCommon.LatLon.newBuilder();
                latLon.setLat(lat + "");
                latLon.setLon(lng + "");
                request.setLatLong(latLon.build());
                request.setOrderId(orderId);
                NetworkTask task = new NetworkTask(Cmd.CmdCode.FindCarDuringOrder_NL_VALUE);
                task.setBusiData(request.build().toByteArray());
                NetworkUtils.executeNetwork(task,new HttpResponse.NetWorkResponse<UUResponseData>(){

                    @Override
                    public void onSuccessResponse(UUResponseData responseData) {
                        if(responseData.getRet() == 0){
                           try{
                               if(!isAdded()){
                                   return;
                               }

                               BoxControlOrder.FindCarDuringOrderMessage.Response response = BoxControlOrder.FindCarDuringOrderMessage.Response.parseFrom(
                               responseData.getBusiData());

                             if(response.getRetStatus() == 0 ){
                                   isOverTime();
                             }else {
                                 showResponseCommonMsg(responseData.getResponseCommonMsg());
                                 dismissProgress();
                             }
                           }catch (Exception e){
                               e.printStackTrace();
                               showNetworkErrorSnackBarMsg();
                               dismissProgress();
                           }
                        }
                    }

                    @Override
                    public void onError(VolleyError errorResponse) {
                        showNetworkErrorSnackBarMsg();
                        dismissProgress();
                    }

                    @Override
                    public void networkFinish() {
                        // dismissProgress();
                    }
                });
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Config.isNetworkConnected(mContext)) {
            String msg = getResources().getString(R.string.network_error_tip);
            Config.showToast(mContext, msg);
            return;
        }
        if (isFirst) {
            mProgressLayout.showLoading();
            getData(true);
            isFirst = false;
        } else {
            showProgress(false);
            getData(true);
        }

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_current_stroke, menu);
        menuItem = menu.findItem(R.id.current_stroke_menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem mItem = menu.findItem(R.id.current_stroke_menu);
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
        if (item.getItemId() == R.id.current_stroke_menu) {
            View view = mContext.findViewById(R.id.current_stroke_menu);
            // 显示popupwindow菜单
            PopupMenuManager.initPupopWindow(mContext, PopupMenuManager.CURRENT_SHOW, view, carConditionUrl, carGuideUrl, carSafeUrl, null);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        // 关闭日结弹窗
        dismissDialog();
        // 若当前选择导航弹出框还在弹出状态，则关闭
        DialogUtil.getInstance(mContext).closeDialog();
        // 关闭menu菜单选择
        PopupMenuManager.dismiss();
    }

    // ######################### 生命周期方法 end ##########################


    // ######################## 其他网络请求或者初始化事件 ####################
    /**
     * 接收EventBus事件通知
     * @param event
     */
    @Override
    public void onEventMainThread(BaseEvent event) {
        super.onEventMainThread(event);

        // 处理网点列表选择后事件(常用网点， 附近网点，地图选择网点，地图列表网点)
        if (EventBusConstant.EVENT_TYPE_SELECTED_DOTINFO2.equals(event.getType())) {
            DotInterface.DotInfo dotInfo = (DotInterface.DotInfo) event.getExtraData();

            fillParkData(dotInfo.getDotName(), dotInfo.getDotDesc(), dotInfo.getDotSpace(), dotInfo.getBackCarFeeDesc());
            //如果选择的是本次网点还车，那么不执行之后的操作
            if (dotInfo.getDotId().equals(parkingInfo.getCarParkingId())) {
                return;
            }
            UuCommon.LatLon.Builder latLonBuilder = UuCommon.LatLon.newBuilder();
            latLonBuilder.setLat(dotInfo.getDotLat() + "");
            latLonBuilder.setLon(dotInfo.getDotLon() + "");
            //创建parkinginfo为了点击导航是传入相应的经纬度
            OrderCommon.ParkingInfo.Builder builder = OrderCommon.ParkingInfo.newBuilder();
            builder.setLatlon(latLonBuilder.build());
            builder.setCarParkingId(dotInfo.getDotId());
            builder.setParkingAddress(dotInfo.getDotDesc());
            builder.setCarDetailAddress(dotInfo.getDotDesc());
            parkingInfo = builder.build();

            changeBackDotNLFun(dotInfo);
        } else if (EventBusConstant.EVENT_TYPE_CLOSE_CANCEL_DIALOG.equals(event.getType())) {
            //关闭定时器
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (timerTask != null) {
                timerTask.cancel();
                timerTask = null;
            }
            // 关闭日结弹窗
            dismissDialog();
        }
        // 开关车门之后等待长连接结果
        else if (EventBusConstant.EVENT_TYPE_ASYNC_RESULT.equals(event.getType())) {
            int result = (int) event.getExtraData();
            if (result == EventBusConstant.EVENT_TYPE_RESULT_FAILIE) {
                Config.showToast(mContext, "操作失败，请重试");
            }

            // 拉取消息成功之后超时标示设置为false（开关车门等异步操作需要此逻辑）
            Config.timeout = false;
            dismissProgress();
        }
    }


    /**
     * 请求页面数据
     * @param isNeedOther
     */
    public void getData(final boolean isNeedOther) {
        if (Config.cityCode == null || Config.cityCode.isEmpty()) {
            Config.getCoordinates(mContext, new LocationListener() {
                @Override
                public void locationSuccess(double lat, double lng, String addr) {
                    loadInfo(isNeedOther);
                }
            });
        } else {
            loadInfo(isNeedOther);
        }
    }

    /**
     * 执行网络请求，初始化页面数据
     * @param isNeedOther
     */
    public void loadInfo(final boolean isNeedOther) {
        final OrderInterface.OnTripOrderInfo.Request.Builder request = OrderInterface.OnTripOrderInfo.Request.newBuilder();
        if (!TextUtils.isEmpty(orderId)) {
            request.setOrderId(orderId);
        }
        if (isNeedOther) {
            request.setIsDisplayDetailInfo(0);
        } else {
            request.setIsDisplayDetailInfo(1);
        }
        if (Config.cityCode == null || Config.cityCode.isEmpty()) {
            Config.cityCode = "010";
        }
        request.setCityCode(Config.cityCode);
        // 判断用户是否点击了四小时还车
        int spClickedInt = getSPIsClickedFourhourBtn(mContext, orderId);
        if (spClickedInt == -1) {
            L.i("request.setclickButton:" + NOTCLICKED_FOURHOUR);
            request.setClickButton(NOTCLICKED_FOURHOUR);
        } else {
            L.i("request.setclickButton:" + CLICKED_FOURHOUR);
            request.setClickButton(spClickedInt);
        }
        NetworkTask task = new NetworkTask(Cmd.CmdCode.QueryOnTripOrderInfo_NL_VALUE);
        task.setTag("QueryOnTripOrderInfo");
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    showResponseCommonMsg(responseData.getResponseCommonMsg());
                    try {
                        OrderInterface.OnTripOrderInfo.Response response = OrderInterface.OnTripOrderInfo.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            if (!isAdded()) {
                                return;
                            }
                            initView();
                            if (menuItem != null) {
                                menuItem.setVisible(true);
                            }
                            carGuideUrl = response.getCarGuideUrl();
                            carConditionUrl = response.getCarConditionUrl();
                            carSafeUrl = response.getFailSafeUrl();
                            if (isFirstGetData) {
                                // 判断是否是首单，是首单的话则弹出用车指南
                                if (response.getIsFirstOrder() == 1) {
                                    if (carGuideUrl != null && !TextUtils.isEmpty(carGuideUrl.getUrl())) {
                                        //车辆品牌+型号来判断不同车型
                                        final String key = response.getBrandName() + response.getModelName();
                                        if (H5FragmentDialogFactory.isShowUseCarTip(mContext, SPConstant.SPNAME_CURRENT_FIRST, key)) {
                                            //展示用车指南弹窗，并设置关闭弹窗时记录不在弹窗
                                            H5FragmentDialogFactory.createFactory((FragmentActivity) mContext).showH5Dialog(carGuideUrl.getUrl(), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    //关闭后本地记录，杀进程后再次进入不在弹窗（此时服务器任然会返回首单，因为没有完成订单）
                                                    H5FragmentDialogFactory.setShowUseCarTip(mContext, SPConstant.SPNAME_CURRENT_FIRST, key);
                                                    //这里去请求获取评价提示弹窗(或者不是首单的判断)
                                                    if (!orderId.equals("")) {
                                                        if (!SharedPreferencesUtil.getSharedPreferences(mContext).getBoolean("current_stroke_" + orderId, false)) {
                                                            getIsComment();
                                                        }
                                                    }
                                                }
                                            });
                                        }else{
                                            if (!orderId.equals("")) {
                                                if (!SharedPreferencesUtil.getSharedPreferences(mContext).getBoolean("current_stroke_" + orderId, false)) {
                                                    getIsComment();
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    //这里去请求获取评价提示弹窗
                                    if (!orderId.equals("")) {
                                        if (!SharedPreferencesUtil.getSharedPreferences(mContext).getBoolean("current_stroke_" + orderId, false)) {
                                            getIsComment();
                                        }
                                    }
                                }
                            }

                            carId = response.getCarId();
                            if (!TextUtils.isEmpty(response.getActualCost())) {
                                float result = Float.parseFloat(response.getActualCost());
                                userPrice.setText("￥" + String.format("%.2f", result));
                            } else {
                                userPrice.setText("￥0.0");
                            }
                            userMileage.setText(response.getMileage() + "公里");
                            userTime.setText(getTime(response.getDrivedTime()));

                            if (isNeedOther) {
                                UUApp.getInstance().display(response.getCarHeadUrl(), carImg, R.mipmap.ic_car_unload_details);

                                parkingInfo = response.getBackParkingInfo();


                                if (parkingInfo != null) {
                                    String parkName = parkingInfo.getCarParkingName();
                                    String parkAddress = parkingInfo.getParkingAddress();
                                    String carDetailAddress = parkingInfo.getCarDetailAddress();
                                    String backCarFeeDesc = parkingInfo.getBackCarFeeDesc();
                                    fillParkData(parkName, parkAddress, carDetailAddress, backCarFeeDesc);

                                    //是否全网开放异地还车:1-支持，2-不支持
                                    int flag = response.getIsAllDotsA2B();
                                    if (flag == 0 || flag == 1) {
                                        llChangeCarAddressText.setVisibility(View.VISIBLE);
                                    } else if (flag == 2) {
                                        llChangeCarAddressText.setVisibility(View.GONE);
                                    }
                                }

                                img.setBackgroundResource(R.mipmap.ic_location_addressbar);
                                carName.setText(response.getBrandName() + response.getModelName() + " " + response.getAutoTrassactionName());
                                carNumber.setText(response.getPlateNumbe() == null ? "" : response.getPlateNumbe());

                                // 解析日结信息
                                parserDayClearInfo(response, orderId, currentToptip, currentToptipBtn, currentToptipMsg);

                                //调整timer和timerTask，避免每次都重新创建
                                if (timer == null || timerTask == null) {
                                    if (timer != null) {
                                        timer.cancel();
                                        timer = null;
                                    }
                                    if (timerTask != null) {
                                        timerTask.cancel();
                                        timerTask = null;
                                    }
                                    timer = new Timer();
                                    timerTask = new Task();
                                    timer.schedule(timerTask, 60 * 1000, 60 * 1000);
                                }

                            }
                            mProgressLayout.showContent();
                        } else {
                            if (menuItem != null) {
                                menuItem.setVisible(false);
                            }
                            if (isFirstGetData) {
                                mProgressLayout.showError(onClickNormalListener);
                            }
                        }
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        if (menuItem != null) {
                            menuItem.setVisible(false);
                        }
                        if (isFirstGetData) {
                            mProgressLayout.showError(onClickNormalListener);
                        }
                    }
                } else {
                    if (menuItem != null) {
                        menuItem.setVisible(false);
                    }
                    if (isFirstGetData) {
                        mProgressLayout.showError(onClickNormalListener);
                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                if (isNeedOther) {
                    if (menuItem != null) {
                        menuItem.setVisible(false);
                    }
                    if (isFirstGetData) {
                        mProgressLayout.showError(onClickNormalListener);
                    }
                }
            }

            @Override
            public void networkFinish() {
                dismissProgress();
                if (isFirstGetData) {
                    new Handler().postDelayed(new Runnable(){
                        public void run() {
                            // 我要还车按钮点击事件处理（屏蔽多次点击）
                            b3Button.setOnClickListener(new OnClickFastListener() {
                                @Override
                                public void onFastClick(View v) {
                                    returnCarClick();
                                }
                            });
                        }
                    }, 800);
                }
            }
        });
    }

    /**
     * 填充部分页面数据
     * @param parkName
     * @param parkAddress
     * @param carDetailAddress
     * @param backCarFree
     */
    private void fillParkData(String parkName, String parkAddress, String carDetailAddress, String backCarFree) {
        text1.setText(parkName);
        text2.setText(parkAddress);
        if (TextUtils.isEmpty(carDetailAddress)) {
            parkNumber.setVisibility(View.GONE);
        } else {
            parkNumber.setText(carDetailAddress);
            parkNumber.setVisibility(View.VISIBLE);

        }
        if (TextUtils.isEmpty(backCarFree)) {
            remoteParkPrice.setVisibility(View.GONE);
        } else {
            remoteParkPrice.setText(backCarFree);
            remoteParkPrice.setVisibility(View.VISIBLE);

        }
    }


    /**
     * 传递更改的网点到服务器。
     *
     * @param dotInfo
     */
    private void changeBackDotNLFun(final DotInterface.DotInfo dotInfo) {
        DotInterface.ChangeBackDotNL.Request.Builder request = DotInterface.ChangeBackDotNL.Request.newBuilder();
        request.setBackParkingInfo(dotInfo);
        if (!TextUtils.isEmpty(orderId)) {
            request.setOrderId(orderId);
        }
        NetworkTask task = new NetworkTask(Cmd.CmdCode.ChangeBackDotNL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    showResponseCommonMsg(responseData.getResponseCommonMsg());
                    try {
                        DotInterface.ChangeBackDotNL.Response response = DotInterface.ChangeBackDotNL.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                        } else {
                            continueSubmit();
                        }
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                } else {
                    continueSubmit();
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                continueSubmit();
            }

            @Override
            public void networkFinish() {
            }

            /**
             * 5s后再次提交
             */
            private void continueSubmit() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        changeBackDotNLFun(dotInfo);
                    }
                }, 5000);
            }

        });

    }

    /**
     * 初始化view
     */
    public void initView() {
        {
            if (userPrice == null) {
                userPrice = (TextView) rootView.findViewById(R.id.user_price);
            }
            if (userMileage == null) {

                userMileage = (TextView) rootView.findViewById(R.id.user_mileage);
            }
            if (userTime == null) {
                userTime = (TextView) rootView.findViewById(R.id.user_time);
            }
            if (carImg == null) {
                carImg = (NetworkImageView) rootView.findViewById(R.id.car_img);
            }
            if (text1 == null) {
                text1 = (TextView) rootView.findViewById(R.id.text1);
            }
            if (text2 == null) {
                text2 = (TextView) rootView.findViewById(R.id.text2);
            }
            if (carName == null) {
                carName = (TextView) rootView.findViewById(R.id.car_name);
            }
            if (img == null) {
                img = (ImageView) rootView.findViewById(R.id.img);
            }
            if (parkNumber == null) {
                parkNumber = (TextView) rootView.findViewById(R.id.park_number);
            }
        }

    }

    /**
     * 错误页面
     */
    OnClickNormalListener onClickNormalListener = new OnClickNormalListener() {
        @Override
        public void onNormalClick(View v) {
            getData(true);
        }
    };


    /**
     * 将String（秒）时间转化我天时分
     * @param time
     * @return
     */
    public String getTime(String time) {
        long lt = Integer.parseInt(time) * 1000L;
        long day = lt / (1000 * 60 * 60 * 24);
        long hour = (lt - day * 1000 * 60 * 60 * 24) / (60 * 60 * 1000);
        long minute = (lt - hour * 60 * 60 * 1000) / (60 * 1000);
        long second = (lt - hour * 60 * 60 * 1000 - minute * 60 * 1000) / 1000;
        if (second >= 60) {
            second = second % 60;
            minute += second / 60;
        }
        if (minute >= 60) {
            minute = minute % 60;
            hour += minute / 60;
        }
        String sd = "";
        String sh = "";
        String sm = "";
        String returnTime = "";
        if (day > 0) {
            sd = String.valueOf(day);
            returnTime = sd + "天";
        }
        if (hour > 0) {
            sh = String.valueOf(hour);
            returnTime += sh + "小时";
        }
        // 若1天0分钟则显示1天
        if (minute > 0 || TextUtils.isEmpty(returnTime)) {
            sm = String.valueOf(minute);
            returnTime += sm + "分钟";
        }
        return returnTime;
    }

    //判断是否是第一次加载行程中数据
    private boolean isFirstGetData = true;

    public class Task extends TimerTask {
        @Override
        public void run() {
            L.i("开始执行执行timer定时任务...");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    isFirstGetData = false;
                    getData(true);
                }
            });
        }
    }

    /**
     * 提交吐槽原因
     *
     * @param evaluateType     评价类型
     * @param chooseReasonList 评价内容对象集合
     */
    public void submitUseCarEvaluate(final UsercarInterface.EvaluateType evaluateType, List<UsercarInterface.EvaluateTag> chooseReasonList) {
        showProgress(false);
        UsercarInterface.SaveUseCarEvaluate.Request.Builder request = UsercarInterface.SaveUseCarEvaluate.Request.newBuilder();
        if (!TextUtils.isEmpty(orderId)) {
            request.setOrderId(orderId);
        }
        request.setEvaluateScene(UsercarInterface.EvaluateScene.strokeing);
        request.setEvaluateType(evaluateType);
        if (!TextUtils.isEmpty(carId)) {
            request.setCarId(carId);
        }
        //添加吐槽原因id
        if (chooseReasonList != null) {
            for (int i = 0; i < chooseReasonList.size(); i++) {
                request.addEvaluateDesc(chooseReasonList.get(i).getId());
            }
        }

        NetworkTask task = new NetworkTask(Cmd.CmdCode.SaveUseCarEvaluate_NL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {

            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    //showResponseCommonMsg(responseData.getResponseCommonMsg());
                    try {
                        UsercarInterface.SaveUseCarEvaluate.Response response = UsercarInterface.SaveUseCarEvaluate.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            if (!UsercarInterface.EvaluateType.user_cancel.equals(evaluateType)) {
                                Config.showToast(mContext, response.getMsg());
                            }
                        } else {
                            Config.showToast(mContext, response.getMsg());
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
     * 判断该订单是否评论过，服务器返回// 0:成功   -1：失败,-2:记录为空
     */
    private void getIsComment() {
        showProgress(false);
        UsercarInterface.QueryOrderEvaluate.Request.Builder request = UsercarInterface.QueryOrderEvaluate.Request.newBuilder();
        if (!TextUtils.isEmpty(orderId)) {
            request.setOrderId(orderId);
        }
        NetworkTask task = new NetworkTask(Cmd.CmdCode.QueryOrderEvaluate_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {

                if (responseData.getRet() == 0) {
                    try {
                        UsercarInterface.QueryOrderEvaluate.Response response = UsercarInterface.QueryOrderEvaluate.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == -2) {
                            //记录为空，没有评论过
                            CommentCarDialog commentCarDialog = new CommentCarDialog(CurrentStrokeFragment.this);
                            commentCarDialog.showCommentCarDialog();
                        } else if (response.getRet() == 0) {
                            //已经评论过了
                        } else {
                            //返回失败
                        }
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
            }

            @Override
            public void networkFinish() {
                dismissProgress();
            }
        });
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

}
