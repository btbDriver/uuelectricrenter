package com.youyou.uuelectric.renter.Utils.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.youyou.uuelectric.renter.Utils.Support.Config;

/**
 * 对双击操作进行过滤的布局
 */
public class DoubleClickFilterLayout extends RelativeLayout {
    public DoubleClickFilterLayout(Context context) {
        super(context);
    }

    public DoubleClickFilterLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DoubleClickFilterLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (Config.isFastDoubleClick()) {
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
