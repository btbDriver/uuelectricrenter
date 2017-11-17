package com.youyou.uuelectric.renter.UI.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.rey.material.widget.Button;
import com.rey.material.widget.EditText;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.passport.pb.bean.LoginCommon;
import com.uu.facade.passport.pb.iface.LoginInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Network.listen.OnClickFastListener;
import com.youyou.uuelectric.renter.Network.listen.OnClickNormalListener;
import com.youyou.uuelectric.renter.Network.listen.TextWatcherAdapter;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.Service.LoopRequest;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.UI.main.MainActivity;
import com.youyou.uuelectric.renter.UI.web.H5Activity;
import com.youyou.uuelectric.renter.UI.web.H5Constant;
import com.youyou.uuelectric.renter.UI.web.url.URLConfig;
import com.youyou.uuelectric.renter.Utils.RegexUtils;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * Created by liuchao on 2015/9/6.
 * 验证手机
 */
public class LoginPhoneActivity extends BaseActivity {

    /**
     * 开始使用
     */
    @InjectView(R.id.b3_button)
    public Button startUser = null;
    /**
     * 手机号Edittext
     */
    @InjectView(R.id.validate_phone_edit)
    public EditText validatePhoneEdit = null;
    /**
     * 获取验证码
     */
    @InjectView(R.id.validate_phone_button)
    public EditText validatePhone = null;


    @InjectView(R.id.validate_phone_delete)
    public ImageView validatePhoneDelete = null;
    /**
     * 获取验证啊button
     */
    @InjectView(R.id.validate_phone_view)
    public View validatePhoneView = null;
    /**
     * 验证码edit
     */
    @InjectView(R.id.validate_code_edit)
    public EditText validateCodeEdit = null;
    /**
     * 语音验证码请求连接
     */
    @InjectView(R.id.validate_desc_text)
    public TextView validate_desc_text = null;
    /**
     * 友友用车租赁合约
     */
    @InjectView(R.id.validate_buttom_agree)
    public TextView validateButtomAgree = null;

    /**
     * 倒计时工具类
     */
    private LoginPhoneCountDown loginPhoneCountDown = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone);

        ButterKnife.inject(this);
        initView();
    }

    /**
     * 初始化组件
     */
    private void initView() {
        /**
         * “请输入手机号”输入监听
         */
        validatePhoneEdit.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 判断清除图标是否显示
                deleteEnable(charSequence);
                // 判断按钮是否可用
                setButtonEnable();
            }
        });
        // 输入手机号焦点触发删除图标是否显示
        validatePhoneEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    if (!validatePhoneEdit.getText().toString().isEmpty())
                        validatePhoneDelete.setVisibility(View.VISIBLE);
                } else {
                    validatePhoneDelete.setVisibility(View.GONE);
                }
            }
        });
        /**
         * 验证码输入框监听输入
         */
        validateCodeEdit.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 判断按钮是否可用
                setButtonEnable();
            }
        });
        /**
         * 清空按钮点击事件
         */
        validatePhoneDelete.setOnClickListener(new OnClickNormalListener() {
            @Override
            public void onNormalClick(View v) {
                validatePhoneEdit.setText("");
                // 停止倒计时
                if (loginPhoneCountDown != null) {
                    loginPhoneCountDown.cancel();
                }
                validatePhoneView.setOnClickListener(onClickNetworkListener);
                validatePhone.setText("获取验证码");
                validateCodeEdit.setText("");
            }
        });
        /**
         * 获取验证码按钮
         */
        validatePhoneView.setOnClickListener(onClickNetworkListener);
        /**
         * 获取语音验证码
         */
        validate_desc_text.setOnClickListener(onClickNetworkListener);
        /**
         * 友友用车租赁条款
         */
        validateButtomAgree.setOnClickListener(onClickNetworkListener);
        /**
         * 开始使用，默认设置不可用
         */
        startUser.setText("开始使用");
        Config.setB3ViewEnable(startUser, false);
        startUser.setOnClickListener(new OnClickFastListener() {
            @Override
            public void onFastClick(View v) {
                if (!Config.isNetworkConnected(mContext)) {
                    Config.showToast(v.getContext(), v.getContext().getResources().getString(R.string.network_error_tip));
                } else {
                    // 判断手机号和验证啊
                    String phone2 = validatePhoneEdit.getText().toString();
                    String code = validateCodeEdit.getText().toString();
                    // 请求服务器，短信登陆
                    requestSmsLogin(phone2, code);
                }
            }
        });
    }


    /**
     * 修复友盟BUG
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
            savedInstanceState = null;
        }
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

    @Override
    public void onBackPressed() {
        if (getIntent().hasExtra("goto")) {
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra("goto", getIntent().getStringExtra("goto"));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mContext.startActivity(intent);
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止倒计时
        if (loginPhoneCountDown != null) {
            loginPhoneCountDown.cancel();
            loginPhoneCountDown = null;
        }
    }

    /**
     * 网络请求点击事件
     */
    View.OnClickListener onClickNetworkListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (!Config.isNetworkConnected(mContext)) {
                showDefaultNetworkSnackBar();
                return;
            }
            switch (v.getId()) {
                // 获取验证码
                case R.id.validate_phone_view:
                    String phone = validatePhoneEdit.getText().toString();
                    // 验证手机号是否合法合法
                    if (RegexUtils.regexMobilePhone(phone)) {
                        requestValidatePhone(phone);
                    } else {
                        showSnackBarMsg("请输入正确的手机号码");
                    }
                    break;
                // 语音验证码
                case R.id.validate_desc_text:
                    String phone1 = validatePhoneEdit.getText().toString();
                    // 验证手机号是否合法合法
                    if (RegexUtils.regexMobilePhone(phone1)) {
                        requestVoiceVerifyCode(phone1);
                    } else {
                        showSnackBarMsg("请输入正确的手机号码");
                    }
                    break;
                // 友友用车租赁条款
                case R.id.validate_buttom_agree:
                    Intent intent = new Intent(mContext, H5Activity.class);
                    intent.putExtra(H5Constant.MURL, URLConfig.getUrlInfo().getRegister().getUrl());
                    intent.putExtra(H5Constant.TITLE, URLConfig.getUrlInfo().getRegister().getTitle());
                    intent.putExtra(H5Constant.CARFLUSH, false);
                    startActivity(intent);
                    break;
            }
        }
    };


    // ############################### 网络请求 ######################################################

    /**
     * 请求语音验证码
     *
     * @param phone
     */
    private void requestVoiceVerifyCode(String phone) {
        showProgress(false);
        LoginInterface.RequireVoiceVerifyCode.Request.Builder request = LoginInterface.RequireVoiceVerifyCode.Request.newBuilder();
        request.setPhone(phone);
        request.setScene(LoginCommon.VerifyCodeScene.SMS_LOGIN);
        NetworkTask task = new NetworkTask(Cmd.CmdCode.RequireVoiceVerifyCode_SSL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        showResponseCommonMsg(responseData.getResponseCommonMsg());
                        LoginInterface.RequireSmsVerifyCode.Response response = LoginInterface.RequireSmsVerifyCode.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() > 0) {
                            // 设置短信验证码
                            validateCodeEdit.setText("" + response.getRet());
                        } else if (response.getRet() == 0) {

                        } else if (response.getRet() == -1 ||
                                response.getRet() == -2 ||
                                response.getRet() == -3) {
                            // -1 获取验证码失败
                            // -2 手机号格式不正确
                            // -3 用户是新用户，导致不能下发验证码
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

    /**
     * 请求获取短信验证码
     */
    private void requestValidatePhone(String phone) {
        showProgress(false);
        LoginInterface.RequireSmsVerifyCode.Request.Builder request = LoginInterface.RequireSmsVerifyCode.Request.newBuilder();
        request.setPhone(phone);
        request.setScene(LoginCommon.VerifyCodeScene.SMS_LOGIN);
        NetworkTask task = new NetworkTask(Cmd.CmdCode.RequireSmsVerifyCode_SSL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        showResponseCommonMsg(responseData.getResponseCommonMsg());
                        LoginInterface.RequireSmsVerifyCode.Response response = LoginInterface.RequireSmsVerifyCode.Response.parseFrom(responseData.getBusiData());

                        if (response.getRet() > 0) {
                            // 设置短信验证码
                            validateCodeEdit.setText("" + response.getRet());
                        }
                        if (response.getRet() >= 0) {
                            // 开始倒计时。。。。
                            loginPhoneCountDown = new LoginPhoneCountDown(LoginPhoneActivity.this);
                            loginPhoneCountDown.start();
                            // 设置不可点击
                            validatePhoneView.setOnClickListener(null);
                        } else if (response.getRet() == -1 ||
                                response.getRet() == -2 ||
                                response.getRet() == -3) {
                            // -1 获取验证码失败
                            // -2 手机号码格式不正确
                            // -3 申请验证码太频繁
                        }

                        // 验证码输入框获取焦点
                        if (response.getRet() == 0) {
                            validateCodeEdit.setText("");
                            validateCodeEdit.requestFocus();
                            // 获取焦点时打开软件盘
                            InputMethodManager imm = (InputMethodManager) validateCodeEdit.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
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


    /**
     * 请求服务器短信登陆
     *
     * @param phone
     * @param code
     */
    private void requestSmsLogin(String phone, String code) {
        showProgress(false);
        LoginInterface.SmsLogin.Request.Builder request = LoginInterface.SmsLogin.Request.newBuilder();
        request.setPhone(phone); // 设置手机号
        request.setVerifyCode(code); // 设置验证码
        NetworkTask task = new NetworkTask(Cmd.CmdCode.SmsLogin_SSL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        showResponseCommonMsg(responseData.getResponseCommonMsg());
                        LoginInterface.SmsLogin.Response response = LoginInterface.SmsLogin.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            // 停止倒计时
                            if (loginPhoneCountDown != null) {
                                loginPhoneCountDown.cancel();
                            }
                            validatePhoneView.setClickable(true);
                            // 将用户信息保存至内存
                            UserConfig.changeTicketToBean(response, UserConfig.userInfo);
                            // 登陆成功之后的跳转逻辑
                            // 启动长连接
                            getApp().startLongConn();
                            // 清除拉去消息的版本号
                            mContext.getSharedPreferences(LoopRequest.SP_NAME, Context.MODE_PRIVATE).edit().clear().commit();
                            // 每次登录，则清空通知栏
                            getApp().notificationManager.cancelAll();

                            Config.isShowReLoginDialog = false;
                            // 发送刷新底部车详情卡片数据事件
                            EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_UPDATE_CAR_DETAIL_BY_LOGIN));

                            // 判断是否从错误页面进入登陆页面并成功登陆
                            if (getIntent() != null && getIntent().hasExtra(IntentConfig.KEY_FROM) && IntentConfig.KEY_FROM_ERROR.equals(getIntent().getStringExtra(IntentConfig.KEY_FROM))) {
                                Intent intent = new Intent(mContext, MainActivity.class);
                                intent.putExtra("goto", MainActivity.GOTO_MAP);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                mContext.startActivity(intent);
                            }
                            // 判断是否从scheme解析跳转登录页面
                            else if (getIntent() != null && getIntent().hasExtra(H5Constant.BACKTOSCHEMEURL) && !TextUtils.isEmpty(getIntent().getStringExtra(H5Constant.BACKTOSCHEMEURL))) {
                                // 解析跳转的scheme
                                String schemeUrl = getIntent().getStringExtra(H5Constant.BACKTOSCHEMEURL);
                                Intent intent = H5Constant.buildSchemeFromUrl(schemeUrl);
                                mContext.startActivity(intent);
                                finish();
                            } else {
                                finish();
                            }
                        } else if (response.getRet() == -1 ||
                                response.getRet() == -2 ||
                                response.getRet() == -4) {
                            // -1 验证码错误
                            // -2 登陆失败
                            // -4 验证码失效
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


    /**
     * 设置判断删除图标是否可见
     *
     * @param charSequence
     */
    private void deleteEnable(CharSequence charSequence) {
        // 判断清除图标是否显示
        if (!TextUtils.isEmpty(charSequence)) {
            validatePhoneDelete.setVisibility(View.VISIBLE);
        } else {
            validatePhoneDelete.setVisibility(View.GONE);
        }
    }


    /**
     * 判断按钮是否可用
     */
    private void setButtonEnable() {
        // 设置开始使用按钮是否可用
        String phone = validatePhoneEdit.getText().toString();
        String code = validateCodeEdit.getText().toString();
        if (RegexUtils.regexMobilePhone(phone) && RegexUtils.regexMobileCode(code)) {
            // 设置按钮可用
            Config.setB3ViewEnable(startUser, true);
        } else {
            // 设置按钮不可用
            Config.setB3ViewEnable(startUser, false);
        }
    }
}
