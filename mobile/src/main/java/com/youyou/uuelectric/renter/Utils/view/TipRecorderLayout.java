package com.youyou.uuelectric.renter.Utils.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.youyou.uuelectric.renter.R;

/**
 * User: qing
 * Date: 2015/9/12 12:06
 * Desc: 行程记录item布局View
 */
public class TipRecorderLayout extends RelativeLayout {

    /**
     * 起始类型
     */
    public static final int START_TYPE = 0;
    /**
     * 中间类型
     */
    public static final int MIDDLE_TYPE = 1;
    /**
     * 结束类型
     */
    public static final int END_TYPE = 2;

    /**
     * 线
     */
    private View line;
    /**
     * 左侧圈圈Icon
     */
    private ImageView icon;
    /**
     * 时间文本
     */
    private TextView timeText;
    /**
     * 描述文案
     */
    private TextView infoText;


    public TipRecorderLayout(Context context) {
        super(context);
        init();
    }

    public TipRecorderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TipRecorderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.tip_recorder_layout, null);
        line = rootView.findViewById(R.id.line);
        icon = (ImageView) rootView.findViewById(R.id.iv_left_icon);
        timeText = (TextView) rootView.findViewById(R.id.tv_time);
        infoText = (TextView) rootView.findViewById(R.id.tv_info);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.tip_item_height));
        addView(rootView, params);
    }

    /**
     * 设置是起始、中间、或是结束时的布局显示
     *
     * @param type
     */
    public void setTypeAndInfo(int type, String time, String info) {

        switch (type) {
            case START_TYPE:
                line.setVisibility(INVISIBLE);
                icon.setImageResource(R.mipmap.ic_greencricle_payment);
                break;
            case MIDDLE_TYPE:
                line.setVisibility(VISIBLE);
                icon.setImageResource(R.mipmap.ic_greycricle_payment);
                break;
            case END_TYPE:
                line.setVisibility(VISIBLE);
                icon.setImageResource(R.mipmap.ic_redcricle_payment);
                break;
        }

        timeText.setText(time);
        infoText.setText(info);

    }
}
