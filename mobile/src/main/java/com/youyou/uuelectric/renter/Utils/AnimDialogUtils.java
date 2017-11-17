package com.youyou.uuelectric.renter.Utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.Utils.Support.L;

/**
 * 使用弹性动画，显示广告和一些自定义界面的弹窗工具类
 */
public class AnimDialogUtils {
    private Activity context;
    private ViewGroup androidContentView;
    private View rootView;
    private FrameLayout flContentContainer;
    private RelativeLayout animContainer;
    private ImageView ivClose;
    private SpringSystem springSystem;

    public static final String ANIM_DIALOG_TAG = "AnimDialogTag";
    private boolean isShowing = false;

    private AnimDialogUtils(Activity context) {
        this.context = context;
        springSystem = SpringSystem.create();
    }

    public static AnimDialogUtils getInstance(Activity context) {
        return new AnimDialogUtils(context);
    }


    public AnimDialogUtils initView(View customView, final View.OnClickListener closeClickListener) {
        return initView(customView, closeClickListener, true);
    }

    /**
     * 初始化弹窗中的界面，添加传入的customView界面，并监听关闭按钮点击事件
     *
     * @param customView
     * @param closeClickListener
     * @return
     */
    public AnimDialogUtils initView(View customView, final View.OnClickListener closeClickListener, boolean isShowClose) {
        androidContentView = (ViewGroup) context.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        rootView = LayoutInflater.from(context).inflate(R.layout.anim_dialog_layout, null);
        rootView.setTag(ANIM_DIALOG_TAG);
        animContainer = (RelativeLayout) rootView.findViewById(R.id.anim_container);
        animContainer.setVisibility(View.INVISIBLE);
        flContentContainer = (FrameLayout) rootView.findViewById(R.id.fl_content_container);
        ViewGroup.LayoutParams contentParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        flContentContainer.addView(customView, contentParams);

        ivClose = (ImageView) rootView.findViewById(R.id.iv_close);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (closeClickListener != null) {
                    closeClickListener.onClick(view);
                }
                dismiss();
            }
        });
        if (isShowClose) {
            ivClose.setVisibility(View.VISIBLE);
        } else {
            ivClose.setVisibility(View.GONE);
        }

        return this;
    }

    public void show() {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        androidContentView.addView(rootView, params);

        L.i("执行dialog.show方法....");
        /*flContentContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                flContentContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                L.i("执行onGlobalLayout.startAnim方法...");
                startAnim();
                isShowing = true;
                L.i("执行onShow方法,isShowing:" + isShowing);
            }
        });*/
        L.i("执行onGlobalLayout.startAnim方法...");
        startAnim();
        isShowing = true;
        L.i("执行onShow方法,isShowing:" + isShowing);
    }
    public void dismiss() {
        closeAnim(rootView);
    }

    /**
     * 关闭订单没有弹窗
     */
    public void dismissNoAnim() {
        closeNoAnim();
    }

    /**
     * 开始时从上到下的动画
     */
    private void startAnim() {
        Spring tranSpring = springSystem.createSpring();
        tranSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringActivate(Spring spring) {
                animContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSpringUpdate(Spring spring) {
                animContainer.setTranslationY((float) spring.getCurrentValue());
            }
        });
        SpringConfig springConfig = SpringConfig.fromBouncinessAndSpeed(1, 1);
        tranSpring.setSpringConfig(springConfig);
        tranSpring.setCurrentValue(DisplayUtil.screenhightPx);
        tranSpring.setEndValue(0);
    }

    /**
     * 关闭时退出屏幕的动画
     *
     * @param view
     */
    public void closeAnim(final View view) {
        if (view != null) {
            view.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    androidContentView.removeView(rootView);
                    isShowing = false;
                }
            }).setDuration(500).setInterpolator(new AccelerateInterpolator()).start();

        }
    }

    /**
     * 关闭弹窗
     */
    public void closeNoAnim() {
        androidContentView.removeView(rootView);
        isShowing = false;
    }

    public boolean isShowing() {
        return isShowing;
    }
}
