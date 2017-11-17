package com.youyou.uuelectric.renter.UI.start;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flyco.pageindicator.indicator.FlycoPageIndicaor;
import com.nineoldandroids.view.ViewHelper;
import com.youyou.uuelectric.renter.Network.user.SPConstant;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.main.MainActivity;

public class GuideActivity extends AppCompatActivity {

    private static final int ITEM_COUNT = 3;
    /**
     * 当前引导图的版本号:当引导图调整后，需要修改此版本号使其生效
     */
    public static final int CURRENT_VERSION = 1;

    ViewPager mViewPager;
    FlycoPageIndicaor mIndicator;
    TextView mTvJump;
    ImageView mIvGuide;
    Context mContext;

    View.OnClickListener jumpClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent mainIntent = new Intent(mContext, MainActivity.class);
            mainIntent.putExtra("goto", StartActivity.userStatus);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(mainIntent);
            finish();
        }
    };

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(final int position) {
            alphaAnim(1.0f, 0.1f, 400, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    alphaAnim(0.1f, 1.0f, 400, null);
                    if (position == 0) {
                        mIvGuide.setImageResource(R.mipmap.guide_image1);
                        mTvJump.setVisibility(View.VISIBLE);
                    } else if (position == 1) {
                        mIvGuide.setImageResource(R.mipmap.guide_image2);
                        mTvJump.setVisibility(View.VISIBLE);
                    } else if (position == 2) {
                        mIvGuide.setImageResource(R.mipmap.guide_image3);
                        mTvJump.setVisibility(View.GONE);
                    }
                }
            });
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    /**
     * 透明度动画，根据传入的开始值和结束值来开启动画
     *
     * @param start
     * @param end
     * @param animatorListener
     */
    private void alphaAnim(float start, float end, int duration, Animator.AnimatorListener animatorListener) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(start, end);
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                mIvGuide.setAlpha(alpha);
            }
        });
        if (animatorListener != null) {
            valueAnimator.addListener(animatorListener);
        }
        valueAnimator.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        mContext = this;
        // 引导页设置不可横竖屏切换
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mIndicator = (FlycoPageIndicaor) findViewById(R.id.indicator);
        mTvJump = (TextView) findViewById(R.id.tv_jump);
        mTvJump.setOnClickListener(jumpClickListener);
        mIvGuide = (ImageView) findViewById(R.id.iv_guide);

        GuidePagerAdapter adapter = new GuidePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.setPageTransformer(true, new GuidePageTransformer());
        mViewPager.addOnPageChangeListener(onPageChangeListener);
        mIndicator.setViewPager(mViewPager);

    }

    class GuidePagerAdapter extends FragmentPagerAdapter {

        public GuidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new GuideFragment();
            Bundle bundle = new Bundle();
            if (position == 0) {
                bundle.putInt(GuideFragment.KEY_LAYOUT_ID, R.layout.gudie01_fragment_layout);
            } else if (position == 1) {
                bundle.putInt(GuideFragment.KEY_LAYOUT_ID, R.layout.gudie02_fragment_layout);
            } else if (position == 2) {
                bundle.putInt(GuideFragment.KEY_LAYOUT_ID, R.layout.gudie03_fragment_layout);
                bundle.putBoolean(GuideFragment.KEY_IS_LAST, true);
            }
            fragment.setArguments(bundle);

            return fragment;
        }

        @Override
        public int getCount() {
            return ITEM_COUNT;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    class GuidePageTransformer implements ViewPager.PageTransformer {
        /**
         * 最小缩放比例
         */
        float MIN_SCALE = 0.4f;

        @Override
        public void transformPage(View view, float position) {
            // 底部描述
            TextView desc = (TextView) view.findViewById(R.id.tv_bottom_desc);
            // 描述上面的介绍
            LinearLayout introduce = (LinearLayout) view.findViewById(R.id.ll_introduce);
            int pageWidth = view.getWidth();
            if (position < -1) {
                view.setAlpha(0);
            } else if (position <= 0 || position <= 1) {
                view.setAlpha(1);
                if (desc != null) {
                    ViewHelper.setTranslationX(desc, pageWidth * position);
                }
                if (introduce != null) {
                    float scale = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
                    ViewHelper.setScaleX(introduce, scale);
                    ViewHelper.setScaleY(introduce, scale);
                }
            } else {
                view.setAlpha(0);
            }
        }
    }

    /**
     * 是否已经显示过引导图
     *
     * @param context
     * @return
     */
    public static boolean isShowedGuide(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SPConstant.SP_NAME_GUIDE_VERSION, Context.MODE_PRIVATE);
        int version = sp.getInt(SPConstant.SP_KEY_GUIDE_VERSION, 0);
        if (version == CURRENT_VERSION) {
            return true;
        } else {
            sp.edit().putInt(SPConstant.SP_KEY_GUIDE_VERSION, CURRENT_VERSION).commit();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
