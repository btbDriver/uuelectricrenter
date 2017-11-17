package com.youyou.uuelectric.renter.UI.order;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.youyou.uuelectric.renter.R;

/**
 * Created by liuchao on 2015/9/8.
 */
public class RecyclerItemDivider extends RecyclerView.ItemDecoration {

    private Drawable mDivider;

    public RecyclerItemDivider(Context context) {
        mDivider = context.getResources().getDrawable(R.drawable.favour_item_driver);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent) {
        drawVertical(c, parent);
    }


    public void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            android.support.v7.widget.RecyclerView v = new android.support.v7.widget.RecyclerView(parent.getContext());
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
        outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
    }
}
