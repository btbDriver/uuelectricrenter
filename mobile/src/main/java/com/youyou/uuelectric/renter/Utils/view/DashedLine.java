package com.youyou.uuelectric.renter.Utils.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;

import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.Utils.DisplayUtil;

/**
 * User: qing
 * Date: 2015/9/12 14:02
 * Desc: 虚线View
 */
public class DashedLine extends View {
    private Paint paint = null;
    private Path path = null;
    private PathEffect pe = null;

    public DashedLine(Context paramContext) {
        this(paramContext, null);
    }

    public DashedLine(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        TypedArray a = paramContext.obtainStyledAttributes(paramAttributeSet, R.styleable.dashedline);
        int lineColor = a.getColor(R.styleable.dashedline_lineColor, getResources().getColor(R.color.c5));
        a.recycle();
        paint = new Paint();
        path = new Path();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(lineColor);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(DisplayUtil.dip2px(getContext(), 2.0F));
        float[] arrayOfFloat = new float[4];
        arrayOfFloat[0] = DisplayUtil.dip2px(getContext(), 2.0F);
        arrayOfFloat[1] = DisplayUtil.dip2px(getContext(), 2.0F);
        arrayOfFloat[2] = DisplayUtil.dip2px(getContext(), 2.0F);
        arrayOfFloat[3] = DisplayUtil.dip2px(getContext(), 2.0F);
        pe = new DashPathEffect(arrayOfFloat, DisplayUtil.dip2px(getContext(), 1.0F));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        path.moveTo(0.0F, 0.0F);
        path.lineTo(getMeasuredWidth(), 0.0F);
        paint.setPathEffect(pe);
        canvas.drawPath(path, paint);
    }

}
