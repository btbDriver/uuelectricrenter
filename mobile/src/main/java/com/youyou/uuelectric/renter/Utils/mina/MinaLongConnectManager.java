package com.youyou.uuelectric.renter.Utils.mina;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.TextUtils;

import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.Service.LongConnService;
import com.youyou.uuelectric.renter.Service.LoopRequest;
import com.youyou.uuelectric.renter.Service.LoopService;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.SimpleIoProcessorPool;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

import de.greenrobot.event.EventBus;

/**
 * User: qing
 * Date: 2015-07-08 11:54
 * Desc:使用MINA实现的长连接管理类,主要实现长连接的创建、连接、重连和关闭等操作
 */
public class MinaLongConnectManager {

    private static final String TAG = MinaLongConnectManager.class.getSimpleName();
    /**
     * 服务器端口号
     */
    public static final int DEFAULT_PORT = 18156;
    /**
     * 连接超时时间，30 seconds
     */
    public static final long SOCKET_CONNECT_TIMEOUT = 30 * 1000L;

    /**
     * 长连接心跳包发送频率，60s
     */
    public static final int KEEP_ALIVE_TIME_INTERVAL = 60;
    private static Context context;
    private static MinaLongConnectManager minaLongConnectManager;

    private static NioSocketConnector connector;
    private static ConnectFuture connectFuture;
    public static IoSession session;
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * 长连接是否正在连接中...
     */
    private static boolean isConnecting = false;

    private MinaLongConnectManager() {
        EventBus.getDefault().register(this);
    }

    public static synchronized MinaLongConnectManager getInstance(Context ctx) {

        if (minaLongConnectManager == null) {
            context = ctx;
            minaLongConnectManager = new MinaLongConnectManager();
        }
        return minaLongConnectManager;
    }

    /**
     * 检查长连接的各种对象状态是否正常，正常情况下无需再创建
     *
     * @return
     */
    public boolean checkConnectStatus() {
        if (connector != null && connector.isActive() && connectFuture != null && connectFuture.isConnected() && session != null && session.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean connectIsNull() {
        return connector != null;
    }

    /**
     * 创建长连接，配置过滤器链和心跳工厂
     */
    public synchronized void crateLongConnect() {
        // 如果是长连接正在创建中
        if (isConnecting) {
            L.i("长连接正在创建中...");
            return;
        }
        if (!Config.isNetworkConnected(context)) {
            L.i("检测到网络未打开，无法正常启动长连接，直接return...");
            return;
        }
        // 检查长连接的各种对象状态是否正常，正常情况下无需再创建
        if (checkConnectStatus()) {
            return;
        }
        isConnecting = true;
        try {
            connector = new NioSocketConnector();
            connector.setConnectTimeoutMillis(SOCKET_CONNECT_TIMEOUT);

            if (L.isDebug) {
                if (!connector.getFilterChain().contains("logger")) {
                    // 设置日志输出工厂
                    connector.getFilterChain().addLast("logger", new LoggingFilter());
                }
            }
            if (!connector.getFilterChain().contains("codec")) {
                // 设置请求和响应对象的编解码操作
                connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new LongConnectProtocolFactory()));
            }
            // 创建心跳工厂
            ClientKeepAliveMessageFactory heartBeatFactory = new ClientKeepAliveMessageFactory();
            // 当读操作空闲时发送心跳
            KeepAliveFilter heartBeat = new KeepAliveFilter(heartBeatFactory, IdleStatus.READER_IDLE);
            // 设置是否将事件继续往下传递
            heartBeat.setForwardEvent(true);
            // 设置心跳包请求后超时无反馈情况下的处理机制，默认为关闭连接,在此处设置为输出日志提醒
            heartBeat.setRequestTimeoutHandler(KeepAliveRequestTimeoutHandler.LOG);
            //设置心跳频率
            heartBeat.setRequestInterval(KEEP_ALIVE_TIME_INTERVAL);
            if (!connector.getFilterChain().contains("keepAlive")) {
                connector.getFilterChain().addLast("keepAlive", heartBeat);
            }
            if (!connector.getFilterChain().contains("reconnect")) {
                // 设置长连接重连过滤器，当检测到Session(会话)断开后，重连长连接
                connector.getFilterChain().addLast("reconnect", new LongConnectReconnectionFilter());
            }
            // 设置接收和发送缓冲区大小
            connector.getSessionConfig().setReceiveBufferSize(1024);
            connector.getSessionConfig().setSendBufferSize(1024);
            // 设置读取空闲时间：单位为s
            connector.getSessionConfig().setReaderIdleTime(60);

            // 设置长连接业务逻辑处理类Handler
            LongConnectHandler longConnectHandler = new LongConnectHandler(this, context);
            connector.setHandler(longConnectHandler);

        } catch (Exception e) {
            e.printStackTrace();
            closeConnect();
        }

        startConnect();
    }

    /**
     * 开始或重连长连接
     */
    public synchronized void startConnect() {
        if (connector != null) {
            L.i("开始创建长连接...");
            boolean isSuccess = beginConnect();
            // 创建成功后，修改创建中状态
            if (isSuccess) {
                isNeedRestart = false;
                if (context != null) {
                    // 长连接启动成功后，主动拉取一次消息
                    LoopRequest.getInstance(context).sendLoopRequest();
                }
            } else {
                // 启动轮询服务
                startLoopService();
            }
            isConnecting = false;
//            printProcessorExecutor();
        } else {
            L.i("connector已为null，不能执行创建连接动作...");
        }
    }

    /**
     * 检测MINA中线程池的活动状态
     */
    private void printProcessorExecutor() {
        Class connectorClass = connector.getClass().getSuperclass();
        try {
            L.i("connectorClass:" + connectorClass.getCanonicalName());
            Field field = connectorClass.getDeclaredField("processor");
            field.setAccessible(true);
            Object connectorObject = field.get(connector);
            if (connectorObject != null) {
                SimpleIoProcessorPool processorPool = (SimpleIoProcessorPool) connectorObject;
                Class processPoolClass = processorPool.getClass();
                Field executorField = processPoolClass.getDeclaredField("executor");
                executorField.setAccessible(true);
                Object executorObject = executorField.get(processorPool);
                if (executorObject != null) {
                    ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorObject;
                    L.i("线程池中当前线程数：" + threadPoolExecutor.getPoolSize() + "\t 核心线程数:" + threadPoolExecutor.getCorePoolSize() + "\t 最大线程数:" + threadPoolExecutor.getMaximumPoolSize());
                }

            } else {
                L.i("connectorObject = null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 开始创建Session
     *
     * @return
     */
    public boolean beginConnect() {

        if (session != null) {
            session.close(false);
            session = null;
        }
        if (connectFuture != null && connectFuture.isConnected()) {
            connectFuture.cancel();
            connectFuture = null;
        }
        FutureTask<Boolean> futureTask = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    InetSocketAddress address = new InetSocketAddress(NetworkTask.getBASEURL(), DEFAULT_PORT);
                    connectFuture = connector.connect(address);
                    connectFuture.awaitUninterruptibly(3000L);
                    session = connectFuture.getSession();
                    if (session == null) {
                        L.i(TAG + "连接创建失败...当前环境:" + NetworkTask.getBASEURL());
                        return false;
                    } else {
                        L.i(TAG + "长连接已启动，连接已成功...当前环境:" + NetworkTask.getBASEURL());
                        return true;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
        });

        executorService.submit(futureTask);
        try {
            return futureTask.get();
        } catch (Exception e) {
            return false;
        }

    }

    /**
     * 关闭连接，根据传入的参数设置session是否需要重新连接
     */
    public synchronized void closeConnect() {
        if (session != null) {
            session.close(false);
            session = null;
        }
        if (connectFuture != null && connectFuture.isConnected()) {
            connectFuture.cancel();
            connectFuture = null;
        }
        if (connector != null && !connector.isDisposed()) {
            // 清空里面注册的所以过滤器
            connector.getFilterChain().clear();
            connector.dispose();
            connector = null;
        }
        isConnecting = false;
        L.i("长连接已关闭...");
    }

    private volatile boolean isNeedRestart = false;

    public boolean isNeedRestart() {
        return isNeedRestart;
    }

    public void onEventMainThread(BaseEvent event) {
        if (event == null || TextUtils.isEmpty(event.getType()))
            return;
        if (EventBusConstant.EVENT_TYPE_NETWORK_STATUS.equals(event.getType())) {
            String status = (String) event.getExtraData();
            // 当网络状态变化的时候请求startQuery接口
            if (status != null && status.equals("open")) {
                if (isNeedRestart && UserConfig.getUserInfo().getB3Key() != null && UserConfig.getUserInfo().getSessionKey() != null) {
                    L.i("检测到网络已打开且长连接处于关闭状态，需要启动长连接...");
                    Intent intent = new Intent(context, LongConnService.class);
                    intent.setAction(LongConnService.ACTION);
                    context.startService(intent);
                }
            }
        }
    }

    /**
     * 出现异常、session关闭后，接收事件进行长连接重连操作
     */
    public void onEventMainThread(LongConnectMessageEvent event) {

        if (event.getType() == LongConnectMessageEvent.TYPE_RESTART) {

            long currentTime = System.currentTimeMillis();

            // 票据有效的情况下进行重连长连接操作
            if (UserConfig.getUserInfo().getB3Key() != null && UserConfig.getUserInfo().getSessionKey() != null
                    && ((currentTime / 1000) < UserConfig.getUserInfo().getUnvalidSecs())) {
                // 等待2s后重新创建长连接
                SystemClock.sleep(1000);
                if (Config.isNetworkConnected(context)) {
                    L.i("出现异常情况，需要自动重连长连接...");
                    startConnect();
                } else {
                    isNeedRestart = true;
                    L.i("长连接出现异常，需要重新创建session会话...");
                }
            }
        } else if (event.getType() == LongConnectMessageEvent.TYPE_CLOSE) {
            L.i("收到session多次close的消息，此时需要关闭长连接，等待下次闹钟服务来启动...");
            closeConnect();
        }
    }


    /**
     * 启动轮询服务
     */
    public void startLoopService() {
        // 启动轮询服务
        // 暂时不考虑加入网络情况的判断...
        if (!LoopService.isServiceRuning) {
            // 用户是登录态，启动轮询服务
            if (UserConfig.isPassLogined()) {
                // 判断当前长连接的状态，若长连接已连接，则不再开启轮询服务
                if (MinaLongConnectManager.session != null && MinaLongConnectManager.session.isConnected()) {
                    LoopService.quitLoopService(context);
                    return;
                }
                LoopService.startLoopService(context);
            } else {
                LoopService.quitLoopService(context);
            }
        }
    }

}
