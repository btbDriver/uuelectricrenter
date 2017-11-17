package com.youyou.uuelectric.renter.UI.main.rentcar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.uu.facade.order.pb.bean.OrderInterface;
import com.youyou.uuelectric.renter.Network.user.SPConstant;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseFragment;
import com.youyou.uuelectric.renter.UI.order.NeedPayOrderFragment;
import com.youyou.uuelectric.renter.UI.web.H5Activity;
import com.youyou.uuelectric.renter.UI.web.H5Constant;
import com.youyou.uuelectric.renter.Utils.AnimDialogUtils;
import com.youyou.uuelectric.renter.Utils.DisplayUtil;
import com.youyou.uuelectric.renter.Utils.Support.L;

/**
 * Created by aaron on 16/5/17.
 * 当前行程页面
 */
public class CurrentStrokePresenter extends BaseFragment{

    // 1:点击了日结弹窗四小时还车按钮，0：未点击日结弹窗四小时还车按钮
    public static final int CLICKED_FOURHOUR = 1;
    public static final int NOTCLICKED_FOURHOUR = -1;

    public int NO_DIALOG = -1;// 没有弹窗
    public int DIALOG_TWOFOUR = 0;// 24小时弹窗
    public int DIALOG_TWOEIGHT = 1;// 28小时弹窗
    public int DIALOG_FOUREIGHT = 2;// 48小时弹窗
    public int DIALOG_FIVETWO = 3;// 52小时弹窗
    public int DIALOG_SEVENTWO = 4;// 72小时弹窗
    public int DIALOG_SEVENSIX = 5;// 76小时弹窗
    public int CURRENT_DIALOG_TYPE = NO_DIALOG;

    private AnimDialogUtils dialogUtils = null;

    @Override
    public View setView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }


    /**
     * 解析网络请求结果中的日结信息
     * @param response
     */
    public void parserDayClearInfo(OrderInterface.OnTripOrderInfo.Response response, String orderId,
                                   RelativeLayout currentToptip, TextView currentToptipBtn, TextView currentToptipMsg) {
        // 显示日结信息弹窗
        L.i("dayClearFlag:" + response.getDayClearFlag());
        // 更新日结弹窗提示
        showDayClearDialog(response, orderId);
        // 日结tip提示
        updateToptipUi(response, currentToptip, currentToptipBtn, currentToptipMsg, response.getDayClearTipFlag());
    }

    /**
     * 显示订单日结弹窗
     */
    private void showDayClearDialog(final OrderInterface.OnTripOrderInfo.Response response, final String orderId) {
        L.i("开始显示订单日结的弹窗...");
        // 服务器端返回-1.不显示弹窗，则销毁当前弹窗
        if (response.getDayClearFlag() == NO_DIALOG) {
            L.i("服务器返回订单flag:" + response.getDayClearFlag() + " 取消弹窗");
            dismissDialog();
            return;
        }
        // 若当前弹窗正在显示，且显示的弹窗与需要显示的弹窗相同，则直接return
        if (dialogUtils != null && dialogUtils.isShowing() && CURRENT_DIALOG_TYPE == response.getDayClearFlag()) {
            L.i("当前订单正在显示,且服务器下发弹窗类型与现实弹窗类型一样,则直接return");
            return;
        }
        // 判断当前页面是否是待支付页面,否则不弹出日结弹窗
        L.i("this.class:" + this.getClass().getSimpleName() + "   CurrentStrokeFragment:" + NeedPayOrderFragment.class.getSimpleName());
        if (this.getClass().getSimpleName().trim().equals(NeedPayOrderFragment.class.getSimpleName())) {
            dismissDialog();
            return;
        }
        // 销毁其他弹窗
        dismissDialog();
        // 更新弹窗类型
        CURRENT_DIALOG_TYPE = response.getDayClearFlag();

        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.currentstroke_dayclear_dialog, null);
        dialogUtils = AnimDialogUtils.getInstance(mContext).initView(dialogView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                L.i("执行了弹窗取消事件...");
            }
        }, !getIsTwoBtnDialog(response.getDayClearFlag()));

        TextView dayClearTitle = (TextView) dialogView.findViewById(R.id.dayclear_title);  // 弹窗title
        TextView dayClearContent = (TextView) dialogView.findViewById(R.id.dayclear_content);// 弹窗文案
        Button dayClearFourHour = (Button) dialogView.findViewById(R.id.dayclear_fourhour_btn);// 四小时还车
        Button dayClearRecharge = (Button) dialogView.findViewById(R.id.dayclear_recharge_btn);// 去充值
        dayClearTitle.setText(response.getDayClearTitle());
        dayClearContent.setText(response.getDayClearText());
        dayClearFourHour.setText(response.getDayClearBackCar());
        dayClearRecharge.setText(response.getDayClearChargeButtonText());
        // 设置四小时还车是否可见
        dayClearFourHour.setVisibility(getIsTwoBtnDialog(response.getDayClearFlag()) ? View.VISIBLE : View.GONE);
        // 四小时还车
        dayClearFourHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogUtils.dismiss();
                // 点击操作保存在本地
                saveSPIsClickedFourhourBtn(mContext, orderId);
            }
        });
        // 去充值
        dayClearRecharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogUtils.dismiss();
                if (getIsTwoBtnDialog(response.getDayClearFlag())) {
                    // 点击操作保存在本地
                    saveSPIsClickedFourhourBtn(mContext, orderId);
                }
                // 若日接订单去充值URL我空，则屏蔽
                if (!TextUtils.isEmpty(response.getDayClearChargeButtonUrl())) {
                    Intent intent = new Intent(mContext, H5Activity.class);
                    // 充值页面不可刷新
                    intent.putExtra(H5Constant.CARFLUSH, false);
                    intent.putExtra(H5Constant.MURL, response.getDayClearChargeButtonUrl());
                    intent.putExtra(H5Constant.TITLE, response.getDayClearChargePageTile());
                    startActivity(intent);
                }
            }
        });

        dialogView.post(new Runnable() {
            @Override
            public void run() {

                // 当弹窗高度大于428dp时，手动将其设置为428dp这是最大高度
                int measuredHeight = dialogView.getMeasuredHeight();
                int maxHeight = DisplayUtil.dip2px(mContext, 428);
                if (measuredHeight > maxHeight) {
                    ViewGroup.LayoutParams layoutParams = dialogView.getLayoutParams();
                    layoutParams.height = maxHeight;
                    dialogView.requestLayout();
                }
            }
        });

        dialogUtils.show();
    }

    /**
     * 更新toptip ui以及点击事件
     */
    private void updateToptipUi(final OrderInterface.OnTripOrderInfo.Response response, RelativeLayout currentToptip,
                                TextView currentToptipBtn, TextView currentToptipMsg, boolean isShowTip) {
        L.i("是否现实当前行程tip:" + isShowTip);
        if (isShowTip && currentToptip.getVisibility() == View.GONE) {
            currentToptip.setVisibility(View.VISIBLE);
            currentToptipMsg.setText(response.getDayClearTip());
            currentToptipBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(response.getDayClearChargeButtonUrl())) {
                        Intent intent = new Intent(mContext, H5Activity.class);
                        // 充值页面不可刷新
                        intent.putExtra(H5Constant.CARFLUSH, false);
                        intent.putExtra(H5Constant.TITLE, response.getDayClearChargePageTile());
                        intent.putExtra(H5Constant.MURL, response.getDayClearChargeButtonUrl());
                        startActivity(intent);
                    }
                }
            });
        } else if (!isShowTip){
            currentToptip.setVisibility(View.GONE);
        }
    }


    /**
     * 保存信息，判断用户是否点击了四小时还车按钮
     */
    public void saveSPIsClickedFourhourBtn(Activity mContext, String orderId) {
        if (TextUtils.isEmpty(orderId)) {
            return;
        }
        L.i("保存点击事件:" + CURRENT_DIALOG_TYPE);
        SharedPreferences sp = mContext.getSharedPreferences(SPConstant.SPNAME_CURRENT_FOURHOURCLICK, Context.MODE_PRIVATE);
        sp.edit().putInt(orderId, CURRENT_DIALOG_TYPE).apply();
    }

    /**
     * 获取保存信息，判断用户是否点击了四小时还车按钮
     * @param mContext
     * @return
     */
    public int getSPIsClickedFourhourBtn(Activity mContext, String orderId) {
        int defaultInt = -1;
        if (TextUtils.isEmpty(orderId)) {
            return defaultInt;
        }
        SharedPreferences sp = mContext.getSharedPreferences(SPConstant.SPNAME_CURRENT_FOURHOURCLICK, Context.MODE_PRIVATE);

        return sp.getInt(orderId, defaultInt);
    }


    /**
     * 销毁弹窗
     */
    public void dismissDialog() {
        // 执行弹窗操作，若当前存在日结弹窗，则销毁
        if (dialogUtils != null && dialogUtils.isShowing()) {
            L.i("执行日结弹窗的操作...");
            dialogUtils.dismissNoAnim();
            dialogUtils = null;
        }
    }

    /**
     * 判断弹窗类型
     * @param dayClearType
     */
    private boolean getIsTwoBtnDialog(int dayClearType) {

        return dayClearType % 2 == 0 ? true : false;
    }
}
