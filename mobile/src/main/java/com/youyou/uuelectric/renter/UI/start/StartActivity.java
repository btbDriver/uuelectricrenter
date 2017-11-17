package com.youyou.uuelectric.renter.UI.start;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.umeng.analytics.AnalyticsConfig;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.user.protobuf.bean.UserInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Network.user.SPConstant;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.UI.main.MainActivity;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.ChannelUtil;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.Support.SysConfig;
import com.youyou.uuelectric.renter.Utils.image.EmptyImageListener;

import java.io.File;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * APP启动页
 */
public class StartActivity extends BaseActivity {

    /**
     * 跳转到主界面
     */
    private static final int GOTO_MAIN = 0;

    /**
     * 跳转到引导界面
     */
    private static final int GOTO_GUIDE = 1;

    public boolean isGoToComplete = false;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            L.i("是否已经跳转完成？" + isGoToComplete);
            if (isGoToComplete) {
                return;
            }
            L.i("执行跳转逻辑...");
            isGoToComplete = true;

            switch (msg.what) {
                case GOTO_MAIN:
                    Intent mainIntent = new Intent(mContext, MainActivity.class);
                    mainIntent.putExtra("goto", userStatus);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(mainIntent);
                    finish();
                    break;
                case GOTO_GUIDE:
                    Intent guideIntent = new Intent(mContext, GuideActivity.class);
                    startActivity(guideIntent);
                    finish();
                    break;
            }

        }
    };
    ImageView channelIcon;
    int eventWhat = GOTO_MAIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewNoParent(R.layout.activity_start);

        // 已经显示过引导图
        L.i("初始化handler，准备等待3s...hehe");
        if (GuideActivity.isShowedGuide(mContext)) {
            eventWhat = GOTO_MAIN;
        } else {
            eventWhat = GOTO_GUIDE;
        }
        // 初始化状态码，并请求数据
        initBooleanFlagAndLoadData();

        channelIcon = (ImageView) findViewById(R.id.channel_icon);
        File destDir = new File(SysConfig.SD_IMAGE_PATH);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        showChannelLogo();
        // 当前页面不是从通知栏打开
        Config.isNotificationOpen = false;

        handler.sendEmptyMessageDelayed(eventWhat, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeMessages(eventWhat);
    }

    /**
     * 起始页的网络操作有三种：
     * （1）请求startQuery接口；
     * （2）请求UserInfo接口；
     * （3）获取地图页面Icon；
     */
    public static boolean isLoadStartQueryComplete = false;
    public static boolean isLoadUserInfoComplete = false;
    public Timer timer = null;
    public TimerTask timerTask = null;
    /**
     * 初始化状态码并请求数据
     */
    private void initBooleanFlagAndLoadData() {
        isLoadStartQueryComplete = false;
        isLoadUserInfoComplete = false;

        // 请求startQuery接口
        StartQueryRequest.startQueryRequest(mContext);
        // 拉取UserInfo状态
        getUserInfo();
        // 拉取地图页图标
        loadMarkerIcon();

        if (Config.isNetworkConnected(mContext)) {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (isGoToComplete) {
                        L.i("等待2s完成，取消timer任务...");
                        cancelTimer();
                        return;
                    }
                    if (isLoadStartQueryComplete && isLoadUserInfoComplete) {
                        // 已经显示过引导图
                        L.i("StartActivity中网络请求完成，执行跳转逻辑.....");
                        handler.sendEmptyMessage(eventWhat);
                        L.i("StartActivity中网络请求完成，取消timer任务...");
                        cancelTimer();
                    }
                }
            };
            timer.schedule(timerTask, 0, 100);
        }
    }

    /**
     * 取消timer任务
     */
    private void cancelTimer() {

        timer.cancel();
        timer = null;
    }


    /**
     * 显示渠道首发图
     */
    public void showChannelLogo() {
        String channel = ChannelUtil.getChannel(mContext);
        System.out.println("启动页获取到的渠道号为：" + channel);
        // 设置友盟统计的渠道号，原来是在Manifest文件中设置的meta-data，现在启动页中设置
        AnalyticsConfig.setChannel(channel);
        if ("qq".equals(channel)) {
            channelIcon.setImageResource(R.mipmap.logo_qq);
        } else if ("m360".equals(channel)) {
            channelIcon.setImageResource(R.mipmap.logo_m360);
        } else if ("baidu".equals(channel)) {
            channelIcon.setImageResource(R.mipmap.logo_baidu);
        }
    }

    /**
     * APP渠道读取
     */
    private String getChannel() {
        ApplicationInfo appInfo;
        try {
            appInfo = this.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            return appInfo.metaData.getString("UMENG_CHANNEL");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 拉取到的用户状态，默认是到地图页
     */
    public static String userStatus = MainActivity.GOTO_MAP;

    /**
     * 拉取UserInfo状态，为主界面切换做准备
     */
    private void getUserInfo() {
        if (!Config.isNetworkConnected(this)) {
            isLoadUserInfoComplete = true;
            return;
        }
        if (!UserConfig.isPassLogined()) {
            isLoadUserInfoComplete = true;
            return;
        }
        UserInterface.UserInfo.Request.Builder request = UserInterface.UserInfo.Request.newBuilder();
        request.setR((int) (System.currentTimeMillis() / 1000));
        NetworkTask task = new NetworkTask(Cmd.CmdCode.UserInfo_NL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        UserInterface.UserInfo.Response response = UserInterface.UserInfo.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            String orderId = response.getOrderId();

                            Config.setOrderId(mContext, orderId);
                            int orderStatus = response.getOrderStatus();
                            switch (orderStatus) {
                                case MainActivity.WAIT_GET_CAR:
                                    userStatus = MainActivity.GOTO_GET_CAR;
                                    break;
                                case MainActivity.CURRENT_STROKE:
                                    userStatus = MainActivity.GOTO_CURRENT_STROKE;
                                    break;
                                case MainActivity.WAIT_PAY:
                                    userStatus = MainActivity.GOTO_NEED_PAY_ORDER;
                                    break;
                                case MainActivity.OTHER_PAY:
                                    userStatus = MainActivity.GOTO_OTHER_PAY;
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
            }

            @Override
            public void networkFinish() {
                isLoadUserInfoComplete = true;
            }
        });
    }

    /**
     * startActivity屏蔽物理返回按钮
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 执行一次拉取地图也Marker图标操作
     */
    public void loadMarkerIcon() {
        if (!Config.isNetworkConnected(mContext)) {
            return;
        }
        SharedPreferences sp = mContext.getSharedPreferences(SPConstant.SP_NAME_MARKER_ICON_URLS, Context.MODE_PRIVATE);
        Set<String> urls = sp.getStringSet(SPConstant.SP_KEY_MARKER_ICON_URLS, null);
        if (urls != null && urls.size() > 0) {
            L.i("拉取SP中保存的Marker图标...");
            for (String url : urls) {
                UUApp.getMapIconImageLoader().get(url, new EmptyImageListener());
            }
        }
    }
}
