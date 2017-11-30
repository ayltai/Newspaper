package com.github.ayltai.newspaper.util;

import java.util.Arrays;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.AnimRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.fujiyuu75.sequent.Sequent;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;

import io.supercharge.shimmerlayout.ShimmerLayout;

public final class Animations {
    private Animations() {
    }

    @NonNull
    public static Animation getAnimation(@NonNull final Context context, @AnimRes final int animationId, @IntegerRes final int durationId) {
        final Animation animation = AnimationUtils.loadAnimation(context, animationId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            animation.setDuration((int)(context.getResources().getInteger(durationId) * Settings.Global.getFloat(context.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1f)));
        } else {
            animation.setDuration(context.getResources().getInteger(durationId));
        }

        return animation;
    }

    @NonNull
    public static Animation getAnimation(@NonNull final Context context, @AnimRes final int animationId, @IntegerRes final int durationId, @Nullable final Runnable onStart, @Nullable final Runnable onEnd) {
        final Animation animation = Animations.getAnimation(context, animationId, durationId);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(@NonNull final Animation animation) {
                if (onStart != null) onStart.run();
            }

            @Override
            public void onAnimationEnd(@NonNull final Animation animation) {
                if (onEnd != null) onEnd.run();
            }

            @Override
            public void onAnimationRepeat(@NonNull final Animation animation) {
            }
        });

        return animation;
    }

    @NonNull
    public static Iterable<Animator> createDefaultAnimators(@NonNull final View view) {
        return Arrays.asList(
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f),
            ObjectAnimator.ofFloat(view, "translationX", -view.getMeasuredWidth(), 0f)
        );
    }

    public static void animateViewGroup(@NonNull final ViewGroup container) {
        Sequent.origin(container)
            .delay(container.getResources().getInteger(android.R.integer.config_mediumAnimTime))
            .offset(Constants.ANIMATION_OFFSET)
            .duration(container.getResources().getInteger(android.R.integer.config_shortAnimTime))
            .anim(container.getContext(), R.anim.fade_in_up)
            .start();
    }

    public static void startShimmerAnimation(@NonNull final View view) {
        if (view instanceof ViewGroup) {
            final ViewGroup parent = (ViewGroup)view;

            for (int i = 0; i < parent.getChildCount(); i++) Animations.startShimmerAnimation(parent.getChildAt(i));

            if (view instanceof ShimmerLayout) ((ShimmerLayout)view).startShimmerAnimation();
        }
    }

    public static boolean isEnabled() {
        return !DevUtils.isRunningInstrumentedTest() && (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || ValueAnimator.areAnimatorsEnabled());
    }
}
