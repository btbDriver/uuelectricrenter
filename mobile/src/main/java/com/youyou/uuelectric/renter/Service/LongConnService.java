package com.youyou.uuelectric.renter.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.mina.MinaLongConnectManager;
import com.youyou.uuelectric.renter.Utils.observer.ObserverListener;
import com.youyou.uuelectric.renter.Utils.observer.ObserverManager;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 长连接服务，在用户登录态时持续运行在后台接收服务端的推送消息
 */
public class LongConnService extends Service {
    public static String ACTION = "com.youyou.uuelectric.renter.Service.LongConnService";
    private static MinaLongConnectManager minaLongConnectManager;
    public String tag = "LongConnService";
    private Context context;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getApplicationContext();
        // 设置用户票据信息类，当APP推出的时候无法使用UUAppCar.getInstance().getContext();获取上下文
        // 所以在此处使用Service来获取应用上下文
        // UserSecurityConfig.context = getApplicationContext();
        startLongConnect();
        ObserverManager.addObserver("LongConnService", stopListener);
        return START_STICKY;
    }

    public ObserverListener stopListener = new ObserverListener() {
        @Override
        public void observer(String from, Object obj) {
            closeConnect();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeConnect();
    }

    private void startLongConnect() {

        /*Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        System.out.println("---------------连接使用的线程----------------");
        for (Thread t : allStackTraces.keySet()) {
            if (t.getName().startsWith("longConnectThread")) {
                System.out.println(t);
            }
        }
        System.out.println("--------------连接使用的线程-----------------");*/

        if (Config.isNetworkConnected(context)) {
            if (minaLongConnectManager != null && minaLongConnectManager.checkConnectStatus()) {
                L.i("长连接状态正常...");
                return;
            }
            if (minaLongConnectManager == null) {
                startThreadCreateConnect();
            } else {
                if (minaLongConnectManager.connectIsNull() && minaLongConnectManager.isNeedRestart()) {
                    L.i("session已关闭，需要重新创建一个session");
                    minaLongConnectManager.startConnect();
                } else {
                    L.i("长连接已关闭，需要重开一个线程来重新创建长连接");
                    startThreadCreateConnect();
                }
            }
        }

    }

    private final AtomicInteger mCount = new AtomicInteger(1);

    private void startThreadCreateConnect() {
        if (UserConfig.getUserInfo().getB3Key() != null && UserConfig.getUserInfo().getSessionKey() != null) {
            System.gc();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    minaLongConnectManager = MinaLongConnectManager.getInstance(context);
                    minaLongConnectManager.crateLongConnect();
                }
            }, "longConnectThread" + mCount.getAndIncrement()).start();
        }
    }


    private void closeConnect() {

        if (minaLongConnectManager != null) {
            minaLongConnectManager.closeConnect();
        }
        minaLongConnectManager = null;

        // 停止长连接服务LongConnService
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
