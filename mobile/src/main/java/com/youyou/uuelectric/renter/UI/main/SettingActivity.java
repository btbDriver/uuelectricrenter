package com.youyou.uuelectric.renter.UI.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.UI.web.H5Activity;
import com.youyou.uuelectric.renter.UI.web.H5Constant;
import com.youyou.uuelectric.renter.UI.web.url.URLConfig;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.view.RippleView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SettingActivity extends BaseActivity {

    @InjectView(R.id.fapiao)
    RippleView fapiao;
    @InjectView(R.id.helper)
    RippleView helper;
    @InjectView(R.id.about)
    RippleView about;
    @InjectView(R.id.feedback)
    RippleView feedback;
    @InjectView(R.id.paymentNotice)
    RippleView paymentNotice;
    @InjectView(R.id.peccancyList)
    RippleView peccancyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.inject(this);
        initView();
    }

    public void initView() {
        ((TextView) fapiao.findViewById(R.id.text)).setText(getString(R.string.fapiao));
        ((TextView) helper.findViewById(R.id.text)).setText(getString(R.string.helper));
        ((TextView) about.findViewById(R.id.text)).setText(getString(R.string.about));
        ((TextView) feedback.findViewById(R.id.text)).setText(getString(R.string.feedback));
        ((TextView) paymentNotice.findViewById(R.id.text)).setText(getString(R.string.paymentNotice));
        ((TextView) peccancyList.findViewById(R.id.text)).setText(getString(R.string.peccancyList));

        fapiao.setOnRippleCompleteListener(onRippleCompleteListener);
        helper.setOnRippleCompleteListener(onRippleCompleteListener);
        about.setOnRippleCompleteListener(onRippleCompleteListener);
        feedback.setOnRippleCompleteListener(onRippleCompleteListener);
        paymentNotice.setOnRippleCompleteListener(onRippleCompleteListener);
        peccancyList.setOnRippleCompleteListener(onRippleCompleteListener);
        // 运控显示发票按钮是否显示
        if (URLConfig.getUrlInfo().getInvoice() == null || TextUtils.isEmpty(URLConfig.getUrlInfo().getInvoice().getUrl())) {
            fapiao.setVisibility(View.GONE);
        } else {
            fapiao.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isShowReadPoint();
    }

    /**
     * 是否显示红点
     */
    private void isShowReadPoint() {
        // 是否显示其他带支付费用红点
        if (Config.otherFee == 1) {
            paymentNotice.findViewById(R.id.read_point).setVisibility(View.VISIBLE);
        } else {
            paymentNotice.findViewById(R.id.read_point).setVisibility(View.GONE);
        }
        // 是否显示待处理违章费用红点
        if (Config.trafficFee == 1) {
            peccancyList.findViewById(R.id.read_point).setVisibility(View.VISIBLE);
        } else {
            peccancyList.findViewById(R.id.read_point).setVisibility(View.GONE);
        }
    }

    /**
     * item点击处理事件（添加水波纹效果）
     */
    RippleView.OnRippleCompleteListener onRippleCompleteListener = new RippleView.OnRippleCompleteListener() {
        @Override
        public void onComplete(RippleView rippleView) {
            Intent intent = null;
            switch (rippleView.getId()) {
                // 发票信息
                case R.id.fapiao:
                    intent = new Intent(mContext, H5Activity.class);
                    intent.putExtra(H5Constant.MURL, URLConfig.getUrlInfo().getInvoice().getUrl());
                    intent.putExtra(H5Constant.TITLE, URLConfig.getUrlInfo().getInvoice().getTitle());
                    // 下拉刷新不可用
                    intent.putExtra(H5Constant.CARFLUSH, false);
                    break;
                // 帮助中心
                case R.id.helper:
                    intent = new Intent(mContext, H5Activity.class);
                    intent.putExtra(H5Constant.MURL, URLConfig.getUrlInfo().getHelpCenter().getUrl());
                    intent.putExtra(H5Constant.TITLE, URLConfig.getUrlInfo().getHelpCenter().getTitle());
                    intent.putExtra(H5Constant.CARFLUSH, false);
                    break;
                // 品牌
                case R.id.about:
                    intent = new Intent(mContext, AboutActivity.class);
                    break;
                // 意见反馈
                case R.id.feedback:
                    intent = new Intent(mContext, FeedbackActivity.class);
                    break;
                // 其它待付费用
                case R.id.paymentNotice:
                    intent = new Intent(mContext, H5Activity.class);
                    intent.putExtra(H5Constant.MURL, URLConfig.getUrlInfo().getPaymentNotice().getUrl());
                    intent.putExtra(H5Constant.TITLE, URLConfig.getUrlInfo().getPaymentNotice().getTitle());
                    intent.putExtra(H5Constant.ISPAYMENTNOTICE, true);
                    break;
                // 待处理违章
                case R.id.peccancyList:
                    intent = new Intent(mContext, H5Activity.class);
                    intent.putExtra(H5Constant.MURL, URLConfig.getUrlInfo().getPeccancyList().getUrl());
                    intent.putExtra(H5Constant.TITLE, URLConfig.getUrlInfo().getPeccancyList().getTitle());
                    intent.putExtra(H5Constant.ISPECCANCYLIST, true);
                    break;
            }
            startActivity(intent);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == null)
            return true;
        if (item.getItemId() == android.R.id.home || item.getItemId() == 0) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);

    }
}
