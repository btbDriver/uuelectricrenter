package com.youyou.uuelectric.renter.UI.main;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviLatLng;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.protobuf.InvalidProtocolBufferException;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.dot.protobuf.iface.DotInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.MapIconLruImageCache;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Network.user.SPConstant;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseFragment;
import com.youyou.uuelectric.renter.UI.main.maptogether.MainMapListener;
import com.youyou.uuelectric.renter.UI.main.maptogether.MapTogetherManager;
import com.youyou.uuelectric.renter.UI.main.maptogether.NavMainListener;
import com.youyou.uuelectric.renter.UI.main.rentcar.MapConfirmCarFragment;
import com.youyou.uuelectric.renter.UI.nearstation.NearStationActivity;
import com.youyou.uuelectric.renter.UI.web.H5Activity;
import com.youyou.uuelectric.renter.UI.web.H5Constant;
import com.youyou.uuelectric.renter.UI.web.url.URLConfig;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.DialogUtil;
import com.youyou.uuelectric.renter.Utils.DisplayUtil;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.animation.EmptyAnimationListener;
import com.youyou.uuelectric.renter.Utils.animation.EmptyAnimatorListener;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import me.grantland.widget.AutofitTextView;

/**
 * User: qing
 * Date: 2015/9/8 18:27
 * Desc: 首页图片界面Fragment
 */
public class MainMapFragment extends BaseFragment {
    /**
     * 地图初始化比例尺,地图比例尺
     */
    public static float ORGZOON = 14;
    public static float INTZOOM = 14;

    @InjectView(R.id.mapView)
    MapView mMapView;
    @InjectView(R.id.ll_path_container)
    LinearLayout mLlPathContainer;
    @InjectView(R.id.fl_bottom_container)
    FrameLayout mFlBottomContainer;
    @InjectView(R.id.btn_location)
    ImageView mBtnLocation;

    //顶部地址栏相关元素
    @InjectView(R.id.fl_address_container)
    FrameLayout mFlAddressContainer;
    View mHasAddressView;
    AutofitTextView mTvNoAddressText;

    public AMap aMap;
    public FragmentActivity context;
    /**
     * 导航实体类
     */
    public AMapNavi mAMapNavi;

    // 起点终点坐标
    public NaviLatLng mNaviStart;
    public NaviLatLng mNaviEnd;
    /**
     * marker数据集合
     */
    public Map<String, Marker> markerMap = new ConcurrentHashMap<>();

    private View rootView;
    private LayoutInflater mLayoutInflater;
    private FragmentManager mFragmentManager;

    private boolean isDestroyView = false;
    /**
     * Marker默认的zIndex值
     */
    private static final int MARKER_NORMAL_ZINDEX = 5;
    /**
     * Marker选中的zIndex值
     */
    private static final int MARKER_SELECTED_ZINDEX = 8;
    /**
     * 定义地图的视角，放到到一定级别后会显示3D建筑
     */
    public static final int MAP_TILT = 3;
    /**
     * 是否是第一次定位
     */
    public boolean isFirstLocation = false;
    /**
     * 地图页面图标URL
     */
    private Map<DotInterface.ActivityIconType, String> mIconMap = null;

    /**
     * 地图监听,导航监听,聚合网点
     */
    private MainMapListener mainMapListener = null;
    private NavMainListener navMainListener = null;

    // -------------------------生命周期方法 start-----------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        mLayoutInflater = LayoutInflater.from(context);
        isFirstRequest = true;
        mFragmentManager = context.getSupportFragmentManager();
        isDestroyView = false;
        isFirstLocation = true;

    }


    @Nullable
    @Override
    public View setView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main_map, null);
        ButterKnife.inject(this, rootView);
        mMapView.onCreate(savedInstanceState);
        initMap();

        initTopAddress();

        return rootView;
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        dismissProgress();
    }

    /**
     * 上一次onStop的时间
     */
    private long lastStopTime;
    /**
     * 上一次Stop到再次start时的时间间隔，大于此时间间隔就执行一次刷新操作；
     */
    private static final int STOP_TIME_INTERVAL = 60000;

    @Override
    public void onStart() {
        super.onStart();

        startLoop();
        long currentTime = System.currentTimeMillis();
        if (lastStopTime > 0 && STOP_TIME_INTERVAL < currentTime - lastStopTime) {
            L.i("上次onStop到当前onStart的时间间隔大于60s,执行立即刷新动作");
            loopRequestHandler.sendEmptyMessage(LOOP_WHAT);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLoop();
        dismissProgress();

        lastStopTime = System.currentTimeMillis();
    }

    public void onEventMainThread(BaseEvent event) {
        if (EventBusConstant.EVENT_TYPE_NETWORK_STATUS.equals(event.getType())) {
            String status = (String) event.getExtraData();
            if (status != null && status.equals("open")) {
                L.i("网络已连接，拉取最新数据...");
                loopRequestHandler.sendEmptyMessage(LOOP_WHAT);
            }
        }
        // 处理网点列表选择后事件
        else if (EventBusConstant.EVENT_TYPE_SELECTED_DOTINFO.equals(event.getType())) {
            Object object = event.getExtraData();
            disposeSelectedDotInfoEvent(object);
        }
        // 接收底部车辆详情刷新消息，立即刷新网点数据
        else if (EventBusConstant.EVENT_TYPE_REFRESH_MAP_DOT.equals(event.getType())) {
            isCarDetailNotifyRefresh = true;
            loopRequestHandler.sendEmptyMessage(LOOP_WHAT);
        }
        // 接收网点列表刷新消息，立即刷新网点数据
        else if (EventBusConstant.EVENT_TYPE_REFRESH_DOT_FORM_DOT_LIST.equals(event.getType())) {
            loopRequestHandler.sendEmptyMessage(LOOP_WHAT);
        }
        // 下载完成更换的图片,发送通知事件更新图标
        else if (EventBusConstant.EVENT_TYPE_UPDATE_MAPICON.equals(event.getType())) {
            L.i("下载icon完成，更新地图页面所有icon...");
            isReceiveUpdateMapIconEvent = true;
            updateMapMarkers(null, false);
        }
        // 如果底部车请求卡片已经显示，则刷新底部车详情数据
        else if (EventBusConstant.EVENT_TYPE_UPDATE_CAR_DETAIL_BY_LOGIN.equals(event.getType())) {
            if (isShowBottomContent) {
                EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_REFRESH_CARDETAIL));
            }
        }
    }


    // 用于保存网点列表选择的网点信息
    private DotInterface.DotInfo selectDotInfo = null;
    /**
     * 处理网点列表点击后选中地图中的网点事件
     *
     * @param object
     */
    private void disposeSelectedDotInfoEvent(Object object) {
        if (object != null && object instanceof DotInterface.DotInfo) {
            final DotInterface.DotInfo dotInfo = (DotInterface.DotInfo) object;
            if (markerMap.containsKey(dotInfo.getDotId())) {
                final Marker marker = markerMap.get(dotInfo.getDotId());
                rootView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 判断是否更新地图marker(当前显示的是普通的marker)
                        if (aMap.getCameraPosition().zoom >= ORGZOON) {
                            // 移动地图视角到选中的网点位置
                            aMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                    new CameraPosition(new LatLng(dotInfo.getDotLat(), dotInfo.getDotLon()), ORGZOON, MAP_TILT, 0)), 1000,
                                    new AMap.CancelableCallback() {
                                        @Override
                                        public void onFinish() {
                                            L.i("选择网点移动视角完成之后回调函数开始执行...");
                                            // 视角移动完成后，选中网点
                                            jumpPoint(marker, true);
                                            // 8s后拉取最新网点数据，更新网点车辆数量
                                            loopRequestHandler.sendEmptyMessageDelayed(LOOP_WHAT, 8000);
                                        }

                                        @Override
                                        public void onCancel() {
                                        }
                                    });
                        }
                        // 当前显示的是聚合网点
                        else {
                            selectDotInfo = dotInfo;
                            // 移动地图视角到选中的网点位置
                            aMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                    new CameraPosition(new LatLng(dotInfo.getDotLat(), dotInfo.getDotLon()), ORGZOON, MAP_TILT, 0)), 1000, null);
                        }

                    }
                }, 500);
            }
        }
    }


    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null) {
            mMapView.onDestroy();
        }
        EventBus.getDefault().unregister(this);

        // 关闭定位，释放资源
        mainMapListener.locationSource.deactivate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isDestroyView = true;
        ButterKnife.reset(this);
        recycleMarker();
        //删除监听
        AMapNavi.getInstance(context).removeAMapNaviListener(navMainListener);
        AMapNavi.getInstance(context).destroy();
        DialogUtil.getInstance(context).closeDialog();
    }

    /**
     * 回收Marker占用的内存空间
     */
    private void recycleMarker() {
        for (String dotId : markerMap.keySet()) {
            Marker marker = markerMap.get(dotId);
            ArrayList<BitmapDescriptor> icons = marker.getIcons();
            if (icons != null && icons.size() > 0) {
                for (BitmapDescriptor bd : icons) {
                    bd.recycle();
                }
            }
        }
        markerMap.clear();
    }

    // -------------------------生命周期方法 end-----------------------------


    // -------------------------初始化方法 start-----------------------------

    public void initMap() {

        if (aMap == null) {
            aMap = mMapView.getMap();
        }

        //----------------------地图事件监听设置------------------------
        mainMapListener = new MainMapListener(this, mContext);
        navMainListener = new NavMainListener(this);
        aMap.setOnMarkerClickListener(mainMapListener);// 设置点击marker事件监听器
        aMap.setOnCameraChangeListener(onCameraChangeListener); // 设置地图移动事件监听
        aMap.setOnMapClickListener(mainMapListener); // 设置地图点击事件监听
        aMap.setLocationSource(mainMapListener.locationSource);// 设置定位监听

        //----------------------地图显示参数设置---------------------------------
        UiSettings uiSettings = aMap.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        uiSettings.setZoomControlsEnabled(false); //地图是否允许显示缩放按钮
        uiSettings.setScaleControlsEnabled(true); // 设置显示比例尺
        uiSettings.setRotateGesturesEnabled(false);   // 禁止3D旋转


        //-----------------------地图定位相关设置-----------------------------
        isMoveCamera = true;
        aMap.setMyLocationEnabled(true);    // 设置为true表示显示定位层并可触发定位
        //设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);

        // 导航路线规划相关设置
        initNaviOpt();
    }


    private String noAddress;
    private String addressSearching;
    private String addressFail;

    /**
     * 记录上一次地址栏中添加进去的View
     */
    private View preView;

    private void initTopAddress() {

        noAddress = getString(R.string.map_top_no_address);
        addressSearching = getString(R.string.map_top_address_searching);
        addressFail = getString(R.string.map_top_address_fail);

        mTvNoAddressText = (AutofitTextView) mLayoutInflater.inflate(R.layout.map_no_address_layout, null);
        mTvNoAddressText.setText(noAddress);
        mTvNoAddressText.setTag(noAddressTag);
        // 初始化默认加载无地址文案
        mFlAddressContainer.addView(mTvNoAddressText);
        preView = mTvNoAddressText;
    }

    private View createAddressView() {
        View parentView = mLayoutInflater.inflate(R.layout.map_has_address_layout, null);
        parentView.setTag(addressViewTag);
        return parentView;
    }


    public Marker locationMarker;
    public String locationTag = "locationTag";
    public String addressViewTag = "addressView";

    /**
     * 设置导航相关配置信息
     */
    private void initNaviOpt() {
        // 设置导航相关配置
        mAMapNavi = AMapNavi.getInstance(getContext());
        mAMapNavi.setAMapNaviListener(navMainListener);
    }

    /**
     * 拉取网点接口是否在请求中
     */
    private boolean isRequesting = false;
    /**
     * 保存所有的网点信息
     */
    private List<DotInterface.DotInfo> allDotsList = new ArrayList<>();


    /**
     * 请求服务器车辆位置数据
     */
    public synchronized void loadData() {
        if (Config.isNetworkConnected(context)) {
            if (isRequesting) {
                L.i("请求正在发送中....");
                return;
            }
            if (!Config.locationIsSuccess()) {
                L.i("定位失败，不能发送网络请求..");
                errorDispose();
                return;
            }
            isRequesting = true;
            DotInterface.MapSearchDotListRequest.Builder builder = DotInterface.MapSearchDotListRequest.newBuilder();
            builder.setCityId(Config.cityCode);
            builder.setCurrentPositionLat(Config.lat);
            builder.setCurrentPositionLon(Config.lng);
            // 首次请求没有成功时，传递是否需要服务端下发比例尺
            if (!isFirstRequestSuccess) {
                builder.setNeedScale(1);
            } else {
                builder.setNeedScale(2);
            }
            // 设置当前网点活动图标的类型
            builder.setIconResolution(DisplayUtil.getLogicScale());
            NetworkTask task = new NetworkTask(Cmd.CmdCode.MapSearchDotList_VALUE);
            task.setBusiData(builder.build().toByteArray());

            NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
                @Override
                public void onSuccessResponse(UUResponseData responseData) {
                    if (responseData.getRet() == 0) {
                        try {
                            showResponseCommonMsg(responseData.getResponseCommonMsg());
                            DotInterface.MapSearchDotListResponse response = DotInterface.MapSearchDotListResponse.parseFrom(responseData.getBusiData());
                            if (response.getRet() == 0) {
                                // 获取所有的活动标签
                                mIconMap = getMapUrlAndDownUrl(response.getActivityIconList());

                                isFirstRequest = false;
                                allDotsList.clear();
                                // 获取所有网点信息
                                allDotsList.addAll(response.getAllDotsList());
                                // 默认网点
                                DotInterface.DotInfo defaultDot = response.getDefaultDot();

                                // 只有首次获取服务端比例尺后才移动地图
                                if (!isFirstRequestSuccess) {
                                    // 设置地图缩放比例
                                    int scale = (int) response.getDisplayRatio();
                                    INTZOOM = scale == 0 ? INTZOOM : scale;
                                    ORGZOON = INTZOOM;
                                    // 初始化地图按指定的比例尺移动到定位的坐标
                                    if (Config.lat != 0 && Config.lng != 0) {
                                        aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(Config.lat, Config.lng), INTZOOM, MAP_TILT, 0)), 1000, null);
                                    }
                                }
                                if (allDotsList != null) {
                                    // 更新的方式来刷新map上的Marker
                                    updateMapMarkers(null, true);
                                    if (!isFirstRequestSuccess) {
                                        // 首次请求成功时，使用默认的网点
                                        isFirstRequestSuccess = true;
                                        updateSelectDot(defaultDot, true);
                                    } else {
                                        // 恢复上一次地图上选中状态的Marker
                                        if (clickMarker != null) {
                                            DotInterface.DotInfo clickDotInfo = (DotInterface.DotInfo) clickMarker.getObject();
                                            updateSelectDot(clickDotInfo, false);
                                        }
                                    }
                                }
                            } else {
                                L.i("服务端返回ret!=0");
                                errorDispose();
                            }

                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    } else {
                        L.i("网络请求失败!");
                        errorDispose();
                    }
                }

                @Override
                public void onError(VolleyError errorResponse) {
                    L.i("网络请求失败!");
                    errorDispose();
                }

                @Override
                public void networkFinish() {
                    isRequesting = false;
                    dismissProgress();
                }
            });
        } else {
            dismissProgress();
            showNetworkErrorSnackBarMsg();
            errorDispose();
        }
    }

    /**
     * 设置地图移动监听
     */
    private AMap.OnCameraChangeListener onCameraChangeListener = new AMap.OnCameraChangeListener() {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            INTZOOM = cameraPosition.zoom;
        }

        @Override
        public void onCameraChangeFinish(CameraPosition cameraPosition) {
            // 放大缩小完成后对聚合点进行重新计算
            updateMapMarkers(selectDotInfo, false);
        }
    };


    // ############################ 更新地图页面marker #####################################
    // 当前地图marker类型
    public int MARKER_NORMAL = 1;
    public int MARKER_TOGETHER =2;
    public int LAST_MARKER_TYPE = 0;
    public float level = 0;
    /**
     * 如果地图上已经加载了网点对应的Marker，则更新marker的显示，不重新生成marker
     * @param selectDotInfo 主要用于执行网点选中操作,若selectDotInfo不为空,则需要为该网点执行选中操作
     * @param isNeedUpdateContent 是否需要更新聚合网点显示内容
     */
    private synchronized void updateMapMarkers(DotInterface.DotInfo selectDotInfo, boolean isNeedUpdateContent) {
        if (allDotsList != null && allDotsList.size() > 0) {
            L.i("执行更新网点图标动作");
            L.i("地图级别:" + aMap.getCameraPosition().zoom);
            // 若当前地图级别大于初始化地图比例尺,则执行正常的marker显示逻辑
            if (aMap.getCameraPosition().zoom >= ORGZOON) {
                updateNormalMarkers(selectDotInfo);
            }
            // 若当前地图级别小于初始化比例尺,则显示聚合网点
            else {
                updateTogMarkers(selectDotInfo, isNeedUpdateContent);
            }

            System.gc();
        } else {
            L.i("网点列表为null");
        }
    }

    /**
     * 更新聚合网点
     * @param isNeedUpdateContent
     */
    private void updateTogMarkers(DotInterface.DotInfo selectDotInfo, boolean isNeedUpdateContent) {
        // 启动的时候若在地图页面还没有展开的时候就收缩了,聚合网点不会显示,因为此时markerMap为空
        if (markerMap.size() == 0 && allDotsList.size() > 0) {
            updateNormalMarkers(selectDotInfo);
        }
        // 判断上一次更新marker操作的操作类型,若上次显示的是正常的网点,则先清空地图,在刷新地图marker
        L.i("LAST_MARKER_TYPE:" + LAST_MARKER_TYPE + "  markerMapSize:" + markerMap.size());
        if (LAST_MARKER_TYPE != MARKER_TOGETHER) {
            L.i("开始显示聚合网点,清空地图normal marker...");
            LAST_MARKER_TYPE = MARKER_TOGETHER;
            aMap.clear();
            // 初始化地图页面tip和bottom
            initTopAndBottom();
            // 更新聚合marker
            MapTogetherManager.getInstance(this, mContext).onMapLoadedUpdateMarker(markerMap);
        } else {
            // 判断level是否发生变化
            L.i("地图显示级别是否发生变化:" + (aMap.getCameraPosition().zoom != level));
            L.i("是否需要更新聚合网点显示内容:" + isNeedUpdateContent);
            if (aMap.getCameraPosition().zoom != level) {
                LAST_MARKER_TYPE = MARKER_TOGETHER;
                level = aMap.getCameraPosition().zoom;
                aMap.clear();
                // 初始化地图页面tip和bottom
                initTopAndBottom();
                // 更新聚合marker
                MapTogetherManager.getInstance(this, mContext).onMapLoadedUpdateMarker(markerMap);
            }
            // 需要更新聚合网点显示内容
            else if (isNeedUpdateContent) {
                MapTogetherManager.getInstance(this, mContext).onMapLoadedUpdateMarkerText(allDotsList, markerMap);
            }
        }

        // 设置marker点击事件,若是聚合网点此时点击marker则放大地图显示正常网点
        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // 初始化地图按指定的比例尺移动到定位的坐标
                aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(marker.getPosition(), ORGZOON, MAP_TILT, 0)), 1000, null);
                return true;
            }
        });
    }


    /**
     * 更新普通网点数据
     */
    private void updateNormalMarkers(DotInterface.DotInfo selectDotInfo) {
        // 判断上一次更新marker操作的操作类型,若上次显示的是聚合网点,则先清空地图,然后清空网点信息,在刷新地图marker
        L.i("LAST_MARKER_TYPE:" + LAST_MARKER_TYPE);
        if (LAST_MARKER_TYPE != MARKER_NORMAL) {
            L.i("开始显示正常网点,开始清空地图markder和markerMap...");
            LAST_MARKER_TYPE = MARKER_NORMAL;
            aMap.clear();
            markerMap.clear();
        }

        // 若有网点数据删除,则删除保存的网点数据
        if (markerMap != null && markerMap.entrySet().size() > 0) {
            if (allDotsList != null && allDotsList.size() > 0) {
                Map<String, Marker> tempMarkerMap = new ConcurrentHashMap<>();
                tempMarkerMap.putAll(markerMap);
                for (DotInterface.DotInfo dotInfo : allDotsList) {
                    if (tempMarkerMap.containsKey(dotInfo.getDotId())) {
                        tempMarkerMap.remove(dotInfo.getDotId());
                    }
                }

                if (tempMarkerMap.entrySet().size() > 0) {
                    Iterator<Map.Entry<String, Marker>> iterator = tempMarkerMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Marker> entryMarker = iterator.next();
                        markerMap.get(entryMarker.getKey()).remove();
                        markerMap.remove(entryMarker.getKey());
                    }
                }
            }
        }

        // 若网点数据有更新和添加,则更新和添加网点信息
        for (DotInterface.DotInfo dotInfo : allDotsList) {
            String dotId = dotInfo.getDotId();
            int count = dotInfo.getCarTotal();
            if (markerMap.containsKey(dotId)) {
                Marker oldMarker = markerMap.get(dotId);

                // 若更新的markder网点车辆数据没有发生变化,则无需更新显示
                if (count != ((DotInterface.DotInfo)oldMarker.getObject()).getCarTotal()) {
                    if (count == 0) {
                        setIconToMarker(0, dotInfo, oldMarker);
                    } else {
                        setIconToMarker(1, dotInfo, oldMarker);
                    }
                }
                // 更新clickMarker
                if (clickMarker != null) {
                    DotInterface.DotInfo clickDotInfo = (DotInterface.DotInfo) clickMarker.getObject();
                    if (dotId.equals(clickDotInfo.getDotId())) {
                        clickMarker.setObject(dotInfo);
                    }
                }
                oldMarker.setZIndex(MARKER_NORMAL_ZINDEX);
                oldMarker.setObject(dotInfo);
            } else {
                MarkerOptions options = new MarkerOptions();
                options.anchor(0.5f, 1.0f);
                options.position(new LatLng(dotInfo.getDotLat(), dotInfo.getDotLon()));
                if (count == 0) {
                    setIconToOptions(0, dotInfo, options);
                } else {
                    setIconToOptions(1, dotInfo, options);
                }
                Marker marker = aMap.addMarker(options);
                marker.setObject(dotInfo);
                marker.setZIndex(MARKER_NORMAL_ZINDEX);

                markerMap.put(dotId, marker);
            }
        }

        // 判断选择的网点是否为空,若不为空则执行选中操作
        if (selectDotInfo != null) {
            // 视角移动完成后，选中网点
            jumpPoint(markerMap.get(selectDotInfo.getDotId()), true);
            // 设置选中的selectDotInfo为空
            this.selectDotInfo = null;
            // 8s后拉取最新网点数据，更新网点车辆数量
            loopRequestHandler.sendEmptyMessageDelayed(LOOP_WHAT, 8000);
        }
        // 设置普通marker点击监听事件
        aMap.setOnMarkerClickListener(mainMapListener);
    }
    // ############################ 更新地图页面marker #####################################







    /**
     * 初始化地图页面tip和bottom
     */
    private void initTopAndBottom() {
        // 还原选中的Marker的上一次的显示状态
        restoreClickMarker(new LatLng(0, 0));
        // 恢复顶部地址栏显示文案
        restoreTopAddress();
        // 隐藏底部车辆详情视图
        hideBottomContent();
    }

    private void errorDispose() {
        // 设置首次请求是否成功标识
        if (isFirstRequest) {
            isFirstRequestSuccess = false;
            // 延迟5S重新拉取数据
            loopRequestHandler.sendEmptyMessageDelayed(LOOP_WHAT, ERROR_INTERVAL);
            isFirstRequest = false;
        }

    }

    /**
     * 根据传入的Dot信息，更新地图界面上的Marker的选中
     *
     * @param defaultDot
     */
    private void updateSelectDot(DotInterface.DotInfo defaultDot, boolean isPlan) {
        if (defaultDot != null) {
            // 更新选中的网点Marker
            if (markerMap.containsKey(defaultDot.getDotId())) {
                Marker marker = markerMap.get(defaultDot.getDotId());
                jumpPoint(marker, isPlan);
            }
        }
    }


    /**
     * 根据网点的数量以及选中状态返回Marker的样式
     *
     * @param type  0：:网点无车辆  1：网点有车辆但未选中  2：网点被选中
     * @param count
     * @param dotInfo
     * @return
     */
    private View createTextIconByType(int type, String count, DotInterface.DotInfo dotInfo, Bitmap mBitmap) {

        View view = mLayoutInflater.inflate(R.layout.map_marker_layout, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.iv_marker);
        TextView textView = (TextView) view.findViewById(R.id.tv_marker_number);
        // 当网点车辆数大于1位时，修改显示网点TextView的宽高
        if (count.length() > 1) {
            int size = DisplayUtil.dip2px(mContext, 22);
            textView.getLayoutParams().width = size;
            textView.getLayoutParams().height = size;
        }
        textView.setText(count);
        if (isAdded()) {
            // 设置默认的icon图标
            switch (type) {
                case 0:
                    // 是否支持异地还车
                    if (dotInfo.getIsA2B() == ISATOB) {
                        textView.setBackgroundResource(R.drawable.marker_num_nonumber_bg);
                        imageView.setImageResource(R.mipmap.ic_location_return_none);
                    } else {
                        // 有折扣
                        if (1 == dotInfo.getHasDiscount()) {
                            textView.setBackgroundResource(R.drawable.marker_num_nonumber_bg);
                            imageView.setImageResource(R.mipmap.ic_location_nonumber_discount);
                        }
                        // 无折扣
                        else {
                            textView.setBackgroundResource(R.drawable.marker_num_nonumber_bg);
                            imageView.setImageResource(R.mipmap.ic_location_nonumber);
                        }
                    }

                    break;
                case 1:
                    // 是否支持异地还车
                    if (dotInfo.getIsA2B() == ISATOB) {
                        textView.setBackgroundResource(R.drawable.marker_num_normal_bg);
                        imageView.setImageResource(R.mipmap.ic_location_return_nromal);
                    } else {
                        // 有折扣
                        if (1 == dotInfo.getHasDiscount()) {
                            textView.setBackgroundResource(R.drawable.marker_num_normal_bg);
                            imageView.setImageResource(R.mipmap.ic_location_normal_discount);
                        }
                        // 无折扣
                        else {
                            textView.setBackgroundResource(R.drawable.marker_num_normal_bg);
                            imageView.setImageResource(R.mipmap.ic_location_normal);
                        }
                    }

                    break;
                case 2:
                    // 是否支持异地还车
                    if (dotInfo.getIsA2B() == ISATOB) {
                        textView.setBackgroundResource(R.drawable.marker_num_selected_bg);
                        imageView.setImageResource(R.mipmap.ic_location_return_selected);
                    } else {
                        // 有折扣
                        if (1 == dotInfo.getHasDiscount()) {
                            textView.setBackgroundResource(R.drawable.marker_num_selected_bg);
                            imageView.setImageResource(R.mipmap.ic_location_seleted_discount);
                        }
                        // 无折扣
                        else {
                            textView.setBackgroundResource(R.drawable.marker_num_selected_bg);
                            imageView.setImageResource(R.mipmap.ic_location_seleted);
                        }
                    }

                    break;
            }
            // 如果有图标，则更换Marker图标
            if (mBitmap != null) {
                imageView.setImageBitmap(mBitmap);
            }
        }
        return view;
    }

    /**
     * 为marker的Options设置icon
     * @param type
     * @param dotInfo
     * @param options
     */
    private void setIconToOptions(int type, DotInterface.DotInfo dotInfo, MarkerOptions options) {
        Bitmap mBitmap = getDownloadBitmap(type, dotInfo);
        View view = createTextIconByType(type, dotInfo.getCarTotal() + "", dotInfo, mBitmap);
        options.icon(BitmapDescriptorFactory.fromView(view));
    }


    /**
     * 为marker设置icon
     * @param type
     * @param dotInfo
     * @param mMarker
     */
    private void setIconToMarker(int type, DotInterface.DotInfo dotInfo, Marker mMarker) {
        BitmapDescriptor oldBitmapDescriptor = null;
        ArrayList<BitmapDescriptor> mMarkerIcons = mMarker.getIcons();
        if (mMarkerIcons != null && mMarkerIcons.size() > 0) {
            oldBitmapDescriptor = mMarkerIcons.get(0);
        }
        Bitmap mBitmap = getDownloadBitmap(type, dotInfo);
        View view = createTextIconByType(type, dotInfo.getCarTotal() + "", dotInfo, mBitmap);
        mMarker.setIcon(BitmapDescriptorFactory.fromView(view));
        if (oldBitmapDescriptor != null) {
            oldBitmapDescriptor.recycle();
        }
    }


    /**
     * 获取下载成功的缓存图片
     * @param type
     * @param dotInfo
     * @return
     */
    public Bitmap getDownloadBitmap(int type, DotInterface.DotInfo dotInfo) {
        String imageUrl = getIconUrlFromType(type, dotInfo);
        if (TextUtils.isEmpty(imageUrl)) {
            return null;
        }
        return MapIconLruImageCache.getInstance().getBitmap(MapIconLruImageCache.getCacheKey(imageUrl));
    }


    // 该网店是否支持异地还车
    public static final int ISATOB = 1;
    /**
     * 根据类型获取活动图标的URL地址
     * @param type 0：:网点无车辆  1：网点有车辆  2：网点被选中
     * @return
     */
    public String getIconUrlFromType(int type, DotInterface.DotInfo dotInfo) {
        String imageUrl = "";
        if (mIconMap != null && mIconMap.size() > 0) {
            switch (type) {
                case 0:
                    // 是否支持异地还车
                    if (dotInfo.getIsA2B() == ISATOB) {
                        imageUrl = mIconMap.get(DotInterface.ActivityIconType.UncheckedNoCarNoDiscountA2B);
                    } else {
                        // 有折扣
                        if (1 == dotInfo.getHasDiscount()) {
                            imageUrl = mIconMap.get(DotInterface.ActivityIconType.UncheckedNoCarHasDiscount);
                        }
                        // 无折扣
                        else {
                            imageUrl = mIconMap.get(DotInterface.ActivityIconType.UncheckedNoCarNoDiscount);
                        }
                    }
                    break;
                case 1:
                    // 是否支持异地还车
                    if (dotInfo.getIsA2B() == ISATOB) {
                        imageUrl = mIconMap.get(DotInterface.ActivityIconType.UncheckedHasCarNoDiscountA2B);
                    } else {
                        // 有折扣
                        if (1 == dotInfo.getHasDiscount()) {
                            imageUrl = mIconMap.get(DotInterface.ActivityIconType.UncheckedHasCarHasDiscount);
                        }
                        // 无折扣
                        else {
                            imageUrl = mIconMap.get(DotInterface.ActivityIconType.UncheckedHasCarNoDiscount);
                        }
                    }
                    break;
                case 2:
                    // 是否支持异地还车
                    if (dotInfo.getIsA2B() == ISATOB) {
                        imageUrl = mIconMap.get(DotInterface.ActivityIconType.CheckedNoDiscountA2B);
                    } else {
                        // 有折扣
                        if (1 == dotInfo.getHasDiscount()) {
                            imageUrl = mIconMap.get(DotInterface.ActivityIconType.CheckedHasDiscount);
                        }
                        // 无折扣
                        else {
                            imageUrl = mIconMap.get(DotInterface.ActivityIconType.CheckedNoDiscount);
                        }
                    }

                    break;
            }
        }
        return imageUrl;
    }


    /**
     * 拉取成功和拉取失败的数量，用来判断拉取是否全部成功
     */
    private int successCount = 0;
    private int failureCount = 0;
    /**
     * 是否接收到更新图标的事件
     */
    private boolean isReceiveUpdateMapIconEvent = false;

    /**
     * 将网点需要拉取的icon图片转化为map，并执行下载图片动作
     * @param iconList
     * @return
     */
    private Map<DotInterface.ActivityIconType, String> getMapUrlAndDownUrl(final List<DotInterface.ActivityIcon> iconList) {
        if (iconList != null && iconList.size() > 0) {
            if (successCount == iconList.size() && compareAllUrlEquals(iconList)) {
                L.i("网点图标已全部拉取成功...");
                return mIconMap;
            }
            Map<DotInterface.ActivityIconType, String> iconMap = new HashMap<>();
            Set<String> urlSet = new HashSet<>();
            successCount = 0;
            failureCount = 0;
            for (int i = 0; i < iconList.size(); i++) {
                DotInterface.ActivityIcon discountIcon = iconList.get(i);
                if (TextUtils.isEmpty(discountIcon.getUrl())) {
                    failureCount++;
                    continue;
                }
                urlSet.add(discountIcon.getUrl());
                iconMap.put(discountIcon.getType(), discountIcon.getUrl());
                L.i("iconType:" + discountIcon.getType() + "   " + "iconUrl:" + discountIcon.getUrl());
                // 请求服务器获取icon列表
                UUApp.getMapIconImageLoader().get(discountIcon.getUrl(), new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        if (response.getBitmap() != null) {
                            successCount++;
                            if (successCount == iconList.size() - failureCount) {
                                // 未收到Event时才发送通知
                                if (!isReceiveUpdateMapIconEvent) {
                                    EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_UPDATE_MAPICON, null));
                                }
                            }
                        }
                    }
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        failureCount++;
                    }
                });
            }
            saveMarkerIconUrlSp(urlSet);
            L.i("保存需要替换的网点图标成功...");
            return iconMap;
        }
        return null;
    }

    /**
     * 比较所有的Url和本地Map中的是否相等
     *
     * @param iconList
     * @return
     */
    private boolean compareAllUrlEquals(List<DotInterface.ActivityIcon> iconList) {
        if (iconList != null && iconList.size() > 0 && mIconMap != null && mIconMap.size() > 0) {
            for (DotInterface.ActivityIcon icon : iconList) {
                DotInterface.ActivityIconType type = icon.getType();
                String url = icon.getUrl();
                if (!mIconMap.containsKey(type) || !url.equals(mIconMap.get(type))) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 持久化服务端下发的Marker的Icon对应的url
     * 在StartActivity启动时，执行一次拉取动作
     *
     * @param urlSet
     */
    private void saveMarkerIconUrlSp(Set urlSet) {
        SharedPreferences sp = context.getSharedPreferences(SPConstant.SP_NAME_MARKER_ICON_URLS, Context.MODE_PRIVATE);
        sp.edit().clear().commit();
        sp.edit().putStringSet(SPConstant.SP_KEY_MARKER_ICON_URLS, urlSet).commit();
    }

    // -------------------------初始化方法 end-----------------------------


    // -------------------------各种Listener start-----------------------------

    private String noAddressTag = "noAddressTag";

    /**
     * 恢复顶部地址栏显示文案
     */
    public void restoreTopAddress() {
        if (preView != null) {
            Object object = preView.getTag();
            if (object != null && noAddressTag.equals(object.toString())) {
                return;
            }
        }
        mTvNoAddressText = (AutofitTextView) mLayoutInflater.inflate(R.layout.map_no_address_layout, null);
        mTvNoAddressText.setTag(noAddressTag);
        mTvNoAddressText.setText(noAddress);
        showAddressByAnim(mTvNoAddressText);
    }

    /**
     * 路线规划的对象
     */
    public AMapNaviPath naviPath;


    // -------------------------各种Listener end-----------------------------


    // -------------------------点击事件处理 start-----------------------------

    /**
     * 是否是点击的定位按钮触发的定位操作:true时不调用loadData()去加载最新数据，false时需要调用loadData()加载最新数据
     */
    public boolean isClickLocationButton = false;
    /**
     * 定位成功后，是否移动视角
     */
    public boolean isMoveCamera = false;

    /**
     * 点击定位按钮，移动地图到定位的位置
     */
    @OnClick(R.id.btn_location)
    public void locationClick() {
        if (Config.lng != 0 && Config.lat != 0) {
            aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(Config.lat, Config.lng), INTZOOM, MAP_TILT, 0)), 500, null);
            return;
        }
        isClickLocationButton = true;
        // 2s内任然没有定位成功，显示定位中进度窗
        rootView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isClickLocationButton) {
                    showProgress(true, "定位中...");
                }
            }
        }, 1000);

        resetLocation();
    }

    /**
     * 执行定位操作
     */
    private void doLocationOption() {
        if (!Config.isNetworkConnected(mContext)) {
            showNetworkErrorSnackBarMsg();
            return;
        }
        resetLocation();
    }

    /**
     * 重新定位逻辑
     */
    private void resetLocation() {
        L.i("重新定位....");
        mainMapListener.locationSource.deactivate();
        isMoveCamera = true;
        aMap.setLocationSource(mainMapListener.locationSource);
        aMap.setMyLocationEnabled(true);
    }


    /**
     * 点击顶部的地址栏，进入网点列表界面
     */
    @OnClick(R.id.ll_path_container)
    public void pathContainerClick() {
        Intent intent = new Intent(context, NearStationActivity.class);
        intent.putExtra(IntentConfig.NEAR_STATION_NEED_GET_DATA, true);
        startActivity(intent);
    }

    // -------------------------点击事件处理 end-----------------------------


    // -------------------------其他方法处理 start-----------------------------


    /**
     * 点击的Marker
     */
    private Marker clickMarker;

    private Object markerLock = new Object();


    /**
     * 还原选中的Marker的上一次的显示状态
     */
    public void restoreClickMarker(LatLng latLng) {
        synchronized (markerLock) {
            if (clickMarker != null) {
                Marker tempMarker = clickMarker;
                DotInterface.DotInfo oldDotInfo = (DotInterface.DotInfo) tempMarker.getObject();
                if (oldDotInfo != null) {
                    if (latLng.latitude != oldDotInfo.getDotLat() || latLng.longitude != oldDotInfo.getDotLon()) {
                        int count = oldDotInfo.getCarTotal();
                        if (count == 0) {
                            setIconToMarker(0, oldDotInfo, tempMarker);
                        } else {
                            setIconToMarker(1, oldDotInfo, tempMarker);
                        }

                        tempMarker.setZIndex(MARKER_NORMAL_ZINDEX);

                        clickMarker = null;
                    }
                }
            }
        }
    }

    /**
     * 点击地图上的Marker后的逻辑处理：
     * 更换Marker的点击状态Icon
     * 在地图上显示路径规划
     *
     * @param marker
     * @param isPlan 是否进行路线规划
     */
    public void jumpPoint(Marker marker, boolean isPlan) {
        synchronized (markerLock) {
            boolean isLastMarker = false;
            DotInterface.DotInfo dotInfo = (DotInterface.DotInfo) marker.getObject();
            if (dotInfo == null) {
                L.i("当前点击的是路线规划后隐藏的起点和终点marker");
                return;
            }
            if (clickMarker != null) {
                DotInterface.DotInfo oldDotInfo = (DotInterface.DotInfo) clickMarker.getObject();
                if (dotInfo != null && oldDotInfo != null) {
                    if (!dotInfo.getDotId().equals(oldDotInfo.getDotId())) {
                        int count = oldDotInfo.getCarTotal();
                        if (count == 0) {
                            setIconToMarker(0, oldDotInfo, clickMarker);
                        } else {
                            setIconToMarker(1, oldDotInfo, clickMarker);
                        }

                        clickMarker.setZIndex(MARKER_NORMAL_ZINDEX);
                    } else {
                        L.i("点击的是同一个Marker");
                        isLastMarker = true;
                        setIconToMarker(2, oldDotInfo, clickMarker);
                        clickMarker.setZIndex(MARKER_SELECTED_ZINDEX);
                        clickMarker = marker;
                    }
                }
            }
            if (dotInfo != null && !isLastMarker) {
                setIconToMarker(2, dotInfo, marker);
                mNaviEnd = new NaviLatLng(dotInfo.getDotLat(), dotInfo.getDotLon());
                marker.setZIndex(MARKER_SELECTED_ZINDEX);
                clickMarker = marker;
            }

            if (mNaviStart != null && mNaviEnd != null && isPlan) {
                // 创建一个新的地址View，并初始化加载中状态
                if (!isLastMarker) {
                    // 是上一次的网点时，只更新地址信息，不进行动画切换
                    View addressView = createAddressView();
                    showAddressByAnim(addressView);
                }
                changeHasAddressView(0, null);
                // 进行路径规划
                mAMapNavi.calculateWalkRoute(mNaviStart, mNaviEnd);

            }
            if (isPlan) {
                if (!isLastMarker) {
                    if (isShowBottomContent) {
                        hideBottomContent();
                        rootView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showBottomContentByAnim();
                            }
                        }, 400);
                    } else {
                        showBottomContentByAnim();
                    }
                } else {
                    if (!isShowBottomContent) {
                        showBottomContentByAnim();
                    }
                }
            } else {
                if (isLastMarker && !isShowBottomContent) {
                    showBottomContentByAnim();
                }
            }
        }
    }

    /**
     * 底部车辆详情界面是否显示
     */
    private boolean isShowBottomContent = false;
    /**
     * 是否是底部车辆详情通知的刷新操作
     */
    private boolean isCarDetailNotifyRefresh = false;

    /**
     * 记录显示上一个车辆详情的Fragment对象
     */
    private MapConfirmCarFragment confirmCarFragment;

    /**
     * 由上至下动画显示车辆详情视图
     */
    private void showBottomContentByAnim() {
        if (isDestroyView) {
            return;
        }
        if (clickMarker != null && clickMarker.getObject() != null) {
            DotInterface.DotInfo dotInfo = (DotInterface.DotInfo) clickMarker.getObject();
            if (confirmCarFragment == null || confirmCarFragment.isDestroy()) {
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                confirmCarFragment = new MapConfirmCarFragment();
                Bundle bundle = new Bundle();
                bundle.putString(IntentConfig.DOT_ID, dotInfo.getDotId());
                bundle.putInt(IntentConfig.KEY_DOT_CAR_NUMBER, dotInfo.getCarTotal());
                confirmCarFragment.setArguments(bundle);
                transaction.replace(R.id.fl_bottom_container, confirmCarFragment);
                transaction.commitAllowingStateLoss();
            } else {
                confirmCarFragment.loadNewData(dotInfo.getDotId(), dotInfo.getCarTotal());
            }
            isShowBottomContent = true;
            updateContainerShowStatus();
        }

    }

    /**
     * 根据是否显示车辆详情标识，动态更新显示状态
     */
    private synchronized void updateContainerShowStatus() {
        if (isDestroyView) {
            return;
        }
        int height = (int) context.getResources().getDimension(R.dimen.map_confirm_height);
        if (isShowBottomContent) {
            mFlBottomContainer.setVisibility(View.VISIBLE);
            ValueAnimator valueAnimator = ValueAnimator.ofInt(height, 0);
            valueAnimator.setInterpolator(new AccelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (mFlBottomContainer != null && mBtnLocation != null) {
                        int height = (int) animation.getAnimatedValue();
                        mFlBottomContainer.setTranslationY(height);
                        mBtnLocation.setTranslationY(height);
                    }
                }
            });
            valueAnimator.setDuration(300).start();

        } else {
            ValueAnimator valueAnimator = ValueAnimator.ofInt(0, height);
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (mFlBottomContainer != null && mBtnLocation != null) {
                        int height = (int) animation.getAnimatedValue();
                        mFlBottomContainer.setTranslationY(height);
                        mBtnLocation.setTranslationY(height);
                    }
                }
            });
            valueAnimator.addListener(new EmptyAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    if (mFlBottomContainer != null) {
                        mFlBottomContainer.setVisibility(View.GONE);
                        mBtnLocation.setTranslationY(0);
                    }
                }
            });
            valueAnimator.setDuration(300).start();

        }
    }

    /**
     * 隐藏底部车辆详情视图
     */
    public void hideBottomContent() {
        if (isShowBottomContent) {
            isShowBottomContent = false;
            updateContainerShowStatus();
        }
    }


    /**
     * 动画切换顶部地址显示
     *
     * @param newView
     */
    private void showAddressByAnim(final View newView) {
        if (isDestroyView) {
            return;
        }
        if (preView != null) {
            preView.animate().alpha(0).setDuration(500).start();
        }
        if (mFlAddressContainer != null) {
            mFlAddressContainer.addView(newView);
        }
        newView.setVisibility(View.INVISIBLE);

        Animation animation = AnimationUtils.loadAnimation(context, R.anim.map_address_anim);
        animation.setAnimationListener(new EmptyAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                super.onAnimationStart(animation);
                newView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                if (preView != null && mFlAddressContainer != null) {
                    mFlAddressContainer.removeView(preView);
                }
                preView = newView;
            }
        });
        newView.setAnimation(animation);
        animation.start();

        Object object = newView.getTag();
        if (object != null && addressViewTag.equals(object.toString())) {
            mHasAddressView = newView;
        }
    }

    private AutofitTextView mLocationText;
    private TextView mTvMsg;

    /**
     * 在地址栏显示地址的过程中，根据类型显示不同的状态
     *
     * @param type 0、加载中   1、加载成功  2、加载失败
     */
    public void changeHasAddressView(int type, AMapNaviPath naviPath) {
        if (mHasAddressView != null) {

            mLocationText = (AutofitTextView) mHasAddressView.findViewById(R.id.tv_location_text);
            mTvMsg = (TextView) mHasAddressView.findViewById(R.id.tv_msg);
            // 显示上方网点信息
            if (clickMarker != null) {
                DotInterface.DotInfo dotInfo = (DotInterface.DotInfo) clickMarker.getObject();
                if (dotInfo != null) {
                    mLocationText.setText(dotInfo.getDotName());
                }
            }
            switch (type) {
                case 0:
                    mTvMsg.setText(addressSearching);
                    break;
                case 1:
                    int allLength = naviPath.getAllLength();
                    float distance = allLength / 1000f;
                    String result;
                    if (distance >= 1) {
                        result = String.format("%.1f", distance) + "公里";
                    } else {
                        result = allLength + "米";
                    }
                    mTvMsg.setText("步行" + result);
                    break;
                case 2:
                    mTvMsg.setText(addressFail);
                    break;
            }
        }
    }

    // -------------------------其他方法处理 end-----------------------------

    //--------------------------轮询拉取网点数据-----------------------------

    /**
     * 默认的时间间隔：1分钟
     */
    private static int DEFAULT_INTERVAL = 60 * 1000;
    /**
     * 异常情况下的轮询时间间隔:5秒
     */
    public static int ERROR_INTERVAL = 5 * 1000;
    /**
     * 当前轮询执行的时间间隔
     */
    public static int interval = DEFAULT_INTERVAL;
    /**
     * 轮询Handler的消息类型
     */
    public static int LOOP_WHAT = 10;
    /**
     * 是否是第一次拉取数据
     */
    private boolean isFirstRequest = false;
    /**
     * 第一次请求数据是否成功
     */
    public boolean isFirstRequestSuccess = false;

    /**
     * 开始执行轮询，正常情况下，每隔1分钟轮询拉取一次最新数据
     * 在onStart时开启轮询
     */
    private void startLoop() {
        L.i("页面onStart，需要开启轮询");
        loopRequestHandler.sendEmptyMessageDelayed(LOOP_WHAT, interval);
    }

    /**
     * 关闭轮询，在界面onStop时，停止轮询操作
     */
    private void stopLoop() {
        L.i("页面已onStop，需要停止轮询");
        loopRequestHandler.removeMessages(LOOP_WHAT);
    }

    /**
     * 处理轮询的Handler
     */
    public Handler loopRequestHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            // 如果首次请求失败，
            if (!isFirstRequestSuccess) {
                L.i("首次请求失败，需要将轮询时间设置为:" + ERROR_INTERVAL);
                interval = ERROR_INTERVAL;
            } else {
                interval = DEFAULT_INTERVAL;
            }

            L.i("轮询中-----当前轮询间隔：" + interval);

            loopRequestHandler.removeMessages(LOOP_WHAT);

            // 首次请求为成功、或者定位未成功时执行重定位，并加载网点数据
            if (!isFirstRequestSuccess || !Config.locationIsSuccess()) {
                isClickLocationButton = false;
                doLocationOption();
            } else {
                loadData();
            }

            System.gc();

            loopRequestHandler.sendEmptyMessageDelayed(LOOP_WHAT, interval);

        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_map_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_active) {
            if (!Config.isNetworkConnected(mContext)) {
                showNetworkErrorSnackBarMsg();
                return true;
            }
            Intent intent = new Intent(mContext, H5Activity.class);
            intent.putExtra(H5Constant.TITLE, URLConfig.getUrlInfo().getActiveList().getTitle());
            intent.putExtra(H5Constant.MURL, URLConfig.getUrlInfo().getActiveList().getUrl());
            startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
