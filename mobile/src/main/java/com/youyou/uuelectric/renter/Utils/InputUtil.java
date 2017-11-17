package com.youyou.uuelectric.renter.Utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by liuchao on 2015/9/18.
 */
public class InputUtil {

    /**
     * 关闭软键盘
     *
     * @param context
     */
    public static void closeInput(Context context) {
        if (context == null)
            return;
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
