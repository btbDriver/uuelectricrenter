package com.youyou.uuelectric.renter.UI.main.user;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.rey.material.widget.Button;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.passport.pb.iface.LoginInterface;
import com.uu.facade.user.protobuf.bean.UserInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Network.listen.OnClickNetworkListener;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.Network.user.UserStatus;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.UI.license.ValidateLicenseActivity;
import com.youyou.uuelectric.renter.UI.main.MainActivity;
import com.youyou.uuelectric.renter.Utils.DialogUtil;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;
import com.youyou.uuelectric.renter.Utils.view.RippleView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

public class UserInfoActivity extends BaseActivity {

    @InjectView(R.id.b4_button)
    Button button;
    @InjectView(R.id.div)
    RelativeLayout div;
    @InjectView(R.id.card)
    RelativeLayout card;
    @InjectView(R.id.div_rip)
    RippleView divRip;
    /**
     * 用户头像
     */
    @InjectView(R.id.userinfo_image)
    NetworkImageView userInfoImage;
    /**
     * 用户状态
     */
    @InjectView(R.id.userinfo_status)
    TextView userInfoStatus;
    /**
     * 驾驶证认证引导
     */
    @InjectView(R.id.iv_userinfo_getcash)
    ImageView ivUserinfoGetcash;
    @InjectView(R.id.tv_userinfo_getcash)
    TextView tvUserinfoGetcash;
    @InjectView(R.id.ll_userinfo_getcash)
    LinearLayout llUserinfoGetcash;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        ButterKnife.inject(this);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressLayout.showLoading();
        // 初始化用户状态
        requestUserInfo();
        if(MainActivity.driverLicenseActivityDescResult != null){
            llUserinfoGetcash.setVisibility(View.VISIBLE);
            tvUserinfoGetcash.setText(MainActivity.driverLicenseActivityDescResult.getPersonalPageDesc());
        }else{
            llUserinfoGetcash.setVisibility(View.GONE);
        }
    }

    private void initView() {
        mProgressLayout.showLoading();
        button.setText(getString(R.string.logout));
        ((TextView) div.findViewById(R.id.text)).setText(getString(R.string.div));
        ((TextView) card.findViewById(R.id.text)).setText(getString(R.string.card));
        ((TextView) div.findViewById(R.id.text2)).setText(getString(R.string.div_state_validateing));
        ((TextView) card.findViewById(R.id.text2)).setText(getString(R.string.card_state_nobind));

        /**
         * 退出登录
         */
        button.setOnClickListener(new OnClickNetworkListener() {
            @Override
            public void onNetworkClick(View v) {
                Dialog dialog = DialogUtil.getInstance(mContext).showMaterialDialog("退出登录", "确定要退出登录吗？", "取消", "确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 请求网络，退出登录状态
                        requestLogout();
                    }
                });
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
            }
        });
    }


    /**
     * 初始化用户状态
     */
    private void initUserStatus() {
        // 初始化View的显示内容
//        UUApp.getInstance().display(UserConfig.getUserInfo().getImgUrl(), userInfoImage, R.mipmap.ic_launcher_uu);

        userInfoImage.setBackgroundResource(R.mipmap.ic_criclepic_menu);
        userInfoStatus.setText(UserConfig.getUserInfo().getDisplayName());
        final int userStatus = UserConfig.getUserInfo().getUserStatus();
        ((TextView) div.findViewById(R.id.text2)).setText(UserStatus.getUserStatusStrFromStatusInt(userStatus));


        /**
         * 0：新注册                             点击跳转上传驾照页面
         * 1：待审核                             不可点击并提示
         * 2：申请被驳回                          重新上传驾照不需要回填上次上传的驾照信息
         * 3：已通过审核                          不可点击并提示
         * 4：申请被驳回，并且不能再次提交审核数据  不可点击并提示
         */
        // 添加水波纹效果
        divRip.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                if (userStatus == UserStatus.NEW_REG || userStatus == UserStatus.APPLY_REJECTED) {
                    Intent intent = new Intent(UserInfoActivity.this, ValidateLicenseActivity.class);
                    startActivity(intent);
                } else if (userStatus == UserStatus.WAIT_VERIFY) {
                    showSnackBarMsg("正在认证中，感谢您的耐心等待！");
                } else if (userStatus == UserStatus.APPLY_PASSED) {
                    showSnackBarMsg("您已通过认证，无需重新上传");
                } else if (userStatus == UserStatus.APPLY_REJECTED_NOT_ALLOW_TRY_AGAIN) {
                    showSnackBarMsg("您的账户认证次数过多，请联系客服（" + Config.kefuphone + "）帮助您完成认证。");
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == null)
            return true;
        if (item.getItemId() == android.R.id.home || item.getItemId() == 0) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 请求用户信息异常点击事件
     */
    View.OnClickListener onErrorClickNetworkListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!Config.isNetworkConnected(mContext)) {
                return;
            }
            mProgressLayout.showLoading();
            requestUserInfo();
        }
    };

    /**
     * 请求网络返回用户状态信息
     */
    private void requestUserInfo() {
        UserInterface.UserInfo.Request.Builder request = UserInterface.UserInfo.Request.newBuilder();
        request.setR((int) (System.currentTimeMillis() / 1000));
        NetworkTask task = new NetworkTask(Cmd.CmdCode.UserInfo_NL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        showResponseCommonMsg(responseData.getResponseCommonMsg());
                        UserInterface.UserInfo.Response response = UserInterface.UserInfo.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            // 更新PB信息
                            UserConfig.updateUserInfoToBean(response, UserConfig.getUserInfo());
                            // 初始化显示信息
                            initUserStatus();
                            mProgressLayout.showContent();
                        } else if (response.getRet() == -1) {
                            // -1 退出登录失败
                            mProgressLayout.showError(onErrorClickNetworkListener);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mProgressLayout.showError(onErrorClickNetworkListener);
                    }
                } else {
                    mProgressLayout.showError(onErrorClickNetworkListener);
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                mProgressLayout.showError(onErrorClickNetworkListener);
            }

            @Override
            public void networkFinish() {
            }
        });
    }


    /**
     * 请求网络退出登录
     */
    private void requestLogout() {
        showProgress(false);
        LoginInterface.Logout.Request.Builder request = LoginInterface.Logout.Request.newBuilder();
        request.setTime((int) (System.currentTimeMillis() / 1000));
        NetworkTask task = new NetworkTask(Cmd.CmdCode.Logout_NL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        showResponseCommonMsg(responseData.getResponseCommonMsg());
                        LoginInterface.Logout.Response response = LoginInterface.Logout.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            // 关闭长连接
                            getApp().quitLongConn();
                            // 清空用户信息
                            UserConfig.clearUserInfo(mContext);

                            if (MainActivity.driverLicenseActivityDescResult != null) {
                                //退出后，去掉首页的上传驾照引导提示。
                                EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_CLOSE_CANCEL_DIALOG));
                            }

                            Intent intent = new Intent(mContext, MainActivity.class);
                            intent.putExtra("goto", MainActivity.GOTO_MAP);

                            Config.clearOrderId(mContext);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);

                            // 重置红点的显示
                            Config.otherFee = 0;
                            Config.trafficFee = 0;

                            // 发送刷新底部车详情卡片数据事件
                            EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_UPDATE_CAR_DETAIL_BY_LOGIN));

                        } else if (response.getRet() == -1) {
                            // -1 退出登录失败
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showDefaultNetworkSnackBar();
                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                showDefaultNetworkSnackBar();
            }

            @Override
            public void networkFinish() {
                dismissProgress();
            }
        });
    }

}
