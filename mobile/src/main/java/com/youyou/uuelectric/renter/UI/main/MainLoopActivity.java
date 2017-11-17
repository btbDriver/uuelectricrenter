package com.youyou.uuelectric.renter.UI.main;

import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

import com.android.volley.VolleyError;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.user.protobuf.bean.UserInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.start.StartQueryRequest;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.update.UpdateManager;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

/**
 * Created by liuchao on 2015/9/15.
 * 主界面-主要包含onResume方法中的轮询用户信息代码、访问StartQuery方法获取配置信息
 * 用户状态轮询-onResume
 * startQuery-onCreate 只执行一次
 */
public abstract class MainLoopActivity extends MaterialNavigationDrawer {

    /**
     * 是否能够开始Loop拉取数据
     */
    public static boolean isCanLoop = true;

    @Override
    public void init(Bundle savedInstanceState) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 请求startQuery（请求一次）
        StartQueryRequest.startQueryRequest(this);

        // 若用户没有登录，则检测是否需要更新
        if (!UserConfig.isPassLogined()) {
            UpdateManager.queryAppBaseVersionInfo(this, true, false);
        }

        // 延时10s更新票据
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (UserConfig.isNeedUpdateTicket()) {
                    UserConfig.requestUpdateTicket();
                }
            }
        }, 10 * 1000);
    }



    @Override
    protected void onResume() {
        super.onResume();

        if (isCanLoop) {
            // 打开请求用户状态轮询
            startLoop();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 关闭用户状态轮询
        stopLoop();
    }


    //--------------------------轮询拉取用户状态数据-----------------------------
    /**
     * 异常情况下的轮询时间间隔:5秒 默认固定值
     */
    public static int ERROR_INTERVAL = 5 * 1000;
    /**
     * 当前轮询执行的时间间隔
     */
    public static int interval = ERROR_INTERVAL;
    /**
     * 轮询Handler的消息类型
     */
    public static int LOOP_WHAT = 101;
    /**
     * 请求用户状态是否成功
     */
    public boolean isUserStatusLoadResult = false;
    /**
     * 是否正在请求用户状态信息
     */
    public boolean isUserStatusLoading = false;

    /**
     * 开始执行轮询，正常情况下，每隔1分钟轮询拉取一次最新数据
     * 在onStart时开启轮询
     */
    public void startLoop() {
        isUserStatusLoading = false;
        isUserStatusLoadResult = false;
        loopRequestHandler.sendEmptyMessage(LOOP_WHAT);
    }

    /**
     * 关闭轮询，在界面onStop时，停止轮询操作
     */
    public void stopLoop() {
        isUserStatusLoading = false;
        isUserStatusLoadResult = false;
        loopRequestHandler.removeMessages(LOOP_WHAT);
    }

    /**
     * 处理轮询的Handler
     */
    public Handler loopRequestHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            loadData();
        }
    };

    /**
     * 请求网络，获取用户状态信息
     */
    public void loadData() {
        // 判断当前是否在更新票据
        if (UserConfig.isUpdateTicketing) {
            L.d("userStatus 轮询中----：当前正在更新票据，" + interval + "毫秒之后重新请求数据");
            loopRequestHandler.sendEmptyMessageDelayed(LOOP_WHAT, interval);
            return;
        }
        // 判断当前网络，没网情况下n秒钟之后请求网络
        if (!Config.isNetworkConnected(this)) {
            L.d("userStatus 轮询中----：当前没有网络，" + interval + "毫秒之后重新请求数据");
            loopRequestHandler.sendEmptyMessageDelayed(LOOP_WHAT, interval);
            return;
        }
        // 判断当前登陆态，未登录情况下n秒钟之后请求网络
        if (!UserConfig.isPassLogined()) {
            loopRequestHandler.sendEmptyMessageDelayed(LOOP_WHAT, interval);
            return;
        }
        // 判断当前是否在请求数据,正在请求情况下下n秒钟之后请求网络
        if (isUserStatusLoading) {
            L.d("userStatus 轮询中----：当前正在获取用户状态数据，" + interval + "毫秒之后重新请求数据");
            loopRequestHandler.sendEmptyMessageDelayed(LOOP_WHAT, interval);
            return;
        }
        // 请求到数据，则请求成功不在请求
        if (isUserStatusLoadResult) {
            L.d("userStatus 轮询中----：请求数据成功，不在请求用户状态数据");
            return;
        }
        // 判断当前没有请求到数据
        if (!isUserStatusLoadResult) {
            isUserStatusLoading = true;
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
                                L.i("userStatus 轮询中-----获取用户状态数据成功！");
                                isUserStatusLoadResult = true;
                                // 解析请求到的用户状态信息
                                parserUserInfoResult(response);
                            } else {
                                // -1 退出登录失败
                                loopRequestHandler.sendEmptyMessageDelayed(LOOP_WHAT, interval);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            loopRequestHandler.sendEmptyMessageDelayed(LOOP_WHAT, interval);
                        }
                    }
                }

                @Override
                public void onError(VolleyError errorResponse) {
                    loopRequestHandler.sendEmptyMessageDelayed(LOOP_WHAT, interval);
                }

                @Override
                public void networkFinish() {
                    isUserStatusLoading = false;
                }
            });
        }
    }

    /**
     * 解析请求到的用户状态信息结果
     */
    public abstract void parserUserInfoResult(UserInterface.UserInfo.Response response);
}
