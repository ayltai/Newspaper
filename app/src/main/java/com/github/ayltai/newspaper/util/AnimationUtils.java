package com.github.ayltai.newspaper.util;

import javax.annotation.Nonnull;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.ViewAnimationUtils;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import flow.Direction;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AnimationUtils {
    private static final String SCALE_Y = "scaleY";
    private static final String ALPHA   = "alpha";

    public boolean areAnimatorsEnabled() {
        return !DevUtils.isRunningInstrumentedTest() && (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || ValueAnimator.areAnimatorsEnabled());
    }

    @Nonnull
    @NonNull
    public Animator createDefaultAnimator(@Nonnull @NonNull @lombok.NonNull final View view, @Nonnull @NonNull @lombok.NonNull final Direction direction, @Nullable final Point position, @Nullable final Runnable onStart, @Nullable final Runnable onEnd) {
        final Animator animator;
        final Point    screenSize   = DeviceUtils.getScreenSize(view.getContext());
        final int      widthRadius  = screenSize.x / 2;
        final int      heightRadius = screenSize.y / 2;
        final int      centerX      = position == null ? widthRadius  : position.x;
        final int      centerY      = position == null ? heightRadius : position.y;
        final int      radiusX      = centerX < widthRadius  ? screenSize.x - centerX : centerX;
        final int      radiusY      = centerX < heightRadius ? screenSize.y - centerX : centerX;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final float radius = (float)Math.hypot(radiusX, radiusY);
            animator = ViewAnimationUtils.createCircularReveal(view, centerX, centerY, direction == Direction.FORWARD ? 0 : radius, direction == Direction.FORWARD ? radius : 0);
        } else {
            final AnimatorSet animators = new AnimatorSet();

            if (direction == Direction.FORWARD) {
                view.setScaleY(0);
                view.setAlpha(0);

                animators.play(ObjectAnimator.ofFloat(view, AnimationUtils.SCALE_Y, 1)).with(ObjectAnimator.ofFloat(view, AnimationUtils.ALPHA, 1));
            } else {
                animators.play(ObjectAnimator.ofFloat(view, AnimationUtils.SCALE_Y, 0)).with(ObjectAnimator.ofFloat(view, AnimationUtils.ALPHA, 0));
            }

            animator = animators;
        }

        animator.setDuration(AnimationUtils.getAnimationDuration(view.getContext(), android.R.integer.config_mediumAnimTime));
        animator.setInterpolator(android.view.animation.AnimationUtils.loadInterpolator(view.getContext(), direction == Direction.FORWARD ? android.R.interpolator.decelerate_cubic : android.R.interpolator.accelerate_quint));

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(final Animator animator) {
                if (onStart != null) onStart.run();
            }

            @Override
            public void onAnimationEnd(final Animator animator) {
                if (onEnd != null) onEnd.run();
            }

            @Override
            public void onAnimationCancel(final Animator animator) {
                if (onEnd != null) onEnd.run();
            }

            @Override
            public void onAnimationRepeat(final Animator animator) {
            }
        });

        return animator;
    }

    private int getAnimationDuration(@Nonnull @NonNull @lombok.NonNull final Context context, @IntegerRes final int durationId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) return (int)(context.getResources().getInteger(durationId) * Settings.Global.getFloat(context.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1f));

        return context.getResources().getInteger(durationId);
    }
}
