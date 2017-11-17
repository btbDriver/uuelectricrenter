package com.youyou.uuelectric.renter.Utils.recycler;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by dcq on 2016/1/20 0020.
 * <p/>
 * 设置RecyclerView中的item的间隔
 */
public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int space;

    public SpaceItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (parent.getChildLayoutPosition(view) != 0) {
            outRect.top = space;
            outRect.right = space;
            outRect.bottom = space;
            outRect.left = space;
        }
    }
}
