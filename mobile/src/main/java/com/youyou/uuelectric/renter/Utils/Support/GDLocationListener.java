package com.youyou.uuelectric.renter.Utils.Support;

import android.os.Bundle;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;


public class GDLocationListener implements AMapLocationListener {
    LocationListener listener;

    public GDLocationListener(LocationListener listener) {
        this.listener = listener;
    }

    @Override
    public void onLocationChanged(AMapLocation location) {
        if (location != null) {
            Config.locationTime = System.currentTimeMillis();
            double geoLat = location.getLatitude();
            double geoLng = location.getLongitude();
            if (geoLat != 0) {
                Config.lat = geoLat;
            }
            if (geoLng != 0) {
                Config.lng = geoLng;
            }
            Bundle locBundle = location.getExtras();
            Config.currentCity = location.getCity();
            Config.currentAddress = locBundle.getString("desc");
            if (Config.currentCity == null) {
                Config.currentCity = "北京市";
            }
            if (Config.currentAddress == null) {
                Config.currentAddress = "";
            }
            if (locBundle != null) {
                Config.cityCode = locBundle.getString("citycode");
            }
            if (listener != null) {
                listener.locationSuccess(geoLat, geoLng, locBundle.getString("desc"));
            }
            String str = ("定位成功:(" + geoLng + "," + geoLat + ")" + "\n精    度    :" + location.getAccuracy() + "米" + "\n定位方式:" + location.getProvider() + "\n城市编码:" + Config.cityCode + "\n位置描述:" + Config.currentAddress + "\n省:" + location.getProvince() + "\n市：" + location.getCity() + "\n区(县)：" + location.getDistrict() + "\n城市编码：" + location.getCityCode() + "\n区域编码：" + location.getAdCode());
            L.i(str);
        } else {
            if (listener != null) {
                listener.locationSuccess(0, 0, "");
            }
        }
        // 此处销毁定位对象后会导致紧跟着的一次定位(定位时间相差特别近)拿不到返回结果，所以不做销毁处理
        Config.disableMyLocation(this);

    }
}
