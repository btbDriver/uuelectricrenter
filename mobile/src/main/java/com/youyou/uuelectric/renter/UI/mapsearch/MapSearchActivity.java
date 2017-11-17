package com.youyou.uuelectric.renter.UI.mapsearch;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.uu.facade.dot.protobuf.iface.DotInterface;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.UI.main.rentcar.LocalPointItem;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;
import com.youyou.uuelectric.renter.Utils.task.ActivityUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * Created by cdj on 2016/2/25.
 */
public class MapSearchActivity extends BaseActivity implements AMap.OnMapLoadedListener, AMap.OnMarkerClickListener {

    /**
     * 地图比例尺
     */
    public static float INTZOOM = 8;
    /**
     * 定义地图的视角，放到到一定级别后会显示3D建筑
     */
    private static final int MAP_TILT = 3;
    private static final int SHOW_MARKER_NUM = 4;
    private AMap aMap;
    @InjectView(R.id.map)
    public MapView mapView = null;
    public LocalPointItem localPointItem;
    //位置附近网点集合
    private ArrayList<DotInterface.DotInfo> dotInfoList = new ArrayList<>();
    private DotAdapter adapter;
//    private BottomSheetBehavior behavior;
//    private View bottomSheet;

    private List<Double> latList = new ArrayList<Double>();
    private List<Double> lonList = new ArrayList<Double>();
    public String dotId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_search);
        ButterKnife.inject(this);
        mapView.onCreate(savedInstanceState); // 此方法必须重写


        localPointItem = getIntent().getParcelableExtra("localPointItem");
        dotId =  getIntent().getStringExtra(IntentConfig.DOT_ID);

        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }

        //创建fragment

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        MapAdressNearDotFragment mapAdressNearDotFragment = new MapAdressNearDotFragment();
        Bundle bundle = new Bundle();
//        bundle.putParcelable("localPointItem",localPointItem);
        mapAdressNearDotFragment.setArguments(bundle);
        transaction.replace(R.id.fl_bottom_container, mapAdressNearDotFragment);
        transaction.commitAllowingStateLoss();

        // The View with the BottomSheetBehavior
        /*CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.cl);
        bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheet.setVisibility(View.GONE);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                //这里是bottomSheet 状态的改变，根据slideOffset可以做一些动画
//                ViewCompat.setScaleX(bottomSheet,1);
//                ViewCompat.setScaleY(bottomSheet,1);
                String state = "null";
                *//*STATE_COLLAPSED
                折叠状态。可通过app:behavior_peekHeight来设置默认显示的高度。

                STATE_SETTING
                拖拽松开之后到达终点位置（collapsed or expanded）前的状态。

                STATE_EXPANDED
                完全展开的状态。

                STATE_HIDDEN
                隐藏状态。默认是false，可通过app:behavior_hideable属性设置。

                STATE_DRAGGING
                        被拖拽状态*//*
                switch (newState) {
                    case 1:
                        state = "STATE_DRAGGING";//被拖拽状态
//                        initDefaultToolBar();
                        break;
                    case 2:
                        state = "STATE_SETTLING";//拖拽松开之后到达终点位置（collapsed or expanded）前的状态

                        break;
                    case 3:
                        state = "STATE_EXPANDED";//完全展开的状态。
//                        removeDefaultToolbar();
                        break;
                    case 4:
                        state = "STATE_COLLAPSED";//折叠状态。可通过app:behavior_peekHeight来设置默认显示的高度

                        break;
                    case 5:
                        state = "STATE_HIDDEN";//隐藏状态。默认是false，可通过app:behavior_hideable属性设置。
                        break;
                }
                Log.d("MainActivity", "newState:" + state);
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
                //这里是拖拽中的回调，根据slideOffset可以做一些动画
//                ViewCompat.setScaleX(bottomSheet,slideOffset);
//                ViewCompat.setScaleY(bottomSheet,slideOffset);
            }
        });

        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });*/

    }
    /**
     * 删除默认的ToolBar
     */
    protected void removeDefaultToolbar() {
        mToolbarContainer.removeAllViews();
        mToolbarContainer.setVisibility(View.GONE);
    }
    /**
     * 初始化默认的ToolBar
     */
    private void initDefaultToolBar() {
        if (mDefaultToolBar != null) {
            String label = getTitle().toString();
            setTitle(label);
            setSupportActionBar(mDefaultToolBar);
            mDefaultToolBar.setNavigationIcon(R.mipmap.toolbar_back_icn_transparent);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0 || item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpMap() {
        //----------------------地图显示参数设置---------------------------------
        UiSettings uiSettings = aMap.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        uiSettings.setZoomControlsEnabled(false); //地图是否允许显示缩放按钮
        uiSettings.setScaleControlsEnabled(true); // 设置显示比例尺
        uiSettings.setRotateGesturesEnabled(false);   // 禁止3D旋转

        //----------------------地图事件监听设置------------------------
        aMap.setOnMapLoadedListener(this);// 设置amap加载成功事件监听器
        aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器

    }

    public void addMarkersToMap(ArrayList<DotInterface.DotInfo> dotInfoList) {
        LatLng dotLatLng;
        int size = dotInfoList.size();
        int flag = size >= SHOW_MARKER_NUM ? SHOW_MARKER_NUM : size;

        for (int i = flag - 1; i >= 0; i--) {
            DotInterface.DotInfo dotInfo = dotInfoList.get(i);
            dotLatLng = new LatLng(dotInfo.getDotLat(), dotInfo.getDotLon());

            //添加marker点的经纬度到集合中
            latList.add(dotInfo.getDotLat());
            lonList.add(dotInfo.getDotLon());

            MarkerOptions options = new MarkerOptions();
            options.anchor(0.5f, 1.0f);
            options.position(dotLatLng);
            options.icon(BitmapDescriptorFactory.fromView(createDotIcon((i + 1))));
            Marker marker = aMap.addMarker(options);
            marker.setObject(dotInfo);
        }

        latList.add(localPointItem.getLat());
        lonList.add(localPointItem.getLng());
        MarkerOptions locationOptions = new MarkerOptions();
        locationOptions.anchor(0.5f, 1.0f);
        locationOptions.position(new LatLng(localPointItem.getLat(), localPointItem.getLng()));
        locationOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_mylocation));
        aMap.addMarker(locationOptions);

        //将所有的marker点正好可以展示到地图可以完全展示的比例尺中去
        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(
                new LatLng(Collections.min(latList),Collections.min(lonList)),new LatLng(Collections.max(latList),Collections.max(lonList))),mDefaultToolBar.getHeight()),1000,null);

    }

    private View createDotIcon(int index) {
        View view;
        if (index <= 4) {
            view = View.inflate(this, R.layout.map_dot_marker_layout, null);
        } else {
            view = View.inflate(this, R.layout.map_dot_marker_layout_small, null);
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.iv_marker);
        TextView textView = (TextView) view.findViewById(R.id.tv_marker_number);
        textView.setText(index + "");
//        textView.setBackgroundResource(R.drawable.marker_num_normal_bg);
//        imageView.setImageResource(R.mipmap.ic_location_normal);
        return view;
    }


    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onMapLoaded() {
//        gotoLocationAnim();

    }

    /**
     * 动画到搜索位置，完成后加上位置的marker，并且去请求附近网点信息
     */
    public void gotoLocationAnim() {
        // 移动地图视角到搜索位置，，动画执行1秒
        aMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition(new LatLng(localPointItem.getLat() , localPointItem.getLng()), INTZOOM, MAP_TILT, 0)), 1000,
                new AMap.CancelableCallback() {

                    private Marker locationMarker;

                    @Override
                    public void onFinish() {
                        // 视角移动完成后，
                        // 设置位置的大头针Marker
                        if (locationMarker != null) {
                            locationMarker.remove();
                            locationMarker.destroy();
                        }
                        MarkerOptions options = new MarkerOptions();
                        options.anchor(0.5f, 1.0f);
                        options.position(new LatLng(localPointItem.getLat(), localPointItem.getLng()));
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_mylocation));
                        locationMarker = aMap.addMarker(options);
                        locationMarker.setZIndex(10);
//                        requestMarkerByLocation();
                    }

                    @Override
                    public void onCancel() {
                    }
                });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!Config.isNetworkConnected(mContext)) {
            ((BaseActivity) mContext).showDefaultNetworkSnackBar();
            return false;
        }
        if(marker.getObject() != null){
            DotInterface.DotInfo dotInfo = (DotInterface.DotInfo) marker.getObject();
            EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_SELECTED_DOTINFO2, dotInfo));

            ActivityUtil.closeNumberActivities(2);
        }
        return false;
    }

}
