package com.youyou.uuelectric.renter.Utils;

import android.content.Context;
import android.location.LocationManager;

/**
 * User: qing
 * Date: 2015/9/8 11:59
 * Desc: 定位相关工具类
 */
public class LocationUtils {


    /**
     * 判断GPS是否开启
     *
     * @param context
     * @return true 表示开启
     */
    public static final boolean isGPSOpen(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        }
        return false;
    }
}
