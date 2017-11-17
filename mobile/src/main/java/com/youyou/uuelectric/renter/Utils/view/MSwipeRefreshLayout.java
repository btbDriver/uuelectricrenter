package com.youyou.uuelectric.renter.Utils.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by liuchao on 2015/9/17.
 * 自定义组件组要用于控制swipeRefreshLayout中不止包含AbListView的情况
 */
public class MSwipeRefreshLayout extends SwipeRefreshLayout{

    RecyclerView recyclerView = null;

    public MSwipeRefreshLayout(Context context, RecyclerView recyclerView) {
        super(context);
        this.recyclerView = recyclerView;
    }

    public MSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();

        /*L.d("scroll1Y: " + recyclerView.getScrollY() + "  scroll2Y: " +
                linearLayoutManager.findViewByPosition(linearLayoutManager.findFirstVisibleItemPosition()).getScrollY() +
                "   topY: " + linearLayoutManager.findViewByPosition(linearLayoutManager.findFirstVisibleItemPosition()).getTop());*/
        // 若当前列表位置不是在起始位置，则由recyclerView处理滑动事件
        if (linearLayoutManager.findFirstVisibleItemPosition() != 0) {
            return false;
        } else {
            // 并且当前列表并未滑动到顶部的时候还是由recyclerView处理滑动事件（滑动到顶部的时候top为0）
            if (linearLayoutManager.findViewByPosition(linearLayoutManager.findFirstVisibleItemPosition()).getTop() < 0) {
                return false;
            }
            return super.onInterceptTouchEvent(ev);
        }
    }
}
