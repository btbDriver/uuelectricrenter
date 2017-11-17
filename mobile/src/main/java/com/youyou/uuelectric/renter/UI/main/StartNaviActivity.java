package com.youyou.uuelectric.renter.UI.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.model.NaviLatLng;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.Utils.DialogUtil;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.Support.LocationListener;
import com.youyou.uuelectric.renter.Utils.map.EmptyAMapNaviListener;
import com.youyou.uuelectric.renter.Utils.map.TTSController;

import java.util.ArrayList;

/**
 * 执行导航操作Activity
 */
public class StartNaviActivity extends Activity {

    /**
     * 步行导航
     */
    public static final int TYPE_WALK = 0;

    /**
     * 驾车导航
     */
    public static final int TYPE_DRIVE = 1;

    private boolean mIsCalculateRouteSuccess = false;
    private AMapNaviView mAmapAMapNaviView;
    private AMapNavi mAMapNavi;
    // 起点终点坐标
    private NaviLatLng mNaviStart;
    private NaviLatLng mNaviEnd;

    // 起点终点列表
    private ArrayList<NaviLatLng> mStartPoints = new ArrayList<NaviLatLng>();
    private ArrayList<NaviLatLng> mEndPoints = new ArrayList<NaviLatLng>();
    /**
     * 导航方式
     */
    private int type = TYPE_WALK;
    private TTSController ttsManager;

    private Context mContext;


    //-------------------------------生命周期方法start -----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_navi);
        mContext = this;
        initView(savedInstanceState);

        initArgs();

        initLocation();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAmapAMapNaviView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAmapAMapNaviView.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        mAmapAMapNaviView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAmapAMapNaviView.onDestroy();
        AMapNavi.getInstance(mContext).stopNavi();
        //删除监听
        AMapNavi.getInstance(mContext).removeAMapNaviListener(aMapNaviListener);
        AMapNavi.getInstance(mContext).removeAMapNaviListener(ttsManager);
        AMapNavi.getInstance(mContext).destroy();

        // 关闭语音
        TTSController.getInstance(this.getApplicationContext()).stopSpeaking();

    }

    //-------------------------------生命周期方法end -----------------------------------


    //-------------------------------初始化方法start -----------------------------

    /**
     * 获取传入的网点坐标点
     */
    private void initArgs() {
        Intent intent = getIntent();
        if (intent.hasExtra(IntentConfig.KEY_END_LAT_LNG)) {
            mNaviEnd = intent.getParcelableExtra(IntentConfig.KEY_END_LAT_LNG);
            mEndPoints.add(mNaviEnd);
        }
        type = intent.getIntExtra(IntentConfig.KEY_NAV_TYPE, TYPE_WALK);
    }

    private void initView(Bundle savedInstanceState) {
        mAMapNavi = AMapNavi.getInstance(mContext);
        mAMapNavi.setAMapNaviListener(aMapNaviListener);
        mAmapAMapNaviView = (AMapNaviView) findViewById(R.id.navi_map);
        mAmapAMapNaviView.onCreate(savedInstanceState);
        mAmapAMapNaviView.setAMapNaviViewListener(aMapNaviViewListener);

        ttsManager = TTSController.getInstance(this.getApplicationContext());
        ttsManager.init();
        AMapNavi.getInstance(getApplicationContext()).setAMapNaviListener(ttsManager);// 设置语音模块播报
    }

    public static final int CALCULATE_MODE_FOOT = 2;
    public static final int CALCULATE_MODE_DRIVE = 3;
    public int calculateMode = CALCULATE_MODE_FOOT;

    /**
     * 定位用户当前位置
     */
    private void initLocation() {
        Config.getCoordinates(this, new LocationListener() {
            @Override
            public void locationSuccess(double lat, double lng, String addr) {
                if (lat != 0 && lng != 0) {
                    mNaviStart = new NaviLatLng(lat, lng);
                    mStartPoints.add(mNaviStart);

                    L.i("定位成功，可以开始路线规划...");

                    if (mNaviEnd == null) {
                        naviError("系统繁忙，路线规划出错，请退出后重新进入导航。");
                        return;
                    }
                    if (type == TYPE_WALK) {
                        calculateFootRoute();
                        calculateMode = CALCULATE_MODE_FOOT;
                    } else if (type == TYPE_DRIVE) {
                        calculateDriveRoute();
                        calculateMode = CALCULATE_MODE_DRIVE;
                    }
                } else {
                    naviError("系统繁忙，路线规划出错，请退出后重新进入导航。");
                }
            }
        });
    }

    //-------------------------------初始化方法end -----------------------------


    //------------------------------- 其他方法start -----------------------------

    /**
     * 开始导航
     */
    private void startNavi() {
        // 开启导航
        AMapNavi.getInstance(mContext).startNavi(AMapNavi.GPSNaviMode);
        TTSController.getInstance(this.getApplicationContext()).startSpeaking();
    }

    /**
     * 导航出错，关闭导航页
     */
    private void naviError(String msg) {
        Dialog dialog = DialogUtil.getInstance(this).showMaterialTipDialog("温馨提示", msg, "确定", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * 计算驾车路线
     */
    private void calculateDriveRoute() {
        boolean isSuccess = mAMapNavi.calculateDriveRoute(mStartPoints,
                mEndPoints, null, AMapNavi.DrivingDefault);
    }

    /**
     * 计算步行路线
     */
    private void calculateFootRoute() {
        boolean isSuccess = mAMapNavi.calculateWalkRoute(mNaviStart, mNaviEnd);


    }


    //------------------------------- 其他方法end -----------------------------


    //-----------------------------------导航相关监听start ----------------------

    /**
     * 路线规划监听
     */
    private EmptyAMapNaviListener aMapNaviListener = new EmptyAMapNaviListener() {
        @Override
        public void onCalculateRouteFailure(int arg0) {
//            showToast("路径规划出错" + arg0);
            L.i("aMapNaviListener----->路线规划失败");
            mIsCalculateRouteSuccess = false;
            naviError("系统繁忙，路线规划出错，请退出后重新进入导航。");
        }

        @Override
        public void onCalculateRouteSuccess() {
            L.i("aMapNaviListener----->路线规划成功");
            mIsCalculateRouteSuccess = true;
            Config.reportLog(calculateMode, Config.getOrderId(mContext) + "\\t" + mAMapNavi.getNaviPath().getAllLength());
            // 路线规划成功，开启导航
            startNavi();
        }
    };


    /**
     * 导航视图监听
     */
    private AMapNaviViewListener aMapNaviViewListener = new AMapNaviViewListener() {
        @Override
        public void onNaviSetting() {

        }

        /**
         * 导航界面返回按钮监听
         */
        @Override
        public void onNaviCancel() {
            finish();
        }

        @Override
        public void onNaviMapMode(int i) {

        }

        @Override
        public void onNaviTurnClick() {

        }

        @Override
        public void onNextRoadClick() {

        }

        @Override
        public void onScanViewButtonClick() {

        }

        @Override
        public void onLockMap(boolean b) {

        }
    };

    //-----------------------------------导航相关监听end -------------------------------------

}
