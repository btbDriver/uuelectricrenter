package com.youyou.uuelectric.renter.Network.user;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;

import com.android.volley.VolleyError;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.passport.pb.bean.LoginCommon;
import com.uu.facade.passport.pb.iface.LoginInterface;
import com.uu.facade.user.protobuf.bean.UserInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.UI.login.LoginPhoneActivity;
import com.youyou.uuelectric.renter.UI.main.MainActivity;
import com.youyou.uuelectric.renter.UI.web.H5Cookie;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.DialogUtil;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;

import java.nio.charset.Charset;

import de.greenrobot.event.EventBus;

/**
 * Created by liuchao on 2015/9/9.
 */
public class UserConfig {
    /**
     * 用户保存用户票据信息
     * #####注：获取userInfo对象时，使用getUserInfo()方法
     */
    public static UserInfo userInfo = new UserInfo();

    // 默认b3Key
    public static final String b3KeyHeader = "uuur-20150905-fixed-b3-key";
    public static final byte[] b3Key = b3KeyHeader.getBytes(Charset.forName("utf-8"));
    // 控制变量，判断当前是否在更新票据
    public static boolean isUpdateTicketing = false;
    // 用户UUID
    public static String UUID = null;


    /**
     * APP启动时初始化
     */
    public static void init(Context context) {
        userInfo = GsonUtils.getInstance().fromJson(UserSPUtils.getParam(context, SPConstant.SPNAME_USER_INFO, SPConstant.SPKEY_USER_INFO), UserInfo.class);
        //票据有效
        //判断是否需要去更新， 则去更新票据
        // L.i("UUApp.UserConfig.init:是否更新票据" + isNeedUpdateTicket());
        // if (isNeedUpdateTicket()) {
        //    requestUpdateTicket();
        // }
        // App启动时清楚Cookie信息
        H5Cookie.removeCookies(context);
        // App启动时清除订单信息
        Config.clearOrderId(context);
    }

    /**
     * 更新票据信息
     *
     * @param ticket
     * @param userInfo
     */
    public static void updateTicketToBean(LoginCommon.UserAppSessionTicket ticket, UserInfo userInfo) {
        if (ticket == null)
            return;
        // byte[] 类型票据
        userInfo.setB2(ticket.getB2().toByteArray());
        userInfo.setB3(ticket.getB3().toByteArray());
        userInfo.setB3Key(ticket.getB3Key().toByteArray());
        userInfo.setB4(ticket.getB4().toByteArray());
        userInfo.setSessionKey(ticket.getSessionKey().toByteArray());
        userInfo.setB4Domain(ticket.getB4Domain());
        userInfo.setIsLoginState(ticket.getIsLoginState());
        // 设置有效时间段，失效时间，需要更新的时间(有效时间过去一半为需要更新的时间)
        int valid = ticket.getValidSecs();
        // valid = 30;
        userInfo.setValidSecs(valid);
        userInfo.setUnvalidSecs(valid + (int) (System.currentTimeMillis() / 1000));
        userInfo.setNeedFlushSecs(valid / 2 + (int) (System.currentTimeMillis() / 1000));
//        L.d("更新票据，当前用户信息：" + GsonUtils.getInstance().toJson(userInfo));
        // 更新SP信息
        UserSPUtils.setParam(UUApp.getInstance().getContext(), SPConstant.SPNAME_USER_INFO, SPConstant.SPKEY_USER_INFO, userInfo);
        userInfo = new UserInfo();
    }

    /**
     * 更新用户信息
     *
     * @param response
     */
    public static void updateUserInfoToBean(UserInterface.UserInfo.Response response, UserInfo userInfo) {
        if (response == null)
            return;
        userInfo.setPhone(response.getPhone());
        userInfo.setImgUrl(response.getImgUrl());
        userInfo.setUserStatus(response.getUserStatus().getNumber());
        L.i("userInfo.displayName:" + userInfo.getDisplayName() + "###");
        userInfo.setDisplayName(userInfo.getDisplayName());
//        L.d("更新用户信息，当前用户信息：" + GsonUtils.getInstance().toJson(userInfo));
        // 更新SP信息
        UserSPUtils.setParam(UUApp.getInstance().getContext(), SPConstant.SPNAME_USER_INFO, SPConstant.SPKEY_USER_INFO, userInfo);
    }

    /**
     * 将账号登陆结果转化为本地对象(用户信息和票据信息)
     *
     * @param response
     * @return
     */
    public static void changeTicketToBean(LoginInterface.SmsLogin.Response response, UserInfo userInfo) {
        // 更新用户其他信息
        if (response == null)
            return;
        userInfo.setPhone(response.getPhone());
        userInfo.setImgUrl(response.getImgUrl());
        userInfo.setUserStatus(response.getUserStatus().getNumber());
        // 更新用户显示信息
        userInfo.setDisplayName(response.getDisplayName());
        // 更新用户票据信息
        LoginCommon.UserAppSessionTicket ticket = response.getSessionTicket();
        if (ticket == null)
            return;
        updateTicketToBean(ticket, userInfo);
    }

    /**
     * 判断当前是否需要更新票据
     *
     * @return
     */
    public static boolean isNeedUpdateTicket() {
        boolean bool1 = getUserInfo().getNeedFlushSecs() <= (System.currentTimeMillis() / 1000);
        boolean bool2 = isPassLogined();
        return bool1 && bool2;
    }

    /**
     * 显示是否登陆弹窗
     *
     * @param mContext
     */
    public static void goToLoginDialog(final Activity mContext) {
        /*if (mContext == null)
            return;
        Config.showMaterialDialog(mContext, "跳转登陆", "您当前尚未登录，是否前去登陆页面?", "登陆", "取消", new OnBtnLeftClickL() {
            @Override
            public void onBtnLeftClick() {
                mContext.startActivity(new Intent(mContext, LoginPhoneActivity.class));
            }
        }, null);*/

        // 不走弹窗，直接跳转到登陆界面
        mContext.startActivity(new Intent(mContext, LoginPhoneActivity.class));
    }


    /**
     * 登陆态失效，重新登陆
     *
     * @param mContext
     */
    public static void goToErrorLoginDialog(final Activity mContext, String content) {
        if (mContext == null)
            return;
        if (Config.isShowReLoginDialog) {
            return;
        }
        if (TextUtils.isEmpty(content)) {
            content = "您的账号已失效，请重新登录";
        }
        Dialog dialog = DialogUtil.getInstance(mContext).showMaterialDialog("重新登录", content, "取消", "登录", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 退出登录，清空用户数据
                UserConfig.clearUserInfo(mContext);

                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("goto", MainActivity.GOTO_MAP);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                mContext.startActivity(intent);

                // 如果规则弹窗显示，关闭规则弹窗
                EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_CLOSE_RULE_DIALOG));
                // 发送刷新底部车详情卡片数据事件
                EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_UPDATE_CAR_DETAIL_BY_LOGIN));

            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 退出登录，清空用户数据
                UserConfig.clearUserInfo(mContext);
                Intent intent = new Intent(mContext, LoginPhoneActivity.class);
                intent.putExtra("goto", MainActivity.GOTO_MAP);
                // 记录从错误登陆页面进入登陆页面
                intent.putExtra(IntentConfig.KEY_FROM, IntentConfig.KEY_FROM_ERROR);
                mContext.startActivity(intent);
            }
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        Config.isShowReLoginDialog = true;

        // 退出登录，清空用户数据
        UserConfig.clearUserInfo(mContext);
        // 关闭长连接和轮询
        ((UUApp) mContext.getApplication()).quitLongConn();

        // 发送关闭取消订单，关闭行程中的轮询定时器弹窗事件,退出登录的事件（关闭掉驾照上传引导提示）
        EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_CLOSE_CANCEL_DIALOG));

        // 重置红点的显示
        Config.otherFee = 0;
        Config.trafficFee = 0;
    }


    /**
     * 判断当前是否是正好登录状态
     *
     * @return
     */
    public static boolean isPassLogined() {
        return isLogining() && getUserInfo().isLoginState();
    }

    /**
     * 判断当前是否是登录状态
     *
     * @return
     */
    private static boolean isLogining() {
        return getUserInfo().getB2() != null && getUserInfo().getB2().length > 0 && getUserInfo().getSessionKey() != null && getUserInfo().getSessionKey().length > 0;
    }


    /**
     * 清空用户信息
     *
     * @param context
     */
    public static void clearUserInfo(Context context) {
        if (context == null)
            return;
        userInfo = new UserInfo();
        UserSPUtils.clearSP(context, SPConstant.SPNAME_USER_INFO);

        // 退出登录,清空H5Cookie
        H5Cookie.removeCookies(context);
        // 清空用户订单信息
        Config.clearOrderId(context);
    }

    /**
     * 获取NetworkTask type
     *
     * @param networkTask
     * @return
     */
    public static int getHttpUrl(final NetworkTask networkTask) {
        int cmd = networkTask.getCmd();
        /**
         * 配置哪些CMD命令是走https协议
         */
        // 当前需要走https协议的CMD命令有：请求验证码接口、短信登陆接口、账号登陆接口、获取语音验证码接口
        if (cmd == Cmd.CmdCode.RequireSmsVerifyCode_SSL_VALUE
                || cmd == Cmd.CmdCode.SmsLogin_SSL_VALUE
                || cmd == Cmd.CmdCode.AccountLogin_SSL_VALUE
                || cmd == Cmd.CmdCode.RequireVoiceVerifyCode_SSL_VALUE
                || cmd == Cmd.CmdCode.GetShContent_SSL_VALUE) {
            return NetworkTask.SSL_NETWORK;
        } else {
            return NetworkTask.HTTP_NETWORK;
        }
    }

    /**
     * 判断请求是否需要走public
     *
     * @param networkTask
     * @return
     */
    public static boolean isPuilc(final NetworkTask networkTask) {

        boolean flag = false;
        int cmd = networkTask.getCmd();
        // 判断是否需要走public
        if (cmd == Cmd.CmdCode.SmsLogin_SSL_VALUE ||
                cmd == Cmd.CmdCode.RequireSmsVerifyCode_SSL_VALUE ||
                cmd == Cmd.CmdCode.AccountLogin_SSL_VALUE ||
                cmd == Cmd.CmdCode.RequireVoiceVerifyCode_SSL_VALUE) {
            return true;
        }
        //说明票据为空
        flag = getUserInfo().getB3() == null || getUserInfo().getB3().length == 0;

        return flag;
    }


    /**
     * 请求网络更新票据
     */
    public static void requestUpdateTicket() {
        L.i("开始执行更新票据的操作...");
        // 确保当前没有在更新票据
        if (!isUpdateTicketing) {
            // 确保请求网络之前网络正常
            if (Config.isNetworkConnected(UUApp.getInstance().getContext())) {
                // 确保当前用户信息正常
                if (getUserInfo().getSessionKey() != null && getUserInfo().getSessionKey().length > 0 && getUserInfo().getB2() != null && getUserInfo().getB2().length > 0) {
                    // 设置当前正在更新票据
                    isUpdateTicketing = true;
                    L.i("开始执行更新票据方法");
                    final LoginInterface.UpdateTicket.Request.Builder request = LoginInterface.UpdateTicket.Request.newBuilder();
                    NetworkTask task = new NetworkTask(Cmd.CmdCode.UpdateTicket_NL_VALUE);
                    task.setBusiData(request.build().toByteArray());
                    NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
                        @Override
                        public void onSuccessResponse(UUResponseData responseData) {
                            if (responseData.getRet() == 0) {
                                try {
                                    LoginInterface.UpdateTicket.Response response = LoginInterface.UpdateTicket.Response.parseFrom(responseData.getBusiData());
                                    if (response.getRet() == 0) {
                                        // 更新用户票据信息
                                        L.i("在更新票据方法中更新票据");
                                        updateTicketToBean(response.getSessionTicket(), userInfo);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onError(VolleyError errorResponse) {
                            L.e("error", errorResponse);
                        }

                        @Override
                        public void networkFinish() {
                            isUpdateTicketing = false;
                        }
                    });
                }
            }
        }
    }


    //############################## get set #######################################

    /**
     * 获取userInfo的信息
     *
     * @return
     */
    public static UserInfo getUserInfo() {
        // 判断当前用户信息
        if (userInfo == null || userInfo.getSessionKey() == null)
            userInfo = GsonUtils.getInstance().fromJson(UserSPUtils.getParam(UUApp.getInstance().getContext(), SPConstant.SPNAME_USER_INFO, SPConstant.SPKEY_USER_INFO), UserInfo.class);
        return userInfo;
    }

    public static String getUUID() {
        if (UUID == null && UUApp.getInstance().getContext() != null) {
            SharedPreferences userInfo = UUApp.getInstance().getContext().getSharedPreferences(SPConstant.SPNAME_UUID, 0);
            UUID = userInfo.getString(SPConstant.SPKEY_UUID, "");
        }
        return UUID;
    }

    public static void setUUID(String UUID) {
        UserConfig.UUID = UUID;
    }
}
