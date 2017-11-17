package com.youyou.uuelectric.renter.UI.main.maptogether;

import com.youyou.uuelectric.renter.UI.main.MainMapFragment;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.map.EmptyAMapNaviListener;

/**
 * Created by aaron on 16/6/1.
 * 地图页面导航监听
 */
public class NavMainListener extends EmptyAMapNaviListener {

    private MainMapFragment mainMapFragment = null;

    public NavMainListener(MainMapFragment mainMapFragment) {
        this.mainMapFragment = mainMapFragment;
    }

    @Override
    public void onCalculateRouteSuccess() {
        L.i("路线规划成功...");
        mainMapFragment.naviPath = mainMapFragment.mAMapNavi.getNaviPath();
        if (mainMapFragment.naviPath == null) {
            mainMapFragment.changeHasAddressView(2, null);
            return;
        }
        mainMapFragment.changeHasAddressView(1, mainMapFragment.naviPath);
    }

    @Override
    public void onCalculateRouteFailure(int i) {
        L.i("路线规划出错...");
        mainMapFragment.changeHasAddressView(2, null);
    }

    @Override
    public void onInitNaviFailure() {
        L.i("路线规划出错...");
        mainMapFragment.changeHasAddressView(2, null);
    }
}
