package com.youyou.uuelectric.renter.UI.web;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.uu.facade.pay.pb.bean.PayCommon;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseFragment;
import com.youyou.uuelectric.renter.UI.pay.BasePayFragmentUtils;
import com.youyou.uuelectric.renter.Utils.DisplayUtil;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by liuchao on 2015/11/30.
 */
public class H5Fragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    @InjectView(R.id.sswipeRefreshLayout)
    public SwipeRefreshLayout swipeRefreshLayout;
    /**
     * H5页面 WebView
     */
    @InjectView(R.id.mwebview)
    public WebView mWebView = null;
    @InjectView(R.id.rl)
    public RelativeLayout rl;
    /**
     * 页面title
     */
    public String title = "";
    /**
     * 页面当前URL
     */
    public String currentUrl = "";
    /**
     * 判断网页是否加载成功
     */
    public boolean isSuccess = true;
    /**
     * 判断前一页H5是否需要刷新
     */
    public boolean isNeedFlushPreH5 = false;

    private BasePayFragmentUtils payFragmentUtils;

    public static final String KEY_DIALOG_WEB_VIEW = "dialog_webView";
    /**
     * 是否是弹窗中的WebView
     */
    private boolean isDialogWebView = false;

    View.OnClickListener errorOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mProgressLayout.showLoading();
            isSuccess = true;
            reflush();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        payFragmentUtils = new BasePayFragmentUtils(this, BasePayFragmentUtils.ORDER_TYPE_H5);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(KEY_DIALOG_WEB_VIEW)) {
            isDialogWebView = bundle.getBoolean(KEY_DIALOG_WEB_VIEW, false);
        }
    }

    @Override
    public View setView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        if (isDialogWebView) {
            rootView = inflater.inflate(R.layout.fragment_h5_custom, null);
        } else {
            rootView = inflater.inflate(R.layout.fragment_h5, null);
        }
        ButterKnife.inject(this, rootView);

        if (getActivity() instanceof H5Activity) {
            H5Activity h5Activity = (H5Activity) getActivity();
            mProgressLayout = h5Activity.mProgressLayout;
        }

        initView();
        initData();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        payFragmentUtils.onPayResume();
        if (H5Constant.isNeedFlush == true || isNeedFlushPreH5 == true) {
            H5Constant.isNeedFlush = false;
            isNeedFlushPreH5 = false;
            // 加载数据
            initData();
        }
    }

    private void initView() {
        // 判断下拉刷新组件是否可用
        isSwipeEnable();
        // 初始化WebView组件
        H5FragmentUtils.initH5View(this);
        // 设置WebView的Client
        mWebView.setWebViewClient(new MWebViewClient(this));
        // 设置可现实js的alert弹窗
        mWebView.setWebChromeClient(new WebChromeClient());

        if (isDialogWebView) {
            mProgressLayout.setCornerResId(R.drawable.map_confirm_bg);
        }
    }


    private void initData() {
        mProgressLayout.showLoading();
        // 设置title
        H5FragmentUtils.setTitle(this, title);
        // 获取请求URL
        currentUrl = H5FragmentUtils.getUrl(this, currentUrl);
        // 刷新页面
        reflush();
    }

    /**
     * 判断下拉刷新组件是否可用
     */
    private void isSwipeEnable() {
        if (getActivity() == null) {
            return;
        }

        if (isDialogWebView) {
            getActivity().getIntent().putExtra(H5Constant.CARFLUSH, false);
        }
        //判断滑动组件是否可用
        if (getActivity().getIntent().getBooleanExtra(H5Constant.CARFLUSH, true)) {
            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setColorSchemeResources(R.color.c1, R.color.c1, R.color.c1);
            swipeRefreshLayout.setOnRefreshListener(this);
            mWebView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        int downY = (int) event.getY();
                        if (downY <= DisplayUtil.screenhightPx / 3) {
                            swipeRefreshLayout.setEnabled(true);
                        } else {
                            swipeRefreshLayout.setEnabled(false);
                        }
                    }
                    return false;
                }
            });
        } else {
            swipeRefreshLayout.setEnabled(false);
        }
    }

    @Override
    public void onRefresh() {
        //判断是否执行刷新动作
        reflush();
    }

    /**
     * 刷新当前页面
     */
    private void reflush() {
        if (Config.isNetworkConnected(mContext)) {
            if (!TextUtils.isEmpty(currentUrl)) {
                H5Cookie.synCookies(mContext, currentUrl, H5Cookie.getToken());
                mWebView.loadUrl(currentUrl);
            } else {
                swipeRefreshLayout.setRefreshing(false);
                mProgressLayout.showError(errorOnClickListener);
            }
        } else {
            swipeRefreshLayout.setRefreshing(false);
            mProgressLayout.showError(errorOnClickListener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        /*swipeRefreshLayout.removeView(mWebView);
        mWebView.removeAllViews();
        mWebView.destroy();*/
    }

    /**
     * 进行支付动作
     *
     * @param payInfo
     */
    public void doPayOption(PayCommon.PayInfo payInfo, List<String> orderList) {
        payFragmentUtils.queryPayOrderInfo(payInfo, orderList);
    }

    /**
     * 加载H5页面是否成功
     *
     * @param success
     */
    public void onLoadFinish(boolean success) {

        /*if (success) {
            Activity activity = getActivity();
            if (activity == null)
                return;
            // 打开“其他待处理费用”H5页面成功
            if (activity.getIntent().hasExtra(H5Constant.ISPAYMENTNOTICE) &&
                    activity.getIntent().getBooleanExtra(H5Constant.ISPAYMENTNOTICE, false)) {
                // 取消【其他待处理费用】红点的显示
                // 此处注释，由服务端控制红点是否显示
//                Config.otherFee = 0;
            }
            if (activity.getIntent().hasExtra(H5Constant.ISPECCANCYLIST) &&
                    activity.getIntent().getBooleanExtra(H5Constant.ISPECCANCYLIST, false)) {
                // 取消【待处理违章】红点的显示
                // 此处注释，由服务端控制红点是否显示
//                Config.trafficFee = 0;
            }

        }*/
    }
}
