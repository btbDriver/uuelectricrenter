package com.youyou.uuelectric.renter.Utils.Support;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.android.volley.VolleyError;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.ext.protobuf.bean.ExtInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.DialogUtil;
import com.youyou.uuelectric.renter.Utils.LocationUtils;
import com.youyou.uuelectric.renter.pay.utils.Constants;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 公用类：包含应用中一些公用变量或公用方法
 */
public class Config {
    /**
     * 判断当前页面是否为通知栏打开
     */
    public static boolean isNotificationOpen = false;
    /**
     * 开关车门等异步操作是否超时
     */
    public static boolean timeout = false;
    /**
     * 开关车门等异步操作的超时时间
     */
    public static long timeouttime = 30 * 1000;
    /**
     * 微信Key
     */
    public static final String WX_APP_ID = "wxcea585ca102b3587";
    /**
     * 是否需要重新请求地图页网点数据
     */
    public static boolean isAgainRequestDotList = false;
    /**
     * 是否显示了重新登录弹窗
     */
    public static boolean isShowReLoginDialog = false;
    /**
     * 字母索引
     */
    public static String[] word = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    public static String kefuphone = "4007-5555-88";

    /**
     * 1 存在其他普通费用  0:不存在
     */
    public static int otherFee = 0;
    /**
     * 1 存在违章费用  0:不存在
     */
    public static int trafficFee = 0;

    /**
     * 获取SP中保存的订单ID
     *
     * @param context
     * @return
     */
    public static String getOrderId(Context context) {
        if (context == null) {
            L.i("从sp中获取orderId失败,context对象为空...");
            return "";
        }
        SharedPreferences sp = context.getSharedPreferences("order", Context.MODE_PRIVATE);
        return sp.getString("orderid", "");
    }

    /**
     * 向Sp中保存orderId信息
     * @param context
     * @param orderId
     */
    public static void setOrderId(Context context, String orderId) {
        if (context == null) {
            L.i("向sp中设置orderId失败,context对象为空...");
            return;
        }
        // 若当前orderId为null,则设置orderId为""
        if (orderId == null) {
            orderId = "";
        }

        SharedPreferences sp = context.getSharedPreferences("order", Context.MODE_PRIVATE);
        boolean isCommitSuccess = sp.edit().putString("orderid", orderId).commit();
        if (!isCommitSuccess) {
            sp.edit().putString("orderid", orderId).commit();
        }
    }

    /**
     * 清空sp中的orderId
     * @param context
     */
    public static void clearOrderId(Context context) {
        if (context == null) {
            L.i("向sp中设置orderId失败,context对象为空...");
            return;
        }

        SharedPreferences sp = context.getSharedPreferences("order", Context.MODE_PRIVATE);
        sp.edit().putString("orderid", "").commit();
    }

    /**
     * 设置按钮是否可用
     *
     * @param enabled
     */
    public static void setB3ViewEnable(View view, boolean enabled) {
        if (view == null)
            return;
        if (enabled) {
            view.setOnTouchListener(null);
            view.setBackgroundResource(R.drawable.b3_btn_bg_shape);
            view.invalidate();
        } else {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return true;
                }
            });
            view.setBackgroundResource(R.drawable.b3_btn_bg_shape_disable);
            view.invalidate();
        }
    }

    /**
     * 显示错误提示Toast
     *
     * @param context
     */
    public static void showFiledToast(Activity context) {
        if (context != null) {
            showToast(context, SysConfig.NETWORK_FAIL);
        }
    }

    /**
     * 公用的Toast提示方法
     *
     * @param context
     * @param msg
     */
    public static void showToast(Context context, String msg) {
        if (context != null && msg != null && !msg.trim().equals("")) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 判断某个应用是否安装
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isAvilible(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        // 获取所有已安装程序的包信息
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (pinfo.get(i).packageName.equalsIgnoreCase(packageName)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 判断微信是否安装
     * @param mContext
     * @return
     */
    public static boolean isWXAppInstalled(Context mContext) {
        IWXAPI msgApi = WXAPIFactory.createWXAPI(mContext, null);
        msgApi.registerApp(Config.WX_APP_ID);


        return msgApi.isWXAppInstalled();
    }

    /**
     * 当前显示在最上面的那个Activity
     */
    public static Activity currentContext;

    /**
     * 将当前Activity设置为竖屏模式，并添加到UUApp的Activity管理栈中
     *
     * @param activity
     */
    public static void setActivityState(Activity activity) {
        currentContext = activity;
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        UUApp.getInstance().addActivity(activity);
    }

    /**
     * 用来判断服务是否运行.
     *
     * @param className 判断的服务名字
     * @return true 在运行 false 不在运行
     */
    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    public static boolean isOpenRing = true;
    public static boolean isOpenvib = true;
    public static long lastringTime = 0;

    /**
     * 播放声音
     *
     * @return
     * @throws Exception
     * @throws java.io.IOException
     */
    public static void ring(Context context) {
        SharedPreferences sp = context.getSharedPreferences("zaitaxiang_user", Context.MODE_PRIVATE);
        isOpenRing = sp.getBoolean("ring", true);
        if (!isOpenRing) {
            return;
        }
        if (System.currentTimeMillis() - lastringTime < 3000) {
            return;
        }
        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(context, alert);
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) != 0) {
                player.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                player.setLooping(false);
                player.prepare();
                player.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        lastringTime = System.currentTimeMillis();
    }

    public static long lastVibtime = 0;

    /**
     * boolean isRepeat ： 是否反复震动，如果是true，反复震动，如果是false，只震动一次 long milliseconds ：震动的时长，单位是毫秒 long[] pattern ：自定义震动模式 。 数组中数字的含义依次是[静止时长，震动时长，静止时长，震动时长。。。]时长的单位是毫秒
     */
    public static void Vibrate(Context Activity/* ,long[] pattern,boolean isRepeat */) {
        SharedPreferences sp = Activity.getSharedPreferences("zaitaxiang_login", Context.MODE_PRIVATE);
        isOpenvib = sp.getBoolean("vib", true);
        if (!isOpenvib) {
            return;
        }
        if (System.currentTimeMillis() - lastVibtime < 3000) {
            return;
        }
        Vibrator vib = (Vibrator) Activity.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(500);
        lastVibtime = System.currentTimeMillis();
    }

    /**
     * 当前定位到的经纬度
     */
    public static double lat, lng;
    public static long locationTime = 0;
    private static AMapLocationClient mAMapLocationClient = null;
    private static GDLocationListener gdLocationListener = null;
    public static String currentCity = "";
    public static String currentAddress = "";

    /**
     * 进入GPS设置界面请求码
     */
    public static final int SETTINGGPS = 1;

    /**
     * 判断是否打开了允许虚拟位置,如果打开了 则弹窗让他去关闭
     */
    public static boolean isAllowMockLocation(final Activity context) {
        boolean isOpen = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0;
        if (isOpen) {
            Config.showTiplDialog(context, null, "定位失败，需要关闭【允许模拟位置】功能后才能使用友友用车查看附近的车辆。", "去设置", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                }
            });
        }
        return isOpen;
    }

    /**
     * 核查定位信息，是否开启GPS，是否使用了模拟定位
     */
    public static boolean checkLocationInfo(final Activity context, String message) {
        boolean isOpen = false;
        if (!isAllowMockLocation(context)) {
            // 判断GPS是否开启
            if (!LocationUtils.isGPSOpen(context)) {
                isOpen = false;
                Dialog dialog = DialogUtil.getInstance(context).showMaterialTipDialog(null, message, "去设置", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            //转到手机设置界面，用户设置GPS
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            context.startActivityForResult(intent, SETTINGGPS);
                        } catch (Exception e) {
                            showToast(context, "您的手机需要手动设置!");
                        }
                    }
                });
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
            } else {
                isOpen = true;
                if (DialogUtil.getDialog() != null) {
                    // 释放弹窗对象
                    DialogUtil.closeDialog();
                }
            }
        }
        return isOpen;
    }


    /**
     * 定位 获取经纬度，取消原来默认的10分钟缓存
     */
    public static void getCoordinates(final Context activity, final LocationListener listener) {
        if (gdLocationListener != null)
            disableMyLocation(gdLocationListener);
        mAMapLocationClient = new AMapLocationClient(activity.getApplicationContext());
        gdLocationListener = new GDLocationListener(listener);
        mAMapLocationClient.setLocationListener(gdLocationListener);

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

    /**
     * 判断定位是否成功，cityCode和lat、lng都不为null时才为成功
     *
     * @return true 定位成功  false 定位失败
     */
    public static boolean locationIsSuccess() {
        if (!TextUtils.isEmpty(cityCode) && lat != 0 && lng != 0) {
            return true;
        }
        return false;
    }

    /**
     * 关闭定位
     *
     * @param listener
     */
    public static void disableMyLocation(AMapLocationListener listener) {
        if (mAMapLocationClient != null) {
            mAMapLocationClient.stopLocation();
            gdLocationListener = null;
            mAMapLocationClient.onDestroy();
        }
        mAMapLocationClient = null;
    }

    /**
     * 定位后,当前城市编号(用于高德)
     */
    public static String cityCode = "010";
    public static Dialog loadingDialog = null;

    /**
     * 显示加载中进度弹窗，使用默认的加载中文本
     *
     * @param activity
     * @param canCancel
     * @param listener
     */
    public static void showProgressDialog(Activity activity, boolean canCancel, final ProgressCancelListener listener) {
        dismissProgress();
        View view = LayoutInflater.from(activity).inflate(R.layout.loading_layout, null);
//        ProgressWheel progressView = (ProgressWheel) view.findViewById(R.id.progress_wheel);
        loadingDialog = new Dialog(activity, R.style.loading_dialog);// 创建自定义样式
        loadingDialog.setContentView(view, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
        loadingDialog.setCancelable(canCancel);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (listener != null) {
                    listener.progressCancel();
                }
            }
        });
        if (!activity.isFinishing()) {
            loadingDialog.show();
        }
    }

    /**
     * 显示加载中进度弹窗，可传入msg作为显示文本
     *
     * @param activity
     * @param canCancel
     * @param listener
     * @param msg
     */
    public static void showProgressDialog(Activity activity, boolean canCancel, final ProgressCancelListener listener, String msg) {

        try {
            dismissProgress();
            View view = LayoutInflater.from(activity).inflate(R.layout.loading_layout, null);
//            ProgressView progressView = (ProgressView) view.findViewById(R.id.progress_pv_circular);
            android.widget.TextView progressMsg = (android.widget.TextView) view.findViewById(R.id.progress_msg);
            if (!TextUtils.isEmpty(msg)) {
                progressMsg.setText(msg);
            }
//            progressView.start();
            loadingDialog = new Dialog(activity, R.style.loading_dialog);// 创建自定义样式
            loadingDialog.setContentView(view, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
            loadingDialog.setCancelable(canCancel);
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (listener != null) {
                        listener.progressCancel();
                    }
                }
            });
            if (!activity.isFinishing()) {
                loadingDialog.show();
            }
        } catch (Exception e) {

        }

    }

    /**
     * 进度弹窗取消时的回调接口
     */
    public static interface ProgressCancelListener {
        public void progressCancel();
    }

    /**
     * 加载进度框是否正在显示
     *
     * @return
     */
    public static boolean loadingDialogIsShowing() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            return true;
        }
        return false;
    }

    /**
     * 隐藏进度弹窗
     */
    public static void dismissProgress() {
        try {
            if (loadingDialog != null) {
                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
                loadingDialog = null;
            }
        } catch (Exception e) {
        }
    }

    /**
     * 上一次按下返回键的时间
     */
    private static long exitTime = 0;

    /**
     * 应用程序主界面点击返回后提示用户是否退出应用
     *
     * @param keyCode
     * @param event
     * @param context
     * @return
     */
    public static boolean onKeyDown(int keyCode, KeyEvent event, Activity context) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                if ((System.currentTimeMillis() - exitTime) > 2000) {
                    showToast(context, "再按一次退出");
                    exitTime = System.currentTimeMillis();
                } else {
                    UUApp.getInstance().exit();
                }
                return true;
            }
            return true;
        }
        return false;
    }

    /**
     * 判断参数中activity是否运行
     * 参数是activity的包名
     */
    public static boolean isRun(Context context, String activity) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        if (rti.get(0).topActivity.getClassName().equals(activity)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断当前界面是否是桌面
     */
    public static boolean outApp(Context context) {
        if (context == null) {
            return true;
        }
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        String topPackageName;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            topPackageName = getRunningTaskByAfterL(mActivityManager);
        } else {
            topPackageName = getRunningTaskByPreL(mActivityManager);
        }
        if (topPackageName.indexOf("com.youyou.uuelectric.renter") == -1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * SDK 21版本之前可以使用getRunningTasks()来获取系统中的任务栈，但是在21版本后该方法被废弃，无法正确的获取栈顶任务
     *
     * @param mActivityManager
     * @return
     */
    public static String getRunningTaskByPreL(ActivityManager mActivityManager) {
        List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        return rti.get(0).topActivity.getPackageName();
    }

    /**
     * SDK 21(L) 之后获取当前应用是否在前台运行
     *
     * @param mActivityManager
     * @return
     */
    public static String getRunningTaskByAfterL(ActivityManager mActivityManager) {
        final int PROCESS_STATE_TOP = 2;
        ActivityManager.RunningAppProcessInfo currentInfo = null;
        Field field = null;
        try {
            field = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<ActivityManager.RunningAppProcessInfo> appList = mActivityManager.getRunningAppProcesses();
        if (appList != null && appList.size() > 0) {
            for (ActivityManager.RunningAppProcessInfo app : appList) {
                if (app != null && app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                        app.importanceReasonCode == 0) {
                    Integer state = null;
                    try {
                        state = field.getInt(app);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (state != null && state == PROCESS_STATE_TOP) {
                        currentInfo = app;
                        break;
                    }
                }
            }
        }

        if (currentInfo != null) {
            return currentInfo.pkgList[0];
        }
        return "";
    }

    /**
     * MD5加密，32位
     *
     * @param str
     * @return
     */
    public static String MD5(String str) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        char[] charArray = str.toCharArray();
        byte[] byteArray = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString().toUpperCase();
    }

    /**
     * 格式化日期：星期几 MM月dd日 HH:mm
     *
     * @param timeSecond 秒
     * @return
     */
    public static String getFormatTime(String timeSecond) {
        Long time = Long.parseLong(timeSecond);
        Date date = new Date(time * 1000L);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int week = calendar.get(Calendar.DAY_OF_WEEK);


        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");
        String result = sdf.format(date);
        return getWeek(week) + " " + result;
    }


    /**
     * 将日期转换为周几
     *
     * @param week
     * @return
     */
    private static String getWeek(int week) {
        if (week == 1) {
            return "周日";
        } else if (week == 2) {
            return "周一";
        } else if (week == 3) {
            return "周二";
        } else if (week == 4) {
            return "周三";
        } else if (week == 5) {
            return "周四";
        } else if (week == 6) {
            return "周五";
        } else if (week == 7) {
            return "周六";
        }
        return "";
    }

    /**
     * 网络连接是否正常
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 拨打电话权限请求码
     */
    public static final int REQUEST_CALL_PHONE = 100;

    /**
     * 拨打电话，联系客服中心
     *
     * @param context
     */
    public static void callCenter(final Activity context) {
        callPhoneByNumber(context, kefuphone);
    }

    /**
     * 拨打指定的电话号码
     *
     * @param context
     * @param number
     */
    public static void callPhoneByNumber(final Activity context, final String number) {
        showMaterialDialog(context, null, "是否需要联系客服？", "返回", "联系客服", null, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (number.trim().length() != 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (context.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            context.requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                                    REQUEST_CALL_PHONE);
                            return;
                        }
                    }
                    Intent phoneIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
                    context.startActivity(phoneIntent);
                }
            }
        });
    }


    /**
     * 显示俩按钮的弹窗
     */
    public static Dialog showMaterialDialog(final Activity mContext, String title, String message, String leftButton, String rightButton, View.OnClickListener lc, View.OnClickListener rc) {
        if (mContext == null)
            return null;
        Dialog dialog = DialogUtil.getInstance(mContext).showMaterialDialog(title, message, leftButton, rightButton, lc, rc);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    /**
     * 显示1个按钮的弹窗
     */
    public static void showTiplDialog(final Activity mContext, String title, String message, String buttonName, View.OnClickListener bt) {
        if (mContext == null)
            return;
        Dialog dialog = DialogUtil.getInstance(mContext).showMaterialTipDialog(title, message, buttonName, bt);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * 上一次点击时间
     */
    private static long lastClickTime = 0;

    /**
     * 是否是双击动作
     *
     * @return
     */
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (timeD >= 0 && timeD <= 300) {
            return true;
        } else {
            lastClickTime = time;
            return false;
        }
    }


    /**
     * 没有网络
     */
    public static final int NETWORKTYPE_INVALID = 0;
    /**
     * wap网络
     */
    public static final int NETWORKTYPE_WAP = 2;
    /**
     * wifi网络
     */
    public static final int NETWORKTYPE_WIFI = 1;
    /**
     * 2G网络
     */
    public static final int NETWORKTYPE_2G = 2;
    /**
     * 3G和3G以上网络，或统称为快速网络
     */
    public static final int NETWORKTYPE_3G = 3;
    /**
     * 4G网络
     */
    public static final int NETWORKTYPE_4G = 4;

    /**
     * 测试当前网络类型wifi？2G？3G？4G？
     *
     * @return
     */
    public static int getNetworkType(Context context) {
        int strNetworkType = NETWORKTYPE_INVALID;
        if (context == null)
            return strNetworkType;

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                strNetworkType = NETWORKTYPE_WIFI;
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                String _strSubTypeName = networkInfo.getSubtypeName();
                // TD-SCDMA   networkType is 17
                int networkType = networkInfo.getSubtype();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                        strNetworkType = NETWORKTYPE_2G;
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                        strNetworkType = NETWORKTYPE_3G;
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                        strNetworkType = NETWORKTYPE_4G;
                        break;
                    default:
                        // http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
                        if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                            strNetworkType = NETWORKTYPE_3G;
                        } else {
                            strNetworkType = NETWORKTYPE_2G;
                        }
                        break;
                }
            }
        }
        return strNetworkType;
    }

    /**
     * 客户端基本日志上报
     *
     * @param type 业务类型 （数据中心维护）
     * @param log  日志数据，使用\t分隔
     */
    public static void reportLog(int type, String log) {
        ExtInterface.CommonReportLog.Request.Builder builder = ExtInterface.CommonReportLog.Request.newBuilder();
        builder.setType(type);
        builder.setLogData(log);
        NetworkTask task = new NetworkTask(Cmd.CmdCode.CommonReportLog_VALUE);
        task.setBusiData(builder.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {

            @Override
            public void onSuccessResponse(UUResponseData responseData) {

            }

            @Override
            public void onError(VolleyError errorResponse) {

            }

            @Override
            public void networkFinish() {

            }
        });
    }
}
