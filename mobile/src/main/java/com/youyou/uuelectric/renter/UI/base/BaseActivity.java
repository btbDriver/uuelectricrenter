package com.youyou.uuelectric.renter.UI.base;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.systembartint.SystemBarTintManager;
import com.umeng.analytics.MobclickAgent;
import com.uu.access.app.header.HeaderCommon;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.Service.LoopRequest;
import com.youyou.uuelectric.renter.UI.main.MainActivity;
import com.youyou.uuelectric.renter.UI.start.StartActivity;
import com.youyou.uuelectric.renter.UI.web.H5Constant;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.DialogUtil;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;
import com.youyou.uuelectric.renter.Utils.view.ProgressLayout;
import com.youyou.uuelectric.renter.Utils.view.RippleView;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 08/28 028.
 */
public class BaseActivity extends AppCompatActivity {

    /**
     * 子类日志TAG，BaseActivity中初始化
     */
    public static String TAG = "";
    protected Activity mContext;

    public ProgressLayout mProgressLayout;
    public Toolbar mDefaultToolBar;
    public TextView mToolbarTitle;
    private RippleView mRvRight;
    /**
     * 右侧操作按钮
     */
    public TextView mRightOptButton;
    /**
     * ToolBar的父容器
     */
    protected FrameLayout mToolbarContainer;
    /**
     * 内容布局容器
     */
    private FrameLayout mContentContainer;
    private LayoutInflater layoutInflater;


    public String getName() {
        return BaseActivity.this.getClass().getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        Config.setActivityState(this);
        TAG = this.getClass().getSimpleName();
        layoutInflater = LayoutInflater.from(this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * 验证用户票据是否失效，失效的话则默认执行更换票据操作
         *     startActivity中不需要更新票据，否则起始页有可能会弹出登陆提示框
         */
        if (UserConfig.isNeedUpdateTicket() && (!(this instanceof StartActivity) || !this.getClass().getName().equals(StartActivity.class.getName()))) {
            if (!UserConfig.isUpdateTicketing) {
                L.d("onResume 中更新用户票据");
                UserConfig.requestUpdateTicket();
            }
        }

        if (!getName().equals("com.youyou.uuelectric.renter.UI.main.MainActivity"))//首页不用统计stat
        {
            MobclickAgent.onPageStart(getName());
        }
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Config.currentContext = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!getName().equals("com.youyou.uuelectric.renter.UI.main.MainActivity"))//首页不用统计stat
        {
            MobclickAgent.onPageEnd(getName());
        }
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DialogUtil.mActivity == this && DialogUtil.getDialog() != null) {
            // 释放弹窗对象
            DialogUtil.closeDialog();
        }
        UUApp.getInstance().removeActivity(this);
        EventBus.getDefault().unregister(this);
        // 当前activity销毁时，反注册ButterKnife
        // ButterKnife.reset(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        View view = getLayoutInflater().inflate(R.layout.activity_base_layout, null);
        super.setContentView(view);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            view.setFitsSystemWindows(true);
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.colorPrimaryDark);

        }

        initDefaultView(layoutResID);
        initDefaultToolBar();

    }

    /**
     * 初始化默认的布局组件
     *
     * @param layoutResID
     */
    private void initDefaultView(int layoutResID) {
        mProgressLayout = (ProgressLayout) findViewById(R.id.progress_layout);
        mProgressLayout.setAttachActivity(this);
        mProgressLayout.setUseSlideBack(false);
        mToolbarContainer = (FrameLayout) findViewById(R.id.toolbar_container);
        mDefaultToolBar = (Toolbar) findViewById(R.id.default_toolbar);
        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mRvRight = (RippleView) findViewById(R.id.rv_right);
        mRightOptButton = (TextView) findViewById(R.id.right_opt_button);
        mContentContainer = (FrameLayout) findViewById(R.id.fl_content_container);

        View childView = layoutInflater.inflate(layoutResID, null);
        mContentContainer.addView(childView, 0);
    }

    /**
     * 对于不需要再外层包裹父布局的，使用该方法设置布局
     *
     * @param layoutResID
     */
    public void setContentViewNoParent(int layoutResID) {
        super.setContentView(layoutResID);
    }

    /**
     * 初始化默认的ToolBar
     */
    private void initDefaultToolBar() {
        if (mDefaultToolBar != null) {
            String label = getTitle().toString();
            setTitle(label);
            setSupportActionBar(mDefaultToolBar);
            mDefaultToolBar.setNavigationIcon(R.mipmap.toolbar_back_icn_transparent);
        }
    }

    /**
     * 覆盖原有默认的ToolBar，需要传入ToolBar的布局文件，建议该ToolBar单独写在一个布局文件中
     *
     * @param toolbarLayoutId
     */
    protected void overrideDefaultToolbar(@NonNull int toolbarLayoutId) {
        View view = layoutInflater.inflate(toolbarLayoutId, null);
        mToolbarContainer.removeAllViews();
        mToolbarContainer.addView(view);
    }

    protected void overrideDefaultToolbar(@NonNull View toolbarView) {
        mToolbarContainer.removeAllViews();
        mToolbarContainer.addView(toolbarView);
    }

    /**
     * 删除默认的ToolBar
     */
    protected void removeDefaultToolbar() {
        mToolbarContainer.removeAllViews();
        mToolbarContainer.setVisibility(View.GONE);
    }

    /**
     * 初始化ToolBar的Title
     *
     * @param title
     */
    public void setTitle(String title) {
        super.setTitle("");
        if (this.mToolbarTitle != null) {
            this.mToolbarTitle.setVisibility(View.VISIBLE);
            this.mToolbarTitle.setText(title);
        }
    }

    /**
     * 设置右侧操作按钮文案及点击事件
     *
     * @param text
     * @param onClickListener
     */
    public void setRightOptBtnInfo(String text, final View.OnClickListener onClickListener) {
        if (!TextUtils.isEmpty(text)) {
            mRvRight.setVisibility(View.VISIBLE);
            mRightOptButton.setText(text);
            mRvRight.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
                @Override
                public void onComplete(RippleView rippleView) {
                    onClickListener.onClick(rippleView);
                }
            });
        }
    }


    /**
     * event 更改显示登陆状态
     */
    public void onEventMainThread(BaseEvent event) {
        // StartActivity不接收事件
        if (getName().equals("com.youyou.uuelectric.renter.UI.start.StartActivity")) {
            return;
        }
        if (event == null || TextUtils.isEmpty(event.getType()))
            return;
        // 当前Activity和currentContext一致时，才执行事件动作
        if (Config.currentContext != null && Config.currentContext == this) {
            if (EventBusConstant.EVENT_TYPE_NETWORK_TOLOGIN.equals(event.getType())) {
                L.d("base中执行goto login");
                String content = (String) event.getExtraData();
                UserConfig.goToErrorLoginDialog(mContext, content);
            }
            // 解析长连接或者是轮询拉取到的数据
            else if (EventBusConstant.EVENT_TYPE_LONGCONNECTION_LOOP.equals(event.getType())) {
                LoopRequest.parserLongConnResult(event.getExtraData(), mContext);
            }
            // 解析支付返回事件
            else if (EventBusConstant.EVENT_TYPE_ACTIVITY_PAY_BACK.equals(event.getType())) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        H5Constant.isNeedFlush = true;
                        finish();
                    }
                }, 500);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Config.REQUEST_CALL_PHONE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }
                    Intent phoneIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Config.kefuphone));
                    startActivity(phoneIntent);
                } else {
                    showToast(getResources().getString(R.string.no_can_call_phone));
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    /**
     * 显示一个简单的SnackBar，仅显示文本，无操作
     *
     * @param msg
     * @param duration
     */
    public void showSnackBarMsg(String msg, int duration) {
        Config.showToast(mContext, msg);
    }

    /**
     * 显示一个默认时间的SnackBar
     *
     * @param msg
     */
    public void showSnackBarMsg(String msg) {
        Config.showToast(mContext, msg);
    }

    /**
     * 默认的网络错误SnackBar
     */
    public void showDefaultNetworkSnackBar() {
        String msg = getResources().getString(R.string.network_error_tip);
        Config.showToast(mContext, msg);
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public void showProgress(boolean canCancled) {
        Config.showProgressDialog(mContext, canCancled, null);
    }

    public void showProgress(boolean canCancled, String msg) {
        Config.showProgressDialog(mContext, canCancled, null, msg);
    }

    public void showProgress(boolean canCancled, final Config.ProgressCancelListener listener) {
        Config.showProgressDialog(mContext, canCancled, listener);
    }

    public void dismissProgress() {
        Config.dismissProgress();
    }


    public void showResponseCommonMsg(HeaderCommon.ResponseCommonMsg msg) {
        if (msg.getMsg() != null && msg.getMsg().length() > 0) {
            if (msg.hasShowType()) {
                if (msg.getShowType().equals(HeaderCommon.ResponseCommonMsgShowType.TOAST)) {
                    showSnackBarMsg(msg.getMsg());
                } else if (msg.getShowType().equals(HeaderCommon.ResponseCommonMsgShowType.WINDOW)) {
                    showResponseCommonMsg(msg, null);
                }
            } else {
                showSnackBarMsg(msg.getMsg());
            }
        }
    }

    public void showResponseCommonMsg(HeaderCommon.ResponseCommonMsg msg, DialogInterface.OnClickListener listener) {
        Config.showTiplDialog(this, null, msg.getMsg(), msg.getButtonsList().get(0).getButtonText(), null);
    }

    public void showResponseCommonMsgToast(HeaderCommon.ResponseCommonMsg msg) {
        if (msg.hasShowType()) {
            if (msg.getShowType().equals(HeaderCommon.ResponseCommonMsgShowType.TOAST)) {
                showToast(msg.getMsg());
            } else if (msg.getShowType().equals(HeaderCommon.ResponseCommonMsgShowType.WINDOW)) {
                showResponseCommonMsg(msg, null);
            }
        } else {
            showSnackBarMsg(msg.getMsg());
        }
    }

    public void showToast(String text) {
        if (text != null && !text.trim().equals("")) {
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    public UUApp getApp() {
        return (UUApp) getApplicationContext();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (Config.isFastDoubleClick()) {
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if (Config.isNotificationOpen) {
            Config.isNotificationOpen = false;
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra("goto", MainActivity.GOTO_MAP);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }


}
