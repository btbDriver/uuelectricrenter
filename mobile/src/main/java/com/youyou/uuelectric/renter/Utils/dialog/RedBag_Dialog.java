package com.youyou.uuelectric.renter.Utils.dialog;


import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by leetylty on 2016/4/15.
 */
public class RedBag_Dialog extends android.app.Dialog{
    private static int default_width = 308; // 默认宽度
    private static int default_height = 406;// 默认高度

    public RedBag_Dialog(Context context, View layout,int style) {


        this(context, default_width, default_height, layout, style);

    }




    public RedBag_Dialog(Context context , int width, int height, View layout,  int style) {
        super(context,style);
        setContentView(layout);
        float density = getDensity(context);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = (int) (width * density);
        params.height = (int) (height * density);
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }




    private float getDensity(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.density;
    }
}
