package com.youyou.uuelectric.renter.UI.web;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;

import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.Utils.AnimDialogUtils;

/**
 * 弹窗中显示H5页面工厂类
 */
public class H5FragmentDialogFactory {

    private FragmentActivity activity;
    private AnimDialogUtils dialog;

    private H5FragmentDialogFactory(FragmentActivity activity) {
        this.activity = activity;
    }

    public static H5FragmentDialogFactory createFactory(FragmentActivity activity) {
        return new H5FragmentDialogFactory(activity);
    }

    public void showH5Dialog(String url,View.OnClickListener closeClickListener) {
        dialog = AnimDialogUtils.getInstance(activity);
        final View dialogView = activity.getLayoutInflater().inflate(R.layout.user_car_guide_layout, null);
        dialog.initView(dialogView, closeClickListener);
        activity.getIntent().putExtra(H5Constant.MURL, url);
        H5Fragment h5Fragment = new H5Fragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(H5Fragment.KEY_DIALOG_WEB_VIEW, true);
        h5Fragment.setArguments(bundle);
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.fl_root_view, h5Fragment).commitAllowingStateLoss();
        dialog.show();
    }

    public void showH5Dialog(String url) {
        showH5Dialog(url, null);
    }

    public AnimDialogUtils getDialog() {
        return dialog;
    }


    /**
     * 判断是否是否已经弹出,针对用户首单弹框只弹出一次
     * @param mContext
     * @return
     */
    public static boolean isFirstOpen(Activity mContext, String spName, String spKey) {
        if (mContext == null) {
            return true;
        }
        String keyValue = UserConfig.getUserInfo().getPhone();
        SharedPreferences sp = mContext.getSharedPreferences(spName, Context.MODE_PRIVATE);
        String resultStr = sp.getString(spKey, "");
        if (!TextUtils.isEmpty(resultStr) && !TextUtils.isEmpty(keyValue)) {
            if (resultStr.contains(keyValue)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否弹窗用车指南
     * @param mContext
     * @param spName
     * @param spKey
     * @return
     */
    public static boolean isShowUseCarTip(Activity mContext, String spName, String spKey){
        boolean b = true;
        if (mContext == null) {
            return b;
        }
        String keyValue = UserConfig.getUserInfo().getPhone();
        SharedPreferences sp = mContext.getSharedPreferences(spName, Context.MODE_PRIVATE);
        b = sp.getBoolean(spKey,true);
        return b;
    }

    /**
     * 设置不在弹出
     * @param mContext
     * @param spName
     * @param spKey
     */
    public static void setShowUseCarTip(Activity mContext, String spName, String spKey){
        if (mContext == null) {
            return;
        }
        SharedPreferences sp = mContext.getSharedPreferences(spName, Context.MODE_PRIVATE);

        SharedPreferences.Editor edit=sp.edit();
        edit.putBoolean(spKey,false);
        edit.commit();

    }


    /**
     * 设置标识符
     * @param mContext
     */
    public static void setFirstOpen(Activity mContext, String spName, String spKey) {
        if (mContext == null) {
            return;
        }
        SharedPreferences sp = mContext.getSharedPreferences(spName, Context.MODE_PRIVATE);
        String keyValue = UserConfig.getUserInfo().getPhone();
        String resultStr = sp.getString(spKey, "");
        sp.edit().clear().commit();
        sp.edit().putString(spKey, resultStr + "\n" + keyValue).commit();
    }
}
