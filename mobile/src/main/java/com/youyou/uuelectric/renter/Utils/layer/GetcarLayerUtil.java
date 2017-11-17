package com.youyou.uuelectric.renter.Utils.layer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import com.youyou.uuelectric.renter.R;

/**
 * Created by liuchao on 2016/1/4.
 * App蒙层工具类
 */
public class GetcarLayerUtil {
    // 取车页面蒙层效果（只允许显示一次）
    public static final String GETCAR_LAYER = "getcar_layer_1.1.3";
    // 蒙层layerView
    public static View layerView = null;

    /**
     * 初始化蒙层
     * @param mContext
     * @param isAdded
     * @param spName
     * @param spKey
     */
    public static void initLayer(final Activity mContext, final boolean isAdded, final String spName, final String spKey) {
        // 初始化蒙层判断是否是第一次打开APP
        if (GetcarLayerUtil.isFirstOpen(mContext, spName, spKey)) {
            layerView = LayoutInflater.from(mContext).inflate(R.layout.fragment_get_car_layer, null);
            // 屏蔽点击事件
            layerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
            LinearLayout layerKnow = (LinearLayout) layerView.findViewById(R.id.getcar_layer_know);
            layerKnow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isAdded) {
                        setFirstOpen(mContext, spName, spKey);
                        GetcarLayerUtil.removeLayer(mContext);
                    }
                }
            });
            GetcarLayerUtil.addLayer(mContext, layerView);
        }
    }

    /**
     * 为rootView添加蒙层
     * @return
     */
    public static void addLayer(Activity mContext, View layerView) {
        if (mContext == null || layerView == null)
            return;
        ViewGroup contentView = (ViewGroup) mContext.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        contentView.addView(layerView);
    }

    /**
     * 为rootView删除蒙层
     * @param mContext
     */
    public static void removeLayer(Activity mContext) {
        if (mContext == null)
            return;
        ViewGroup contentView = (ViewGroup) mContext.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        if (layerView != null) {
            contentView.removeView(layerView);
        }
    }


    /**
     * 判断是否是第一次打开
     * @param mContext
     * @return
     */
    public static boolean isFirstOpen(Activity mContext, String spName, String spKey) {
        if (mContext == null) {
            return true;
        }
        SharedPreferences sp = mContext.getSharedPreferences(spName, Context.MODE_PRIVATE);
        String resultStr = sp.getString(spKey, "");
        if (!TextUtils.isEmpty(resultStr)) {
            if (resultStr.equals(GETCAR_LAYER)) {
                return false;
            }
        }
        return true;
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
        // String versionName = getVersionName(mContext);
        sp.edit().clear().commit();
        sp.edit().putString(spKey, GETCAR_LAYER).commit();
    }
}
