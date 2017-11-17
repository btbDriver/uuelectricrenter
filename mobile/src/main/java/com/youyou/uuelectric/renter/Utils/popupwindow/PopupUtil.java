package com.youyou.uuelectric.renter.Utils.popupwindow;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.Utils.DisplayUtil;

/**
 * Created by liuchao on 2016/1/5.
 */
public class PopupUtil {

    public static PopupWindow popupWindow = null;

    /**
     * 显示popupwindow 菜单
     * @param mContext
     * @param anchorView
     * @param layoutId
     * @return Object[]
     */
    public static View showPopupWindowMenu(Activity mContext, View anchorView, int layoutId) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layoutId, null);
        popupWindow = new PopupWindow(view, DisplayUtil.dip2px(mContext, 148), WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.menu_bg));
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);

        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        popupWindow.setAnimationStyle(R.style.popwin_anim_style);
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY,
                location[0] - popupWindow.getWidth() + anchorView.getWidth() - DisplayUtil.dip2px(mContext, 12),
                location[1] + anchorView.getHeight() - DisplayUtil.dip2px(mContext, 10));

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                popupWindow = null;
            }
        });
        return view;
    }

    /**
     * 关闭popupWindow
     */
    public static void dismiss() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        popupWindow = null;
    }
}
