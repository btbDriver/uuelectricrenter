package com.youyou.uuelectric.renter.Utils.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.youyou.uuelectric.renter.R;

/**
 * User: qing
 * Date: 2015/9/18 13:03
 * Desc:
 */
public class UUSnackBar extends FrameLayout {

    private boolean mRemoveOnDismiss;

    public static final int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;
    public static final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;

    private Animation mInAnimation;
    private Animation mOutAnimation;

    private Runnable mDismissRunnable = new Runnable() {
        @Override
        public void run() {
            dismiss();
        }
    };

    private int mState = STATE_DISMISSED;

    /**
     * Indicate this SnackBar is already dismissed.
     */
    public static final int STATE_DISMISSED = 0;
    /**
     * Indicate this SnackBar is already shown.
     */
    public static final int STATE_SHOWN = 1;
    /**
     * Indicate this SnackBar is being shown.
     */
    public static final int STATE_SHOWING = 2;
    /**
     * Indicate this SnackBar is being dismissed.
     */
    public static final int STATE_DISMISSING = 3;

    private long mDuration = -1;

    private TextView mText;


    public UUSnackBar(Context context) {
        super(context);
        init(context);
    }

    public UUSnackBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UUSnackBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    /**
     * 初始化布局
     */
    private void init(Context context) {

        mInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.abc_slide_in_bottom);
        mOutAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.abc_slide_out_bottom);

        View rootView = LayoutInflater.from(context).inflate(R.layout.snackbar_layout, null);
        mText = (TextView) rootView.findViewById(R.id.tv_snack_bar_text);
        FrameLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(rootView, params);

    }

    /**
     * Set the text that this SnackBar is to display.
     *
     * @param text The text is displayed.
     * @return This SnackBar for chaining methods.
     */
    public UUSnackBar text(CharSequence text) {
        mText.setText(text);
        return this;
    }

    /**
     * Set the text that this SnackBar is to display.
     *
     * @param id The resourceId of text is displayed.
     * @return This SnackBar for chaining methods.
     */
    public UUSnackBar text(int id) {
        return text(getContext().getResources().getString(id));
    }

    /**
     * Set the duration this SnackBar will be shown before dismissing.
     *
     * @param duration If 0, then the SnackBar will not be dismissed until {@link #dismiss() dismiss()} is called.
     * @return This SnackBar for chaining methods.
     */
    public UUSnackBar duration(long duration) {
        mDuration = duration;
        return this;
    }

    /**
     * Set the animation will be shown when SnackBar enter screen.
     *
     * @param anim The animation.
     * @return This SnackBar for chaining methods.
     */
    public UUSnackBar animationIn(Animation anim) {
        mInAnimation = anim;
        return this;
    }

    /**
     * Set the animation will be shown when SnackBar exit screen.
     *
     * @param anim The animation.
     * @return This SnackBar for chaining methods.
     */
    public UUSnackBar animationOut(Animation anim) {
        mOutAnimation = anim;
        return this;
    }


    /**
     * Interface definition for a callback to be invoked when SnackBar's state is changed.
     */
    public interface OnStateChangeListener {

        /**
         * Called when SnackBar's state is changed.
         *
         * @param sb       The SnackBar fire this event.
         * @param oldState The old state of SnackBar.
         * @param newState The new state of SnackBar.
         */
        public void onStateChange(UUSnackBar sb, int oldState, int newState);
    }

    private OnStateChangeListener mStateChangeListener;


    /**
     * Get the current state of this SnackBar.
     *
     * @return The current state of this SnackBar. Can be {@link #STATE_DISMISSED}, {@link #STATE_DISMISSING}, {@link #STATE_SHOWING} or {@link #STATE_SHOWN}.
     */
    public int getState() {
        return mState;
    }

    private void setState(int state) {
        if (mState != state) {
            int oldState = mState;
            mState = state;
            if (mStateChangeListener != null)
                mStateChangeListener.onStateChange(this, oldState, mState);
        }
    }


    /**
     * Show this SnackBar. It will auto attach to the activity's root view.
     *
     * @param activity
     */
    public void show(Activity activity) {
        show((ViewGroup) activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT));
    }

    /**
     * Show this SnackBar. It will auto attach to the parent view.
     *
     * @param parent Must be {@linke android.widget.FrameLayout} or {@link RelativeLayout}
     */
    public void show(ViewGroup parent) {
        if (mState == STATE_SHOWING || mState == STATE_DISMISSING)
            return;

        if (getParent() != parent) {
            if (getParent() != null)
                ((ViewGroup) getParent()).removeView(this);

            parent.addView(this);
        }

        show();
    }

    /**
     * Show this SnackBar.
     * Make sure it already attached to a parent view or this method will do nothing.
     */
    public void show() {
        ViewGroup parent = (ViewGroup) getParent();
        if (parent == null || mState == STATE_SHOWING || mState == STATE_DISMISSING)
            return;

        if (parent instanceof FrameLayout) {
            LayoutParams params = (LayoutParams) getLayoutParams();

            params.height = WRAP_CONTENT;
            params.width = MATCH_PARENT;
            params.gravity = Gravity.START | Gravity.BOTTOM;
        } else if (parent instanceof RelativeLayout) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();

            params.height = WRAP_CONTENT;
            params.width = MATCH_PARENT;
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
        }

        if (mInAnimation != null && mState != STATE_SHOWN) {
            mInAnimation.cancel();
            mInAnimation.reset();
            mInAnimation.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    setState(STATE_SHOWING);
                    setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    setState(STATE_SHOWN);
                    startTimer();
                }
            });
            clearAnimation();
            startAnimation(mInAnimation);
        } else {
            setVisibility(View.VISIBLE);
            setState(STATE_SHOWN);
            startTimer();
        }
    }


    private void startTimer() {
        removeCallbacks(mDismissRunnable);
        if (mDuration > 0)
            postDelayed(mDismissRunnable, mDuration);
    }

    /**
     * Dismiss this SnackBar. It must be in {@link #STATE_SHOWN} to be dismissed.
     */
    public void dismiss() {
        if (mState != STATE_SHOWN)
            return;

        removeCallbacks(mDismissRunnable);

        if (mOutAnimation != null) {
            mOutAnimation.cancel();
            mOutAnimation.reset();
            mOutAnimation.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    setState(STATE_DISMISSING);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mRemoveOnDismiss && getParent() != null && getParent() instanceof ViewGroup)
                        ((ViewGroup) getParent()).removeView(UUSnackBar.this);

                    setState(STATE_DISMISSED);
                    setVisibility(View.GONE);
                }
            });
            clearAnimation();
            startAnimation(mOutAnimation);
        } else {
            if (mRemoveOnDismiss && getParent() != null && getParent() instanceof ViewGroup)
                ((ViewGroup) getParent()).removeView(this);

            setState(STATE_DISMISSED);
            setVisibility(View.GONE);
        }

    }


}
