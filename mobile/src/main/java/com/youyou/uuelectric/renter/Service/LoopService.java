package com.youyou.uuelectric.renter.Service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.mina.MinaLongConnectManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 长连接异常时启动服务，长连接恢复时关闭服务
 */
public class LoopService extends Service {

    public static final String ACTION = "com.youyou.uuelectric.renter.Service.LoopService";

    /**
     * 客户端执行轮询的时间间隔，该值由StartQueryInterface接口返回，默认设置为30s
     */
    public static int LOOP_INTERVAL_SECS = 30;
    /**
     * 轮询时间间隔(MLOOP_INTERVAL_SECS 这个时间间隔变量有服务器下发，此时轮询服务的场景与逻辑与定义时发生变化，涉及到IOS端，因此采用自己定义的常量在客户端写死时间间隔)
     */
    public static int MLOOP_INTERVAL_SECS = 30;
    /**
     * 当前服务是否正在执行
     */
    public static boolean isServiceRuning = false;
    /**
     * 定时任务工具类
     */
    public static Timer timer = new Timer();

    private static Context context;

    public LoopService() {
        isServiceRuning = false;
    }

    //-------------------------------使用闹钟执行轮询服务------------------------------------

    /**
     * 启动轮询服务
     */
    public static void startLoopService(Context context) {
        if (context == null)
            return;
        quitLoopService(context);
        L.i("开启轮询服务，轮询间隔：" + MLOOP_INTERVAL_SECS + "s");
        AlarmManager manager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context.getApplicationContext(), LoopService.class);
        intent.setAction(LoopService.ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // long triggerAtTime = SystemClock.elapsedRealtime() + 1000;
        /**
         * 闹钟的第一次执行时间，以毫秒为单位，可以自定义时间，不过一般使用当前时间。需要注意的是，本属性与第一个属性（type）密切相关，
         * 如果第一个参数对应的闹钟使用的是相对时间（ELAPSED_REALTIME和ELAPSED_REALTIME_WAKEUP），那么本属性就得使用相对时间（相对于系统启动时间来说），
         *      比如当前时间就表示为：SystemClock.elapsedRealtime()；
         * 如果第一个参数对应的闹钟使用的是绝对时间（RTC、RTC_WAKEUP、POWER_OFF_WAKEUP），那么本属性就得使用绝对时间，
         *      比如当前时间就表示为：System.currentTimeMillis()。
         */
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), MLOOP_INTERVAL_SECS * 1000, pendingIntent);
    }

    /**
     * 停止轮询服务
     */
    public static void quitLoopService(Context context) {
        if (context == null)
            return;
        L.i("关闭轮询闹钟服务...");
        AlarmManager manager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context.getApplicationContext(), LoopService.class);
        intent.setAction(LoopService.ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.cancel(pendingIntent);
        // 关闭轮询服务
        L.i("关闭轮询服务...");
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.i("开始执行轮询服务... \n 判断当前用户是否已登录...");
        // 若当前网络不正常或者是用户未登陆，则不再跳转
        if (UserConfig.isPassLogined()) {
            // 判断当前长连接状态，若长连接正常，则关闭轮询服务
            L.i("当前用户已登录... \n 判断长连接是否已经连接...");
            if (MinaLongConnectManager.session != null && MinaLongConnectManager.session.isConnected()) {
                L.i("长连接已恢复连接，退出轮询服务...");
                quitLoopService(context);
            } else {
                if (isServiceRuning) {
                    return START_STICKY;
                }
                // 启动轮询拉取消息
                startLoop();
            }
        } else {
            L.i("用户已退出登录，关闭轮询服务...");
            quitLoopService(context);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        L.i("轮询服务退出，执行onDestory()方法，inServiceRuning赋值false");
        isServiceRuning = false;
        timer.cancel();
        timer = new Timer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * 启动轮询拉去消息
     */
    private void startLoop() {
        if (timer == null) {
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                isServiceRuning = true;
                L.i("长连接未恢复连接，执行轮询操作... \n 轮询服务中请求getInstance接口...");
                LoopRequest.getInstance(context).sendLoopRequest();
            }
        }, 0, MLOOP_INTERVAL_SECS * 1000);
    }
}
