package com.youyou.uuelectric.renter.Utils.Support;


public interface LocationListener
{
    /**
     * 获取经纬度成功
     *
     * @param lat
     * @param lng
     */
    public void locationSuccess(double lat, double lng, String addr);

}
