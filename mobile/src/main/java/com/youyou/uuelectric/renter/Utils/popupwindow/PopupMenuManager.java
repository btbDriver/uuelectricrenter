package com.youyou.uuelectric.renter.Utils.popupwindow;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;

import com.uu.facade.base.common.UuCommon;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.main.rentcar.CancelOrderDialog;
import com.youyou.uuelectric.renter.UI.web.H5Activity;
import com.youyou.uuelectric.renter.UI.web.H5Constant;
import com.youyou.uuelectric.renter.UI.web.H5FragmentDialogFactory;
import com.youyou.uuelectric.renter.UI.web.url.WebUrl;
import com.youyou.uuelectric.renter.Utils.view.RippleView;

/**
 * Created by aaron on 16/4/22.
 */
public class PopupMenuManager {
    // 弹出menu菜单的位置
    public static final int GETCAR_SHOW = 1;
    public static final int CURRENT_SHOW = 2;
    public static final int RETURN_SHOW = 3;

    /**
     * 初始化popupmenu菜单
     * @param mContext
     * @param localType
     * @param view
     * @param carConditionUrl
     * @param carGuideUrl
     * @param mCancelOrderDialog
     */
    public static void initPupopWindow(Activity mContext, int localType, View view, final UuCommon.WebUrl carConditionUrl,
                                       final UuCommon.WebUrl carGuideUrl, final UuCommon.WebUrl carSafeUrl, CancelOrderDialog mCancelOrderDialog) {
        // 显示popupwindow菜单
        View contentView = PopupUtil.showPopupWindowMenu(mContext, view, R.layout.fragment_getcar_menu);

        WebUrl carConditionUrlWeb = new WebUrl();
        carConditionUrlWeb.setTitle(carConditionUrl.getTitle());
        carConditionUrlWeb.setUrl(carConditionUrl.getUrl());
        WebUrl carGuideUrlWeb = new WebUrl();
        carGuideUrlWeb.setTitle(carGuideUrl.getTitle());
        carGuideUrlWeb.setUrl(carGuideUrl.getUrl());
        WebUrl carSafeUrlWeb = new WebUrl();
        carSafeUrlWeb.setTitle(carSafeUrl.getTitle());
        carSafeUrlWeb.setUrl(carSafeUrl.getUrl());
        // 添加事件监听
        initPopupListener(mContext, localType, contentView, carConditionUrlWeb, carGuideUrlWeb, carSafeUrlWeb, mCancelOrderDialog);
    }

    /**
     * 重载函数，初始化popupmenu
     * @param mContext
     * @param localType
     * @param view
     * @param carConditionUrl
     * @param carGuideUrl
     * @param mCancelOrderDialog
     */
    public static void initPupopWindow(Activity mContext, int localType, View view, final WebUrl carConditionUrl,
                                       final WebUrl carGuideUrl, final WebUrl carSafeUrl, CancelOrderDialog mCancelOrderDialog) {
        // 显示popupwindow菜单
        View contentView = PopupUtil.showPopupWindowMenu(mContext, view, R.layout.fragment_getcar_menu);
        // 添加事件监听
        initPopupListener(mContext, localType, contentView, carConditionUrl, carGuideUrl, carSafeUrl, mCancelOrderDialog);
    }

    /**
     * 初始化popupWindow事件监听
     * @param mContext
     * @param contentView
     * @param carConditionUrl
     * @param carGuideUrl
     * @param mCancelOrderDialog
     */
    public static void initPopupListener(final Activity mContext, int localType, View contentView, final WebUrl carConditionUrl,
                                         final WebUrl carGuideUrl, final WebUrl carSafeUrl, final CancelOrderDialog mCancelOrderDialog) {
        RippleView feedbackRela = (RippleView) contentView.findViewById(R.id.caritem_feedback_rela);
        RippleView guideRela = (RippleView) contentView.findViewById(R.id.caritem_guide_rela);
        RippleView safeRela = (RippleView) contentView.findViewById(R.id.caritem_safeorder_rela);
        RippleView cancelRela = (RippleView) contentView.findViewById(R.id.caritem_cancelorder_rela);
        // 根据显示的位置，屏蔽不同的菜单
        if (localType != GETCAR_SHOW) {
            cancelRela.setVisibility(View.GONE);
        }

        // 车况反馈
        feedbackRela.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                PopupUtil.dismiss();
                if (carConditionUrl != null && !TextUtils.isEmpty(carConditionUrl.getUrl())) {
                    Intent intent = new Intent(mContext, H5Activity.class);
                    intent.putExtra(H5Constant.TITLE, carConditionUrl.getTitle());
                    intent.putExtra(H5Constant.MURL, carConditionUrl.getUrl());
                    mContext.startActivity(intent);
                }
            }
        });

        // 用车指南
        guideRela.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                PopupUtil.dismiss();
                if (carGuideUrl != null && !TextUtils.isEmpty(carGuideUrl.getUrl())) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            H5FragmentDialogFactory.createFactory((FragmentActivity) mContext).showH5Dialog(carGuideUrl.getUrl());
                        }
                    }, 300);
                }
            }
        });

        // 保险保障
        safeRela.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                PopupUtil.dismiss();
                if (carSafeUrl != null && !TextUtils.isEmpty(carSafeUrl.getUrl())) {
                    Intent intent = new Intent(mContext, H5Activity.class);
                    intent.putExtra(H5Constant.TITLE, carSafeUrl.getTitle());
                    intent.putExtra(H5Constant.MURL, carSafeUrl.getUrl());
                    mContext.startActivity(intent);
                }
            }
        });

        // 取消订单
        cancelRela.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                PopupUtil.dismiss();
                // 弹出取消原因弹窗
                mCancelOrderDialog.showCancelOrderDialog();
            }
        });
    }

    /**
     * 关闭popupWindow
     */
    public static void dismiss() {
        PopupUtil.dismiss();
    }
}
