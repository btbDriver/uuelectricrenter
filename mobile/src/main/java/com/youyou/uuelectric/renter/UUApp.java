package com.youyou.uuelectric.renter;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.support.multidex.MultiDexApplication;
import android.util.DisplayMetrics;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.orhanobut.logger.LogLevel;
import com.umeng.analytics.MobclickAgent;
import com.youyou.uuelectric.renter.Network.LruImageCache;
import com.youyou.uuelectric.renter.Network.MapIconLruImageCache;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.user.SPConstant;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.Service.LongConnService;
import com.youyou.uuelectric.renter.UI.web.url.URLConfig;
import com.youyou.uuelectric.renter.Utils.ChannelUtil;
import com.youyou.uuelectric.renter.Utils.DisplayUtil;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.Support.SysConfig;
import com.youyou.uuelectric.renter.Utils.andfix.AndfixManager;
import com.youyou.uuelectric.renter.Utils.logcrash.LogCrashHandler;
import com.youyou.uuelectric.renter.Utils.observer.ObserverManager;
import com.youyou.uuelectric.renter.Utils.update.DownloadNotification;
import com.youyou.uuelectric.renter.Utils.volley.toolbox.VolleyHurlStack;
import com.youyou.uuelectric.renter.pay.MD5;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by Administrator on 08/28 028.
 */
public class UUApp extends MultiDexApplication {
    private volatile static UUApp instance;
    private static Context context;
    public String tag = "UUApp";
    private List<Activity> activityList = new LinkedList<Activity>();
    private static final Object lockObj = new Object();
    private volatile static RequestQueue requestQueue;
    public static volatile ImageLoader mCommonImageLoader;
    public static volatile ImageLoader mMapIconImageLoader;
    // 通知管理类
    public static NotificationManager notificationManager = null;


    // 单例模式中获取唯一的MyApplication实例
    public static UUApp getInstance() {
        if (instance == null) {
            synchronized (UUApp.class) {
                if (instance == null) {
                    instance = new UUApp();
                }
            }
        }
        return instance;
    }

    public Context getContext() {
        if (context == null) {
            context = getApplicationContext();
        }
        if (context == null) {
            context = getInstance();
        }
        return context;
    }

    public Activity getActivity() {
        return Config.currentContext;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        // 使用集成测试服务
        // MobclickAgent.setDebugMode(true);
        // 禁止默认的页面统计方式，这样将不会再自动统计Activity。
        MobclickAgent.openActivityDurationTrack(false);
        // 发送策略定义了用户由统计分析SDK产生的数据发送回友盟服务器的频率。需要在程序的入口 Activity 中添加
        MobclickAgent.updateOnlineConfig(this);
        instance = this;
        L.init().setMethodOffset(1);
        // 在debug下，才显示log
        L.init().setLogLevel(BuildConfig.DEBUG ? LogLevel.FULL : LogLevel.NONE);

        initDisplayOpinion();
        // 初始化UA信息
        String ua = initUA();
        // 初始化UUID
        initUUID(ua, getContext());
        // 初始化IP环境
        if (SysConfig.DEVELOP_MODE) {
            initDevelopMode();
        }
        if (SysConfig.DEVELOP_MODE) {
            L.i("初始化内存溢出检测...");
            // 内存泄露监测
            // LeakCanary.install(this);
            // 异常检测
            // CrashWoodpecker.fly().to(this);
        }
        // 初始化全局异常捕获
        if (!SysConfig.DEVELOP_MODE) {
            LogCrashHandler.getInstance().init(this);
        }
        // 初始化用户信息
        UserConfig.init(getContext());
        // 初始化WEBVIEW url信息
        URLConfig.init(getContext());
        // 初始化通知栏管理器
        initNotification();
        // 初始化rebound动画
        // initRebound();

        //检测主线程耗时
        //UICheckManager.getInstance().start(this);

        // 登录后开启长连接
        if (UserConfig.isPassLogined()) {
            L.i("用户已登录，开启长连接...");
            startLongConn();
        }

        // 加载Andfix管理类
        AndfixManager.getInstance().init(getApplicationContext());
    }


    /**
     * 初始化通知栏管理器
     */
    public void initNotification() {
        // 初始化通知管理类
        notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * 初始化UA
     */
    public String initUA() {
        String ua = "A_" + SysConfig.getAppVerSion(getApplicationContext()) + "&"
                + Build.VERSION.RELEASE + "&" + android.os.Build.MODEL + "&" + ChannelUtil.getChannel(context);
        NetworkTask.DEFEALT_UA = ua;
        L.i("ua:" + ua);
        return ua;
    }

    //-------------------------------使用闹钟定时执行长连接服务-------------------------------
    public void startLongConn() {
        quitLongConn();
        L.i("长连接服务已开启");
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, LongConnService.class);
        intent.setAction(LongConnService.ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long triggerAtTime = SystemClock.elapsedRealtime();
        manager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, 60 * 1000, pendingIntent);
    }

    public void quitLongConn() {
        L.i("长连接服务已关闭");
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, LongConnService.class);
        intent.setAction(LongConnService.ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.cancel(pendingIntent);
        ObserverManager.getObserver("LongConnService").observer("", "stop");

    }


    private void initDevelopMode() {
        SharedPreferences network = getSharedPreferences("network", 0);
        boolean check = network.getBoolean("check", true);
        // 设置测试环境IP，正式环境IP
        if (check) {
            NetworkTask.setBASEURL(NetworkTask.NORMAL_IP);
        } else {
            String ip = network.getString("ip", NetworkTask.TEST_IP);
            NetworkTask.setBASEURL(ip);
        }
        L.i("当前环境：" + NetworkTask.getBASEURL());
    }

    /**
     * 初始化UUID
     *
     * @param ua
     */
    public static String initUUID(String ua, Context context) {
        SharedPreferences userInfo = context.getSharedPreferences(SPConstant.SPNAME_UUID, 0);
        String uuid = userInfo.getString(SPConstant.SPKEY_UUID, "");
        if (uuid == null || uuid.trim().equals("")) {
            uuid = MD5.getMessageDigest((ua + System.currentTimeMillis()).getBytes());
            userInfo.edit().putString(SPConstant.SPKEY_UUID, uuid).apply();
        }
        UserConfig.UUID = uuid;
        L.i("uuid:" + uuid);
        return uuid;
    }

    private void initDisplayOpinion() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        DisplayUtil.density = dm.density;
        DisplayUtil.densityDPI = dm.densityDpi;
        DisplayUtil.screenWidthPx = dm.widthPixels;
        DisplayUtil.screenhightPx = dm.heightPixels;
        DisplayUtil.screenWidthDip = DisplayUtil.px2dip(getApplicationContext(), dm.widthPixels);
        DisplayUtil.screenHightDip = DisplayUtil.px2dip(getApplicationContext(), dm.heightPixels);
        DisplayUtil.statusBarHight = getStatusBarHeight(getContext());
    }

    private int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        try {
            Class clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            Field field = clazz.getField("status_bar_height");
            //反射出该对象中status_bar_height字段所对应的在R文件的id值
            //该id值由系统工具自动生成,文档描述如下:
            //The desired resource identifier, as generated by the aapt tool.
            int id = Integer.parseInt(field.get(object).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(id);
        } catch (Exception e) {
        }
        return statusBarHeight;
    }


    // 添加Activity到容器中
    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    // 删除activity
    public void removeActivity(Activity activity) {
        activityList.remove(activity);
    }

    public List<Activity> getActivitys() {
        return activityList;
    }

    public void stopAndMain() {
        for (Activity activity : activityList) {
            activity.finish();
        }
        activityList.clear();
    }

    // 遍历所有Activity并finish
    public void exit() {
        // 保存友盟数据
        MobclickAgent.onKillProcess(context);
        // 退出的时候清空下载通知栏消息
        UUApp.notificationManager.cancel(DownloadNotification.notofyId);
        if (activityList == null)
            return;
        for (Activity activity : activityList) {
            activity.finish();
        }
        activityList.clear();
        System.exit(0);
    }

    // 除当前Activity外，遍历所有Activity并finish
    public void exitOther(Activity thisActivity) {
        if (activityList == null)
            return;
        for (Activity activity : activityList) {
            if (!thisActivity.equals(activity)) {
                activity.finish();
            }
        }
        activityList.clear();
    }


    // 遍历所有Activity并finish
    public void exitCrash() {
        for (Activity activity : activityList) {
            activity.finish();
        }
        activityList.clear();
    }


    /**
     * 加载图片
     *
     * @param imgurl
     * @param imageView
     * @param defaultPicId
     */
    public void display(String imgurl, NetworkImageView imageView, int defaultPicId) {
        mCommonImageLoader = getImageLoaderInstance();
        imageView.setDefaultImageResId(defaultPicId);
        imageView.setImageUrl(imgurl, mCommonImageLoader);
    }

    public void display(String imgurl, NetworkImageView imageView) {
        if (imgurl != null && imageView != null) {
            mCommonImageLoader = getImageLoaderInstance();
            imageView.setImageUrl(imgurl, mCommonImageLoader);
        }
    }

    public static ImageLoader getImageLoaderInstance() {
        if (mCommonImageLoader == null) {
            synchronized (lockObj) {
                if (mCommonImageLoader == null) {
                    mCommonImageLoader = new ImageLoader(getRequestQueue(), LruImageCache.getInstance());
                }
            }
        }
        return mCommonImageLoader;
    }

    /**
     * 首页地图拉取MarkerIcon的ImageLoader
     *
     * @return
     */
    public synchronized static ImageLoader getMapIconImageLoader() {
        if (mMapIconImageLoader == null) {
            if (mMapIconImageLoader == null) {
                mMapIconImageLoader = new ImageLoader(getRequestQueue(), MapIconLruImageCache.getInstance());
            }
        }
        return mMapIconImageLoader;
    }

    public static RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            synchronized (lockObj) {
                if (requestQueue == null) {
                    requestQueue = Volley.newRequestQueue(UUApp.getInstance(), new VolleyHurlStack());
                }
            }
        }
        return requestQueue;
    }
}
