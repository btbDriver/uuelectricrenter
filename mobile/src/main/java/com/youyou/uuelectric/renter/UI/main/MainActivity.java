package com.youyou.uuelectric.renter.UI.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.uu.facade.activity.pb.common.ActivityCommon;
import com.uu.facade.activity.pb.iface.ActivityInterface;
import com.uu.facade.advertisement.protobuf.bean.AdvCommon;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.user.protobuf.bean.UserInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Network.listen.OnClickLoginedListener;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.Network.user.UserStatus;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.Service.LoopRequest;
import com.youyou.uuelectric.renter.UI.main.rentcar.CurrentStrokeFragment;
import com.youyou.uuelectric.renter.UI.main.rentcar.GetCarFragment;
import com.youyou.uuelectric.renter.UI.main.rentcar.GetCarPresenter;
import com.youyou.uuelectric.renter.UI.main.user.UserInfoActivity;
import com.youyou.uuelectric.renter.UI.order.FavourActivity;
import com.youyou.uuelectric.renter.UI.order.NeedPayOrderFragment;
import com.youyou.uuelectric.renter.UI.order.RouteActivity;
import com.youyou.uuelectric.renter.UI.start.StartQueryRequest;
import com.youyou.uuelectric.renter.UI.web.H5Activity;
import com.youyou.uuelectric.renter.UI.web.H5Constant;
import com.youyou.uuelectric.renter.UI.web.H5Fragment;
import com.youyou.uuelectric.renter.UI.web.url.URLConfig;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.DialogUtil;
import com.youyou.uuelectric.renter.Utils.SharedPreferencesUtil;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.Support.SysConfig;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;
import com.youyou.uuelectric.renter.Utils.update.UpdateManager;

import java.util.List;

import de.greenrobot.event.EventBus;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;

import static android.graphics.PixelFormat.TRANSLUCENT;

public class MainActivity extends MainLoopActivity {

    /**
     * 跳转到scheme对应的页面
     */
    public static final String GOTO_SCHEME = "GOTO_SCHEME";
    /**
     * 去地图页
     */
    public static final String GOTO_MAP = "map";
    /**
     * 取车页面的GOTO
     */
    public static final String GOTO_GET_CAR = "get_car";
    /**
     * 当前行程的GOTO
     */
    public static final String GOTO_CURRENT_STROKE = "current_stroke";
    /**
     * 支付车费的GOTO
     */
    public static final String GOTO_NEED_PAY_ORDER = "need_pay_order";
    /**
     * 其他待支付费用GOTO
     */
    public static final String GOTO_OTHER_PAY = "other_pay";

    Activity mContext;
    /**
     * 导航Header
     */
    public View headerView = null;
    /**
     * 用户头像信息
     */
    public NetworkImageView headerUserPicImage = null;
    /**
     * 用户
     */
    public TextView headerUserPhoneText = null;

    /**
     * 地图页title
     */
    private static String mapTitle;
    /**
     * 取车页title
     */
    private static String getCarTitle;
    /**
     * 当前行程页title
     */
    private static String currentStrokeTitle;
    /**
     * 支付车费页title
     */
    private static String needPayOrderTitle;
    /**
     * 行程title
     */
    private static String strokeTitle;
    /**
     * 余额title
     */
    private static String moneyTitle;
    /**
     * 优惠券title
     */
    private static String couponTitle;
    /**
     * 活动title
     */
    private static String activeTitle;
    /**
     * 邀请title
     */
    private static String shareTitle;
    /**
     * faq title
     */
    public static String faqTitle;
    /**
     * 设置title
     */
    private static String settingTitle;
    /**
     * 其他待支付费用
     */
    private static String otherPayTitle;

    // 侧导栏 余额
    public MaterialSection balanceList;
    // 摇一摇切换环境
    private SensorManager sensorManager;
    private SensorListener sensorListener;
    private LinearLayout llGetCashTip;


    private static final String TIP_IS_SHOW_CLICK = "tip_is_show_click";
    private TextView tvGetCashText;
    public static ActivityCommon.DriverLicenseActivityDescResult driverLicenseActivityDescResult;
    private LinearLayout llHeaderGetCash;
    private TextView tvHeaderGetCashText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFormat(TRANSLUCENT);
        super.onCreate(savedInstanceState);
        Config.setActivityState(this);
        EventBus.getDefault().register(this);

        setNavigationIcon(R.mipmap.ic_optionmenu_toolbar);


        // 只有在未展现过广告时，才拉取广告数据
        if (!MainMapAdManager.isSaveRecord(mContext)) {
            new LoadAdvDataManager(mContext).queryAdv();
        }

        if (SysConfig.DEVELOP_MODE) {
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            sensorListener = new SensorListener(this);
        }
        // 如果应用被异常杀死，当再次打开时，需要将首页再次设置为地图页，至于真正应该加载哪个Fragment，根据onResume中拉取到的UserInfo来判断
        if (savedInstanceState != null && savedInstanceState.getBoolean(DEAD_KEY)) {
            updateContent(GOTO_MAP);
        }
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mContext = this;

        headerView = LayoutInflater.from(this).inflate(R.layout.drawer_header, null);
        headerUserPicImage = (NetworkImageView) headerView.findViewById(R.id.header_user_pic_image);
        headerUserPhoneText = (TextView) headerView.findViewById(R.id.header_user_phone_text);
        llHeaderGetCash = (LinearLayout) headerView.findViewById(R.id.ll_header_getcash);
        tvHeaderGetCashText = (TextView) headerView.findViewById(R.id.tv_header_getcash_text);

        setDrawerHeaderCustom(headerView);
        headerView.setOnClickListener(new OnClickLoginedListener(MainActivity.this) {
            @Override
            public void onLoginedClick(View v) {
                closeDrawer();
                startActivity(new Intent(MainActivity.this, UserInfoActivity.class));
            }
        });

        initTitle();
        initDrawerSections();

        initGetCashTip();


    }


    /**
     * 初始化首页驾驶证认证领取现金
     */
    private void initGetCashTip() {
        llGetCashTip = (LinearLayout) findViewById(R.id.material_ll_getcash_tip);
        ImageView ivGetCashIcon = (ImageView) findViewById(R.id.material_iv_getcash_icon);
        tvGetCashText = (TextView) findViewById(R.id.material_tv_getcash_text);
        ImageView ivGetCashDel = (ImageView) findViewById(R.id.material_iv_getcash_del);


        llGetCashTip.setBackgroundResource(R.mipmap.ic_popuop_dropdown);
        ivGetCashIcon.setImageResource(R.mipmap.ic_gold_homeview);
        ivGetCashDel.setImageResource(R.mipmap.ic_cancel_homeview);

        ivGetCashDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setIsClickTip(true);
                //关闭的动画
                Animation outAnim = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_top_left);
                outAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        llGetCashTip.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                llGetCashTip.startAnimation(outAnim);

            }
        });
        llGetCashTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Config.isNetworkConnected(mContext)) {
                    Config.showToast(mContext, mContext.getString(R.string.network_error_tip));
                    return;
                }
                setIsClickTip(true);
                //判断活动时间是否结束
                if (driverLicenseActivityDescResult != null && System.currentTimeMillis()/1000 < driverLicenseActivityDescResult.getAtivityDeadLineTime()) {
                    llGetCashTip.setVisibility(View.GONE);
                    openDrawer();
                }else {
                    //显示活动结束弹窗提示
                    Dialog dialog = DialogUtil.getInstance(mContext).showMaterialTipDialog("抱歉", "你来晚了，活动已结束了", "好的", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            driverLicenseActivityDescResult = null;
                            llGetCashTip.setVisibility(View.GONE);
                            llHeaderGetCash.setVisibility(View.GONE);
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                }
            }
        });
    }

    /**
     * 设置是否关闭或点击了引导
     *
     * @param b
     */
    private void setIsClickTip(boolean b) {
        SharedPreferencesUtil.getSharedPreferences(mContext).putBoolean(TIP_IS_SHOW_CLICK+UserConfig.getUserInfo().getPhone(), b);
    }

    /**
     * 初始化所有的title
     */
    private void initTitle() {
        mapTitle = getString(R.string.app_name);
        getCarTitle = getString(R.string.title_activity_get_car);
        currentStrokeTitle = getString(R.string.title_activity_current_stroke);
        needPayOrderTitle = getString(R.string.title_activity_need_pay_order);
        strokeTitle = getString(R.string.stroke);
        moneyTitle = getString(R.string.money);
        couponTitle = getString(R.string.coupon);
        activeTitle = getString(R.string.active);
        shareTitle = getString(R.string.share);
        faqTitle = getString(R.string.usercarknow);
        settingTitle = getString(R.string.setting);

        otherPayTitle = getString(R.string.title_other_pay);

    }


    public void initDrawerSections() {

        Intent intent = getIntent();
        String gotoFlag = intent.getStringExtra("goto");
        //首页
        if (MainActivity.GOTO_MAP.equals(gotoFlag)) {
            this.addSection(newSection(mapTitle, new MainMapFragment()));
            //首页的导航隐藏掉
            getSectionByTitle(mapTitle).getView().setVisibility(View.GONE);
        }
        // 取车
        else if (MainActivity.GOTO_GET_CAR.equals(gotoFlag)) {
            this.addSection(newSection(getCarTitle, new GetCarFragment()));
            getSectionByTitle(getCarTitle).getView().setVisibility(View.GONE);
        }
        // 当前行程
        else if (MainActivity.GOTO_CURRENT_STROKE.equals(gotoFlag)) {
            this.addSection(newSection(currentStrokeTitle, new CurrentStrokeFragment()));
            getSectionByTitle(currentStrokeTitle).getView().setVisibility(View.GONE);
        }
        // 支付车费
        else if (MainActivity.GOTO_NEED_PAY_ORDER.equals(gotoFlag)) {
            this.addSection(newSection(needPayOrderTitle, new NeedPayOrderFragment()));
            getSectionByTitle(needPayOrderTitle).getView().setVisibility(View.GONE);
        }
        // 其他待支付费用
        else if (MainActivity.GOTO_OTHER_PAY.equals(gotoFlag)) {
            intent.putExtra(H5Constant.MURL, URLConfig.getUrlInfo().getPaymentNotice().getUrl());
            this.addSection(newSection(otherPayTitle, new H5Fragment()));
            getSectionByTitle(otherPayTitle).getView().setVisibility(View.GONE);
        }

        //行程
        this.addSection(newSection(strokeTitle, R.mipmap.ic_distance_menu, new MSectionListener(new Intent(mContext, RouteActivity.class), mContext)));

        //余额
        Intent intent1 = new Intent(mContext, H5Activity.class);
        intent1.putExtra(MSectionListener.isGotoH5Args, true);
        intent1.putExtra(MSectionListener.GotoH5, MSectionListener.GotoH5balance);
        balanceList = newSection(moneyTitle, R.mipmap.ic_money_menu, new MSectionListener(intent1, mContext));
        // 更新侧导充值栏活动文案
        if (!TextUtils.isEmpty(URLConfig.getUrlInfo().getRechargeDesc())) {
            balanceList.setSectionDesc(URLConfig.getUrlInfo().getRechargeDesc());
        }
        this.addSection(balanceList);

        // 优惠
        this.addSection(newSection(couponTitle, R.mipmap.ic_coupon_menu, new MSectionListener(new Intent(mContext, FavourActivity.class), mContext)));

        // 活动
        Intent intent2 = new Intent(mContext, H5Activity.class);
        intent2.putExtra(MSectionListener.isGotoH5Args, true);
        intent2.putExtra(MSectionListener.GotoH5, MSectionListener.GotoH5active);
        this.addSection(newSection(activeTitle, R.mipmap.ic_activity_menu, new MSectionListener(intent2, mContext)));

        // 邀请好友
        Intent intent3 = new Intent(mContext, H5Activity.class);
        intent3.putExtra(MSectionListener.isGotoH5Args, true);
        intent3.putExtra(MSectionListener.GotoH5, MSectionListener.GotoH5share);
        MaterialSection shareSection = newSection(shareTitle, R.mipmap.ic_sharefriend_menu, new MSectionListener(intent3, mContext));
        shareSection.setSectionDesc("有奖励");
        this.addSection(shareSection);

        // 用车早知道
        Intent intent4 = new Intent(mContext, H5Activity.class);
        intent4.putExtra(MSectionListener.isGotoH5Args, true);
        intent4.putExtra(MSectionListener.GotoH5, MSectionListener.GotoH5Faq);
        this.addSection(newSection(faqTitle, R.mipmap.ic_userguide_menu, new MSectionListener(intent4, mContext)));

        // 更多
        MaterialSection moreSection = newSection(settingTitle, R.mipmap.ic_more_menu, new MSectionListener(new Intent(mContext, SettingActivity.class), mContext));
        this.addSection(moreSection);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Config.currentContext = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 初始化组件，主要用于设置用户头像和手机号
        initUserView();
        // 若当前从通知栏打开的标识为true，则设置为false
        if (Config.isNotificationOpen) {
            Config.isNotificationOpen = false;
        }

        if (SysConfig.DEVELOP_MODE) {
            sensorManager.registerListener(sensorListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SysConfig.DEVELOP_MODE) {
            sensorManager.unregisterListener(sensorListener);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String gotos = getIntent().getStringExtra("goto");
        if (gotos != null && !gotos.equals("")) {
            updateContent(gotos);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        UUApp.getInstance().removeActivity(this);
    }

    /**
     * 初始化组件，主要是设置用户头像和手机号
     */
    public void initUserView() {
        // 判断用户是否已经登录，若已登录，则设置头像和手机号
        if (UserConfig.isPassLogined()) {
            headerUserPicImage.setBackgroundResource(R.mipmap.ic_criclepic_menu);
            headerUserPhoneText.setText(UserConfig.getUserInfo().getDisplayName());
        } else {
            headerUserPicImage.setBackgroundResource(R.mipmap.ic_criclepic_menu);
            headerUserPhoneText.setText("尚未登录，请登录");
            switchRedPoint();
        }
    }

    /**
     * 更换主界面
     *
     * @param gotos
     */
    public synchronized void updateContent(String gotos) {
        MaterialSection currentSection = getCurrentSection();
        String currentTitle = currentSection.getTitle();
        Fragment currentFragment = null;
        if (currentSection.getTarget() == MaterialSection.TARGET_FRAGMENT) {
            currentFragment = (Fragment) currentSection.getTargetFragment();
        }
        // gotos对应的Fragment和currentTitle不对应时，才检查侧导如果打开，则自动关闭
        if (!currentTitle.equals(parseGoto2Title(gotos))) {
            if (isDrawerOpen()) {
                closeDrawer();
            }
        }
        // 替换主界面显示的Fragment，需要根据title判断并进行替换
        if (gotos.equals(GOTO_GET_CAR)) {
            if (!getCarTitle.equals(currentTitle) || (currentFragment != null && !currentFragment.isAdded())) {
                MaterialSection newSection = newSection(getCarTitle, new GetCarFragment());
                switchSection(currentSection, newSection);
            }
        } else if (gotos.equals(GOTO_CURRENT_STROKE)) {
            if (!currentStrokeTitle.equals(currentTitle) || (currentFragment != null && !currentFragment.isAdded())) {
                MaterialSection newSection = newSection(currentStrokeTitle, new CurrentStrokeFragment());
                switchSection(currentSection, newSection);
            }
        } else if (gotos.equals(GOTO_NEED_PAY_ORDER)) {
            if (!needPayOrderTitle.equals(currentTitle) || (currentFragment != null && !currentFragment.isAdded())) {
                MaterialSection newSection = newSection(needPayOrderTitle, new NeedPayOrderFragment());
                switchSection(currentSection, newSection);
            }
        } else if (gotos.equals(GOTO_MAP)) {
            if (!mapTitle.equals(currentTitle) || (currentFragment != null && !currentFragment.isAdded())) {
                MaterialSection newSection = newSection(mapTitle, new MainMapFragment());
                switchSection(currentSection, newSection);
            }
        } else if (gotos.equals(GOTO_OTHER_PAY)) {
            if (!otherPayTitle.equals(currentTitle) || (currentFragment != null && !currentFragment.isAdded())) {
                getIntent().putExtra(H5Constant.MURL, URLConfig.getUrlInfo().getPaymentNotice().getUrl());
                MaterialSection newSection = newSection(otherPayTitle, new H5Fragment());
                switchSection(currentSection, newSection);
            }
        }
        // App退出后台，接收发送取消订单事件，再次回到前台，则显示取消订单消息弹窗
        if (isShowCancelOrderDialog) {
            isShowCancelOrderDialog = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Config.showTiplDialog(mContext, null, showCancelOrderDialogString, "我知道了", null);
                }
            }, 2000);
        }
        switchRedPoint();
    }

    public boolean isShowCancelOrderDialog = false;// 是否显示取消订单弹窗
    public String showCancelOrderDialogString = "";// 取消弹窗显示内容

    /**
     * 切换当前主界面
     *
     * @param currentSection
     * @param newSection
     */
    private void switchSection(MaterialSection currentSection, MaterialSection newSection) {
        newSection.getView().setVisibility(View.GONE);
        addSection(newSection);
        setSection(newSection);
        removeSection(currentSection);
    }

    /**
     * 根据goto参数查询其对应的title
     *
     * @param gotos
     * @return
     */
    private String parseGoto2Title(String gotos) {
        String title = "";
        if (GOTO_GET_CAR.equals(gotos)) {
            title = getCarTitle;
        } else if (GOTO_CURRENT_STROKE.equals(gotos)) {
            title = currentStrokeTitle;
        } else if (GOTO_NEED_PAY_ORDER.equals(gotos)) {
            title = needPayOrderTitle;
        } else if (GOTO_OTHER_PAY.equals(gotos)) {
            title = otherPayTitle;
        } else if (GOTO_MAP.equals(gotos)) {
            title = mapTitle;
        }
        return title;
    }

    /**
     * 根据条件判断侧倒中的item已经主页面左上角的图标上是否显示红点
     */
    private void switchRedPoint() {
        MaterialSection settingSection = getSectionByTitle(settingTitle);
        // 【更多】是否显示红点
        if (Config.otherFee == 0 && Config.trafficFee == 0) {
            settingSection.setNotificationReadPointVisible(false);
        } else {
            settingSection.setNotificationReadPointVisible(true);
        }

        boolean hasReadPoint = false;
        // 遍历所有侧导中的item，看其中是否有显示红点的item，如果有，则在主页面左上角的图标上也显示红点
        List<MaterialSection> sectionList = getSectionList();
        for (MaterialSection section : sectionList) {
            if (section.notificationReadPointIsShowing()) {
                hasReadPoint = true;
                break;
            }
        }
        if (hasReadPoint) {
            setNavigationIcon(R.mipmap.ic_optionmenu_toolbar_redpoint);
        } else {
            setNavigationIcon(R.mipmap.ic_optionmenu_toolbar);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return Config.onKeyDown(keyCode, event, this);
    }

    /**
     * event 更改显示登陆状态
     */
    public void onEventMainThread(BaseEvent event) {
        if (event == null || TextUtils.isEmpty(event.getType()))
            return;
        // 更改View，显示用户状态
        if (event.getType().equals(EventBusConstant.EVENTBUS_MAINACTIVITY_SHOWLOGIN)) {
            initUserView();
        }
        // 监听网络状态
        else if (EventBusConstant.EVENT_TYPE_NETWORK_STATUS.equals(event.getType())) {
            String status = (String) event.getExtraData();
            // 当网络状态变化的时候请求startQuery接口
            if (status != null && status.equals("open")) {
                L.i("网络已连接，请求startQuery接口...");
                // 请求网络，调用startQuery接口
                StartQueryRequest.startQueryRequest(mContext);
            }
            // 只有当前页面为地图页面的时候拉取用户信息,否则导致在网络变化时拉取UserInfo与开关车门跳转等操作冲突
            if (status != null && status.equals("open") && getCurrentSection().getTitle().equals(mapTitle)) {
                L.i("网络已连接，拉取最新数据...");
                // 请求网络，获取用户信息
                startLoop();
            }
        }
        // VolleyNetworkHelper网络请求过程中发生异常，发送弹窗事件
        else if (EventBusConstant.EVENT_TYPE_NETWORK_TOLOGIN.equals(event.getType())) {
            if (Config.currentContext != null && this.getClass().getSimpleName().trim().equals(Config.currentContext.getClass().getSimpleName())) {
                String content = (String) event.getExtraData();
                UserConfig.goToErrorLoginDialog(mContext, content);
            }
        }
        // 解析长连接或者是轮询拉取到的数据
        else if (EventBusConstant.EVENT_TYPE_LONGCONNECTION_LOOP.equals(event.getType())) {
            if (Config.currentContext != null && this.getClass().getSimpleName().trim().equals(Config.currentContext.getClass().getSimpleName())) {
                L.d("在mainActivity中解析长连接拉取到的数据");
                // 解析长连接和轮询拉取到的数据
                LoopRequest.parserLongConnResult(event.getExtraData(), mContext);
            }
        }
        // 处理广告弹窗事件
        else if (EventBusConstant.EVENT_TYPE_QUERY_ADV.equals(event.getType())) {
            Object object = event.getExtraData();
            disposeAdEvent(object);
        }
        // 更新侧导栏（余额）文案
        else if (EventBusConstant.EVENT_TYPE_REFRESH_NAVIGATION.equals(event.getType())) {
            // 当前侧导栏已经初始化完成
            if (balanceList != null) {
                L.i("接收到event事件，更新通知栏（余额）文案...");
                Object object = event.getExtraData();
                balanceList.setSectionDesc(object.toString());
            }
        }
        //退出登录、被挤下线的事件（清除掉上传驾照引导tip）
        else if (EventBusConstant.EVENT_TYPE_CLOSE_CANCEL_DIALOG.equals(event.getType())) {
            driverLicenseActivityDescResult = null;
            if (llGetCashTip.getVisibility() == View.VISIBLE) {
                llGetCashTip.setVisibility(View.GONE);
            }
            llHeaderGetCash.setVisibility(View.GONE);
        }
        // 当前超时等待类型（免费等待，超时等待）
        else if (EventBusConstant.EVENT_TYPE_UPDATE_WAIT_TYPE.equals(event.getType())) {
            countDownType = (int)event.getExtraData();
        }
        // App退出后台发送取消订单事件
        else if (EventBusConstant.EVENT_TYPE_CANCEL_DIALOG.equals(event.getType())) {
            isShowCancelOrderDialog = true;
            showCancelOrderDialogString = event.getExtraData().toString();
        }
    }

    /**
     * 处理广告事件，根据不同情况，判断是否显示或隐藏广告弹窗
     *
     * @param object
     */
    private void disposeAdEvent(Object object) {
        if (object != null) {
            L.i("接收到展示广告事件，准备展现广告信息...");
            List<AdvCommon.AdvInfo> advInfoListList = (List<AdvCommon.AdvInfo>) object;
            if (advInfoListList != null && advInfoListList.size() > 0) {
                MainMapAdManager adManager = new MainMapAdManager(mContext, advInfoListList);
                adManager.showAdDialog();
            }
        }
    }


    /**
     * 待取车
     */
    public static final int WAIT_GET_CAR = 1;
    /**
     * 行程中
     */
    public static final int CURRENT_STROKE = 2;
    /**
     * 待支付
     */
    public static final int WAIT_PAY = 4;
    /**
     * 其他待支付费用
     */
    public static final int OTHER_PAY = 20;

    /**
     * 解析请求到的用户状态信息
     *
     * @param response
     */
    @Override
    public void parserUserInfoResult(UserInterface.UserInfo.Response response) {
        // 更新用户信息(内存和SP中)
        UserConfig.updateUserInfoToBean(response, UserConfig.getUserInfo());
        // 更新用户信息界面
        EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENTBUS_MAINACTIVITY_SHOWLOGIN));
        String orderId = response.getOrderId();

        Config.setOrderId(mContext, orderId);
        int orderStatus = response.getOrderStatus();

        int userStatus = UserConfig.getUserInfo().getUserStatus();
        if (userStatus == UserStatus.NEW_REG ) {
            //触发网络请求获取驾照活动引导
            getDriverLicenseActivityFilter();
        }else{
            driverLicenseActivityDescResult = null;
            if (llGetCashTip.getVisibility() == View.VISIBLE) {
                llGetCashTip.setVisibility(View.GONE);
            }
            llHeaderGetCash.setVisibility(View.GONE);
        }

        // 是否需要显示其他待支付费用或者违章费用红点
//        Config.otherFee = response.getOtherFee();
//        Config.trafficFee = response.getTrafficFee();

        // 首页切换其他Fragment时，才删除广告弹窗
        /*if (orderStatus == WAIT_GET_CAR || orderStatus == CURRENT_STROKE || orderStatus == WAIT_PAY || orderStatus == OTHER_PAY) {
            MainMapAdManager.removeAdDialog(mContext);
        }*/

        switch (orderStatus) {
            case WAIT_GET_CAR:
                updateContent(GOTO_GET_CAR);
                llGetCashTip.setVisibility(View.GONE);
                break;
            case CURRENT_STROKE:
                updateContent(GOTO_CURRENT_STROKE);
                llGetCashTip.setVisibility(View.GONE);
                break;
            case WAIT_PAY:
                updateContent(GOTO_NEED_PAY_ORDER);
                llGetCashTip.setVisibility(View.GONE);
                break;
            case OTHER_PAY:
                updateContent(GOTO_OTHER_PAY);
                llGetCashTip.setVisibility(View.GONE);
                break;
            default:
                // 若用户没有待取车，进行中，待支付订单，则检测是否是要版本更新
                UpdateManager.queryAppBaseVersionInfo(mContext, true, false);
                updateContent(GOTO_MAP);
                openDriverLicenseActivityFilterView();
                break;
        }
    }

    /**
     * 请求驾照认证引导
     */
    private void getDriverLicenseActivityFilter() {
        ActivityInterface.DriverLicenseActivityFilter.Request.Builder request = ActivityInterface.DriverLicenseActivityFilter.Request.newBuilder();
        NetworkTask task = new NetworkTask(Cmd.CmdCode.DriverLicenseActivityFilter_NL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {

            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        ActivityInterface.DriverLicenseActivityFilter.Response response = ActivityInterface.DriverLicenseActivityFilter.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            driverLicenseActivityDescResult = response.getRescResult();
                            llHeaderGetCash.setVisibility(View.VISIBLE);
                            tvHeaderGetCashText.setText(driverLicenseActivityDescResult.getSideBarDesc());
                            openDriverLicenseActivityFilterView();
                        } else {
                            driverLicenseActivityDescResult = null;
                            if (llGetCashTip.getVisibility() == View.VISIBLE) {
                                llGetCashTip.setVisibility(View.GONE);
                            }
                            llHeaderGetCash.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        driverLicenseActivityDescResult = null;
                        if (llGetCashTip.getVisibility() == View.VISIBLE) {
                            llGetCashTip.setVisibility(View.GONE);
                        }
                        llHeaderGetCash.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                driverLicenseActivityDescResult = null;
                if (llGetCashTip.getVisibility() == View.VISIBLE) {
                    llGetCashTip.setVisibility(View.GONE);
                }
                llHeaderGetCash.setVisibility(View.GONE);
            }

            @Override
            public void networkFinish() {

            }
        });
    }

    private void openDriverLicenseActivityFilterView() {
        if (!SharedPreferencesUtil.getSharedPreferences(mContext).getBoolean(TIP_IS_SHOW_CLICK+UserConfig.getUserInfo().getPhone(), false)) {
            if (mapTitle.equals(getCurrentSection().getTitle()) && driverLicenseActivityDescResult != null) {
                if (llGetCashTip.getVisibility() == View.GONE) {
                    tvGetCashText.setText(driverLicenseActivityDescResult.getHomePageDesc());
                    //打开的动画
                    Animation inAnim = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_top_left);
                    inAnim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            llGetCashTip.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    llGetCashTip.startAnimation(inAnim);
                }
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // 只有测导打开，或者非地图页时才进行双击事件拦截，否则会影响地图的双击缩放功能
            if (isDrawerOpen() || !mapTitle.equals(getCurrentSection().getTitle())) {
                if (Config.isFastDoubleClick()) {
                    return true;
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }


    // 判断当前点击我知道了，跳转页面
    public int countDownType = GetCarPresenter.COUNT_DOWN_FREE;
    /**
     * 订单自动取消弹窗中的按钮点击监听，该弹窗监听本在GetCarFragment中，如果由于倒计时结束时，APP刚好从后台切换到前台
     * 此时MainActivity会拉取状态切换Fragment为地图Fragment，切换后点击弹窗按钮会报非法状态异常，因为GetCarFragment已经被销毁，
     * 所以将此监听放在MainActivity中声明
     */
    public View.OnClickListener cancelOrderListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(mContext, MainActivity.class);
            // 若当前订单ID为空,则说明用户待支付信息被支付,跳转地图页面
            if (countDownType == GetCarPresenter.COUNT_DOWN_FREE || TextUtils.isEmpty(Config.getOrderId(mContext))) {
                intent.putExtra("goto", MainActivity.GOTO_MAP);
            } else {
                intent.putExtra("goto", MainActivity.GOTO_NEED_PAY_ORDER);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
    };
}
