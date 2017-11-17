package com.youyou.uuelectric.renter.UI.main.rentcar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.Utils.view.RippleView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.DayArrayAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import kankan.wheel.widget.adapters.SpeedAdapter;

/**
 * User: qing
 * Date: 2015/9/10 20:50
 * Desc: 时间选择
 */
public class SelectTimeFragment extends Fragment {

    @InjectView(R.id.day)
    WheelView mDay;
    @InjectView(R.id.hour)
    WheelView mHour;
    @InjectView(R.id.mins)
    WheelView mMins;
    @InjectView(R.id.ll_root)
    LinearLayout mLlRoot;
    @InjectView(R.id.tv_tip)
    TextView mTvTip;
    @InjectView(R.id.btn_cancel)
    RippleView btn_cancel;
    @InjectView(R.id.btn_sure)
    RippleView btn_sure;

    private static final int MSGWHAT = 10;


    private CostAssessActivity costAssessActivity;

    /**
     * 最大时间可选时间：2天
     */
    private int maxGetCarTime = 2;

    /**
     * View是否被销毁
     */
    private boolean isDestroyView = false;
    /**
     * 分钟的时间间隔
     */
    private int minStep = 30;
    /**
     * 当前时间控件所选择的时间:字符串
     */
    private String selectTimeStr;
    /**
     * 当前时间：时间格式
     */
    private Date selectTiem;


    private Handler timeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mTvTip.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        costAssessActivity = (CostAssessActivity) getActivity();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_select_time, null);
        ButterKnife.inject(this, rootView);

        isDestroyView = false;

        btn_cancel.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                hideClick();
            }
        });

        btn_sure.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                sureClick();
            }
        });

        initTimePick();

        mLlRoot.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTimePick(System.currentTimeMillis());
            }
        }, 500);

        return rootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        isDestroyView = true;
        timeHandler.removeMessages(MSGWHAT);
    }


    @OnClick({R.id.out_area})
    public void hideClick() {
        costAssessActivity.hideFilterContainer();
    }

    public void sureClick() {
        long currentTime = System.currentTimeMillis();
        long st = selectTiem.getTime();
        int hours = maxGetCarTime * 24 * 60 * 60 * 1000;
        long time = st - currentTime;
        if (time < 0) {
            mTvTip.setVisibility(View.VISIBLE);
            mTvTip.setText(getResources().getString(R.string.time_tip2));
            timeHandler.sendEmptyMessageDelayed(MSGWHAT, 2000);
            return;
        }
        if (time > hours) {
            mTvTip.setVisibility(View.VISIBLE);
            mTvTip.setText(getResources().getString(R.string.time_tip1));
            timeHandler.sendEmptyMessageDelayed(MSGWHAT, 2000);
            return;
        }
        costAssessActivity.updateSelectTime(selectTimeStr, selectTiem);
        costAssessActivity.hideFilterContainer();
    }

    /**
     * 初始化时间选择器
     */
    private void initTimePick() {

        final Date currentTime = new Date(System.currentTimeMillis());
        final Calendar dayCalendar = Calendar.getInstance();
        dayCalendar.setTime(currentTime);

        // 设置天对应的数据Adapter
        DayArrayAdapter dayArrayAdapter = new DayArrayAdapter(costAssessActivity, dayCalendar);

        dayArrayAdapter.setDaysCount(maxGetCarTime);

        mDay.setViewAdapter(dayArrayAdapter);
        mDay.setVisibleItems(7);
        // 设置小时对应的数据Adapter
        mHour.setViewAdapter(new NumericWheelAdapter(costAssessActivity, 0, 23));
        mHour.setVisibleItems(7);
        // 设置分钟对应的数据Adapter
        mMins.setViewAdapter(new SpeedAdapter(costAssessActivity, 30, minStep));
        mMins.setVisibleItems(7);

        /**
         * 设置滚动监听
         */
        OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {
            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                // 如果Fragment的View被销毁了，就不执行该回调方法
                if (isDestroyView) {
                    return;
                }
                int daysRoll = mDay.getCurrentItem();
                int hoursRoll = mHour.getCurrentItem();
                int minsRoll = mMins.getCurrentItem();
                Calendar selectCalendar = Calendar.getInstance();
                selectCalendar.setTime(currentTime);
                selectCalendar.add(Calendar.DATE, daysRoll);
                selectCalendar.set(Calendar.HOUR_OF_DAY, hoursRoll);
                selectCalendar.set(Calendar.MINUTE, minsRoll * minStep);
                selectCalendar.set(Calendar.SECOND, 0);

                selectTiem = selectCalendar.getTime();
                selectTimeStr = "";
                selectTimeStr += DayArrayAdapter.formatTime(selectCalendar) + " ";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                selectTimeStr += simpleDateFormat.format(selectTiem);
            }
        };

        mHour.addScrollingListener(scrollListener);
        mMins.addScrollingListener(scrollListener);
        mDay.addScrollingListener(scrollListener);

    }

    /**
     * 根据传入的时间，更新时间选择器的各项数据
     *
     * @param time
     */
    private void updateTimePick(long time) {
        Date date = new Date(time);
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int minsInt = calendar.get(Calendar.MINUTE);
        int yu = minsInt % minStep;
        if (yu != 0) {
            calendar.add(Calendar.MINUTE, minStep - yu);
        }
        final Calendar nowCalendar = Calendar.getInstance();
        long currentTime = System.currentTimeMillis();
        final Date nowDate = new Date(currentTime);
        nowCalendar.setTime(nowDate);
        int rollDay = 0;
        while (nowCalendar.before(calendar)) {
            int nowDay = nowCalendar.get(Calendar.DATE);
            int day = calendar.get(Calendar.DATE);
            if (day == nowDay) {
                int nowMonth = nowCalendar.get(Calendar.MONTH);
                int month = calendar.get(Calendar.MONTH);
                if (nowMonth == month) {
                    break;
                }
            }
            nowCalendar.add(Calendar.DATE, 1);
            rollDay++;
        }

        if (!isDestroyView) {
            mDay.setCurrentItem(rollDay, true);
            mHour.setCurrentItem(calendar.get(Calendar.HOUR_OF_DAY), true);
            mMins.setCurrentItem(calendar.get(Calendar.MINUTE) / minStep, true);
        }
    }
}
