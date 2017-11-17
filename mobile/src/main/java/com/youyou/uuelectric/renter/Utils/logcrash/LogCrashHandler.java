package com.youyou.uuelectric.renter.Utils.logcrash;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.Support.L;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,由该类来接管程序,并记录发送错误报告.
 */
public class LogCrashHandler implements UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private UncaughtExceptionHandler mDefaultHandler;// 系统默认的UncaughtException处理类
    private static LogCrashHandler INSTANCE = new LogCrashHandler();// CrashHandler实例
    private Context mContext;// 程序的Context对象

    /**
     * 保证只有一个CrashHandler实例
     */
    private LogCrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static LogCrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();// 获取系统默认的UncaughtException处理器
        Thread.setDefaultUncaughtExceptionHandler(this);// 设置该CrashHandler为程序的默认处理器
    }

    /**
     * 当UncaughtException发生时会转入该重写的方法来处理
     */
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果自定义的没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            // 退出程序
            try {
                Thread.sleep(1000);// 如果处理了，让程序继续运行3秒再退出，保证文件保存并上传到服务器
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            UUApp.getInstance().exitCrash();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex 异常信息
     * @return true 如果处理了该异常信息;否则返回false.
     */
    public boolean handleException(Throwable ex) {
        if (ex == null)
            return true;
        L.e("error:" + ex.getMessage());
        new Thread() {
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出", Toast.LENGTH_SHORT).show();

                Looper.loop();
            }
        }.start();
        return true;
    }

}
