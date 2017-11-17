package com.youyou.uuelectric.renter.Utils.animation;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

/**
 * Created by taurusxi on 14-9-1.
 */
public class AnimationUtils {

    public static void makeProgressDismiss(final View frameLayout, final View linearLayout) {
//        linearLayout.setVisibility(View.VISIBLE);
//        Animation animation = new AlphaAnimation(1.0f, 0.0f);
//        animation.setInterpolator(new AccelerateDecelerateInterpolator());
//        animation.setDuration(400);
//        animation.setAnimationListener(new EmptyAnimationListener() {
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                frameLayout.setVisibility(View.GONE);
//                frameLayout.clearAnimation();
//            }
//        });
//        frameLayout.startAnimation(animation);
        frameLayout.setVisibility(View.GONE);
        linearLayout.setVisibility(View.VISIBLE);
    }

    public static void rotateIndicatingArrow(final View view, boolean direction) {
        RotateAnimation rotateAnimation;
        if (direction) {
            rotateAnimation = new RotateAnimation(0, 90.0f, view.getWidth() / 2, view.getHeight() / 2);
        } else {
            rotateAnimation = new RotateAnimation(
                    90.0f, 0, view.getWidth() / 2, view.getHeight() / 2);
        }
        rotateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        rotateAnimation.setDuration(400);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                view.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(rotateAnimation);


    }

    public static void rotateIndicatingArrow180(final View view, boolean direction) {
        RotateAnimation rotateAnimation;
        if (direction) {
            rotateAnimation = new RotateAnimation(0, 180.0f, view.getWidth() / 2, view.getHeight() / 2);
        } else {
            rotateAnimation = new RotateAnimation(
                    180.0f, 0, view.getWidth() / 2, view.getHeight() / 2);
        }
        rotateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        rotateAnimation.setDuration(400);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                view.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(rotateAnimation);


    }
}
