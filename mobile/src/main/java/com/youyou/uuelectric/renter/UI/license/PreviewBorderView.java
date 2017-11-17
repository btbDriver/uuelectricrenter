package com.youyou.uuelectric.renter.UI.license;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.Utils.DisplayUtil;


/**
 * Created by chudaijiang on 2016/5/13.
 */
public class PreviewBorderView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private int mScreenH;
    private int mScreenW;
    private Canvas mCanvas;
    private Paint mPaint;
    private Paint mPaintLine;
    private SurfaceHolder mHolder;
    private Thread mThread;
    private static final String DEFAULT_TIPS_TEXT = "请将驾照正副页上下排布放入框内拍照";
    private static final int DEFAULT_TIPS_TEXT_SIZE = 16;
    private static final int DEFAULT_TIPS_TEXT_COLOR = Color.WHITE;
    /**
     * 自定义属性
     */
    private float tipTextSize;
    private float tipOtherTextSize;
    private int tipTextColor;
    private String tipText;
    private String tipTopText;
    private String tipBottomText;
    private int lineLength;

    public PreviewBorderView(Context context) {
        this(context, null);
    }

    public PreviewBorderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreviewBorderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        init();
    }

    /**
     * 初始化自定义属性
     *
     * @param context Context
     * @param attrs   AttributeSet
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        lineLength = DisplayUtil.dip2px(context,getResources().getDimension(R.dimen.s2));
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PreviewBorderView);
        try {
            tipTextSize = a.getDimension(R.styleable.PreviewBorderView_tipTextSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TIPS_TEXT_SIZE, getResources().getDisplayMetrics()));
            tipTextColor = a.getColor(R.styleable.PreviewBorderView_tipTextColor, DEFAULT_TIPS_TEXT_COLOR);
            tipText = a.getString(R.styleable.PreviewBorderView_tipMidText);
            if (tipText == null) {
                tipText = DEFAULT_TIPS_TEXT;
            }
            tipTopText = a.getString(R.styleable.PreviewBorderView_tipTopText);
            tipBottomText = a.getString(R.styleable.PreviewBorderView_tipBottomText);
            tipOtherTextSize = a.getDimension(R.styleable.PreviewBorderView_tipOtherTextSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TIPS_TEXT_SIZE, getResources().getDisplayMetrics()));
        } finally {
            a.recycle();
        }


    }

    /**
     * 初始化绘图变量
     */
    private void init() {
        this.mHolder = getHolder();
        this.mHolder.addCallback(this);
        this.mHolder.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(Color.WHITE);
        this.mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        this.mPaintLine = new Paint();
        this.mPaintLine.setColor(tipTextColor);
        this.mPaintLine.setStrokeWidth(3.0F);
        setKeepScreenOn(true);
    }

    /**
     * 绘制取景框
     */
    private void draw() {
        try {
            this.mCanvas = this.mHolder.lockCanvas();
            this.mCanvas.drawARGB(100, 0, 0, 0);

            float left = this.mScreenW / 16;
            float top = this.mScreenH / 2 - this.mScreenW * 7 / 12;
            float right = this.mScreenW * 15 / 16;
            float bottom = this.mScreenH / 2 + this.mScreenW * 7 / 12;



            this.mCanvas.drawRect(new RectF(left, top, right, bottom), this.mPaint);
            this.mCanvas.drawLine(left-3, top-3, left-3, top + lineLength, this.mPaintLine);
            this.mCanvas.drawLine(left-4, top-3, left + lineLength, top-3, this.mPaintLine);
            this.mCanvas.drawLine(right+3, top-3, right+3, top + lineLength, this.mPaintLine);
            this.mCanvas.drawLine(right+5, top-3, right - lineLength, top-3, this.mPaintLine);
            this.mCanvas.drawLine(left-3, bottom+3, left-3, bottom - lineLength, this.mPaintLine);
            this.mCanvas.drawLine(left-4, bottom+3, left + lineLength, bottom+3, this.mPaintLine);
            this.mCanvas.drawLine(right+3, bottom+3, right+3, bottom - lineLength, this.mPaintLine);
            this.mCanvas.drawLine(right+5, bottom+3, right - lineLength, bottom+3, this.mPaintLine);

            mPaintLine.setTextSize(tipTextSize);
            mPaintLine.setAntiAlias(true);
            mPaintLine.setDither(true);
            float length = mPaintLine.measureText(tipText);
            this.mCanvas.drawText(tipText, this.mScreenW / 2 - length / 2, this.mScreenH / 2, mPaintLine);
            //切换字体大小
            mPaintLine.setTextSize(tipOtherTextSize);
            mPaintLine.setColor(Color.parseColor("#88ffffff"));
            length = mPaintLine.measureText(tipTopText);
            this.mCanvas.drawText(tipTopText, this.mScreenW / 2 - length / 2, (this.mScreenH / 2-top)/2+top, mPaintLine);
            length = mPaintLine.measureText(tipBottomText);
            this.mCanvas.drawText(tipBottomText, this.mScreenW / 2 - length / 2, this.mScreenH / 2+(this.mScreenH / 2-top)/2, mPaintLine);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (this.mCanvas != null) {
                this.mHolder.unlockCanvasAndPost(this.mCanvas);
            }
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //获得宽高，开启子线程绘图
        this.mScreenW = getWidth();
        this.mScreenH = getHeight();
        this.mThread = new Thread(this);
        this.mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //停止线程
        try {
            mThread.interrupt();
            mThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        //子线程绘图
        draw();
    }
}