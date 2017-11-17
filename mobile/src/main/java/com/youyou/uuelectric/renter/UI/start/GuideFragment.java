package com.youyou.uuelectric.renter.UI.start;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.main.MainActivity;

/**
 *  引导页Fragment
 */
public class GuideFragment extends Fragment {
    /**
     * KEY：布局id
     */
    public static String KEY_LAYOUT_ID = "key_layout_id";
    /**
     * KEY:是否是最后一页
     */
    public static String KEY_IS_LAST = "key_is_last";


    int layoutId;
    boolean isLast = false;
    Activity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        layoutId = bundle.getInt(KEY_LAYOUT_ID, R.layout.gudie01_fragment_layout);
        isLast = bundle.getBoolean(KEY_IS_LAST, false);
        View rootView = inflater.inflate(layoutId, null);
        if (isLast) {
            rootView.setOnTouchListener(lastOnTouchListener);
        }
        return rootView;
    }

    float downX;

    View.OnTouchListener lastOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    float moveX = event.getX();
                    float distance = moveX - downX;
                    if (distance < 0 && Math.abs(distance) > 20) {

                        Intent mainIntent = new Intent(activity, MainActivity.class);
                        mainIntent.putExtra("goto", StartActivity.userStatus);
                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(mainIntent);
                        activity.finish();
                        return true;
                    }
                    break;
            }
            return true;
        }
    };
}
