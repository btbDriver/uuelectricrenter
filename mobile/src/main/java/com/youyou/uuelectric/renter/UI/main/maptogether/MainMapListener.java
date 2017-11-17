package com.youyou.uuelectric.renter.UI.main.maptogether;

import android.app.Activity;
import android.os.Bundle;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.model.NaviLatLng;
import com.uu.facade.dot.protobuf.iface.DotInterface;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.main.MainMapFragment;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.map.EmptyAMapLocationListener;
import com.youyou.uuelectric.renter.Utils.map.EmptyAMapNaviListener;

import java.util.List;

/**
 * Created by aaron on 16/6/1.
 * 地图页面事件监听
 */
public class MainMapListener implements AMap.OnMarkerClickListener
                        ,AMap.OnMapClickListener{

    // ################  初始化方法 start ############################
    private MainMapFragment mainMapFragment;
    private Activity mContext;

    public MainMapListener(MainMapFragment mainMapFragment, Activity mContext) {
        this.mainMapFragment = mainMapFragment;
        this.mContext = mContext;
    }
    // ################# 初始化方法 end ##############################




    // ############### 监听marker点击事件 start ########################
    @Override
    public boolean onMarkerClick(Marker marker) {
        Object object = marker.getObject();
        if (mainMapFragment.aMap != null && object != null && object instanceof DotInterface.DotInfo) {
            // 删除掉路线规划时地图自己产生的Marker，此Marker的object为null，网点Marker的object一定不空
            List<Marker> mapScreenMarkers = mainMapFragment.aMap.getMapScreenMarkers();
            if (mapScreenMarkers != null) {
                for (Marker m : mapScreenMarkers) {
                    if (m.getObject() == null) {
                        m.remove();
                    }
                }
            }
            mainMapFragment.jumpPoint(marker, true);
        }
        return true;
    }
    // ############## 监听marker点击事件 end ##########################






    // ############# 监听地图点击事件 start ###########################

    @Override
    public void onMapClick(LatLng latLng) {
        // 还原选中的Marker的上一次的显示状态
        mainMapFragment.restoreClickMarker(latLng);
        // 恢复顶部地址栏显示文案
        mainMapFragment.restoreTopAddress();
        // 隐藏底部车辆详情视图
        mainMapFragment.hideBottomContent();
    }

    // ############# 监听地图点击事件 end  ############################




    // ############ 设置定位监听 start #############################
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mAMapLocationClient;

    public LocationSource locationSource = new LocationSource() {
        @Override
        public void activate(OnLocationChangedListener listener) {
            mListener = listener;
            if (mAMapLocationClient == null) {
                mAMapLocationClient = new AMapLocationClient(mContext.getApplicationContext());

                mAMapLocationClient.setLocationListener(locationListener);

                //初始化定位参数
                AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
                //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
                mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                //设置是否返回地址信息（默认返回地址信息）
                mLocationOption.setNeedAddress(true);
                //设置是否只定位一次,默认为false
                mLocationOption.setOnceLocation(false);
                //设置是否强制刷新WIFI，默认为强制刷新
                mLocationOption.setWifiActiveScan(true);
                //设置是否允许模拟位置,默认为false，不允许模拟位置
                mLocationOption.setMockEnable(false);
                //设置定位间隔,单位毫秒,默认为2000ms
                mLocationOption.setInterval(60 * 1000);
                //给定位客户端对象设置定位参数
                mAMapLocationClient.setLocationOption(mLocationOption);
                //启动定位
                mAMapLocationClient.startLocation();
            }
        }

        @Override
        public void deactivate() {
            mListener = null;
            if (mAMapLocationClient != null) {
                mAMapLocationClient.stopLocation();
                mAMapLocationClient.onDestroy();
            }
            mAMapLocationClient = null;
        }
    };

    private Object locationLock = new Object();

    /**
     * 定位的监听Listener
     */
    private AMapLocationListener locationListener = new EmptyAMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aLocation) {

            if (mListener != null && aLocation != null) {

                synchronized (locationLock) {
                    if (aLocation.getLatitude() != 0 && aLocation.getLongitude() != 0) {
                        Config.lat = aLocation.getLatitude();
                        Config.lng = aLocation.getLongitude();
                        Config.cityCode = aLocation.getCityCode();
                        Bundle locBundle = aLocation.getExtras();
                        Config.currentCity = aLocation.getCity();
                        Config.currentAddress = locBundle.getString("desc");
                        L.i("定位成功...cityCode:" + Config.cityCode + "\t currentLat:" + Config.lat + "\t currentLng:" + Config.lng + "\t address:" + Config.currentAddress);

                        if (Config.lat == 0 || Config.lng == 0) {
                            // 定位未成功时，查看上一次定位是否成功，如果上一次定位成功，使用上一次定位的经纬度坐标
                            if (mainMapFragment.mNaviStart != null) {
                                L.i("定位未能获取正确的经纬度，使用上一次的位置的经纬度数据...");
                                Config.lat = mainMapFragment.mNaviStart.getLatitude();
                                Config.lng = mainMapFragment.mNaviStart.getLongitude();
                            }
                        } else {
                            mainMapFragment.mNaviStart = new NaviLatLng(aLocation.getLatitude(), aLocation.getLongitude());
                        }

                        // 设置定位的大头针Marker
                        if (mainMapFragment.locationMarker != null) {
                            mainMapFragment.locationMarker.remove();
                            mainMapFragment.locationMarker.destroy();
                        }
                        MarkerOptions options = new MarkerOptions();
                        options.anchor(0.5f, 1.0f);
                        options.position(new LatLng(Config.lat, Config.lng));
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_mylocation));
                        mainMapFragment.locationMarker = mainMapFragment.aMap.addMarker(options);
                        mainMapFragment.locationMarker.setObject(mainMapFragment.locationTag);
                        mainMapFragment.locationMarker.setZIndex(10);

                        // 根据标识，移动地图视角，高德地图会在设置的时间间隔内重新定位，这种情况不是手动触发的定位，只需要更新大头针即可，不用移动地图视角
                        if (mainMapFragment.isMoveCamera) {
                            mainMapFragment.aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(Config.lat, Config.lng), mainMapFragment.INTZOOM, mainMapFragment.MAP_TILT, 0)), 500, null);
                            mainMapFragment.isMoveCamera = false;

                            // 不是点击定位按钮触发的定位操作时，才请求最新数据
                            if (!mainMapFragment.isClickLocationButton) {
                                // 定位成功后，拉取网点数据
                                mainMapFragment.loadData();
                            } else {
                                mainMapFragment.dismissProgress();
                                mainMapFragment.isClickLocationButton = false;
                            }
                        } else {
                            L.i("本次定位是高德内部根部时间间隔或者距离进行的一次定位，只做刷新大头针处理，不做其他业务逻辑");
                        }
                        mainMapFragment.isFirstLocation = false;
                    } else {
                        if (mainMapFragment.isFirstLocation) {
                            mainMapFragment.isFirstLocation = false;
                            mainMapFragment.isFirstRequestSuccess = false;
                            // 延迟5S重新拉取数据
                            mainMapFragment.loopRequestHandler.sendEmptyMessageDelayed(mainMapFragment.LOOP_WHAT, mainMapFragment.ERROR_INTERVAL);
                            mainMapFragment.showNetworkErrorSnackBarMsg();
                        }
                    }

                }

            }
        }
    };

    // ########### 设置定位监听 end ###############################

}
