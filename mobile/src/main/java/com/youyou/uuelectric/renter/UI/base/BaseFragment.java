package com.youyou.uuelectric.renter.UI.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.uu.access.app.header.HeaderCommon;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.view.ProgressLayout;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 08/28 028.
 */
public abstract class BaseFragment extends Fragment {
    public Activity mContext;

    public String getName() {
        return BaseFragment.this.getClass().getSimpleName();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        setHasOptionsMenu(true);
//        LeakCanary.install(mContext.getApplication()).watch(this);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getName());
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getName());
    }


    View baseView;
    View childView;
    public ProgressLayout mProgressLayout;

    public void onEventMainThread(BaseEvent event) {
    }

    /**
     * 内容布局容器
     */
    public FrameLayout mContentContainer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseView = inflater.inflate(R.layout.fragment_base_layout, null);
        mProgressLayout = (ProgressLayout) baseView.findViewById(R.id.progress_layout);
        mContentContainer = (FrameLayout) baseView.findViewById(R.id.fl_content_container);
        childView = setView(inflater, container, savedInstanceState);
        if (childView != null)
            mContentContainer.addView(childView, 0);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        return baseView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        // 当前Fragment销毁时反注册ButterKnife
        // ButterKnife.reset(this);
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

    public void showResponseCommonMsg(HeaderCommon.ResponseCommonMsg msg, View.OnClickListener listener) {
        /*AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(msg.getMsg());
        builder.setNegativeButton(msg.getButtonsList().get(0).getButtonText(), listener);
        Dialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();*/

        Config.showTiplDialog(mContext, null, msg.getMsg(), msg.getButtonsList().get(0).getButtonText(), null);
    }

    public void showResponseCommonMsgToast(HeaderCommon.ResponseCommonMsg msg) {
        if (msg.hasShowType()) {
            if (msg.getShowType().equals(HeaderCommon.ResponseCommonMsgShowType.TOAST)) {
//                showSnackBarMsg(msg.getMsg());
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
            Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示一个默认时间的SnackBar
     *
     * @param msg
     */
    public void showSnackBarMsg(String msg) {
        Config.showToast(mContext, msg);
    }

    public void showNetworkErrorSnackBarMsg() {
        if (isAdded()) {
            Config.showToast(mContext, getString(R.string.network_error_tip));
        }
    }

    /**
     * 不在使用onCreateView方法设置view,而是使用setView方法设置
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    public abstract View setView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);


}
