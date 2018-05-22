package com.github.ayltai.newspaper.util;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.AnimRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;

import flow.Direction;
import io.reactivex.Flowable;
import io.supercharge.shimmerlayout.ShimmerLayout;

public final class Animations {
    private static final String SCALE_Y = "scaleY";
    private static final String ALPHA   = "alpha";

    public static final class Builder {
        //region Variables

        private final View container;

        private long      delay;
        private long      offset;
        private Animation animation;

        //endregion

        public Builder(@NonNull final View container) {
            this.container = container;
        }

        //region Properties

        @NonNull
        public Animations.Builder delay(@IntegerRes final int id) {
            return this.delay((long)this.container.getResources().getInteger(id));
        }

        @NonNull
        public Animations.Builder delay(final long delay) {
            this.delay = Math.max(delay, 0);

            return this;
        }

        @NonNull
        public Animations.Builder offset(@IntegerRes final int id) {
            return this.offset((long)this.container.getResources().getInteger(id));
        }

        @NonNull
        public Animations.Builder offset(final long offset) {
            this.offset = Math.max(offset, 0);

            return this;
        }

        @NonNull
        public Animations.Builder animate(@AnimRes final int id) {
            return this.animate(AnimationUtils.loadAnimation(this.container.getContext(), id));
        }

        @NonNull
        public Animations.Builder animate(@NonNull final Animation animation) {
            this.animation = animation;

            return this;
        }

        //endregion

        //region Methods

        public void start(final boolean recursive) {
            this.hide(this.container);

            this.container.postDelayed(() -> {
                if (recursive) {
                    this.start(this.container, new ArrayDeque<>());
                } else {
                    this.container.setVisibility(View.VISIBLE);
                    this.container.startAnimation(this.animation);
                }
            }, this.delay);
        }

        private void start(@NonNull final View container, @NonNull final Queue<View> queue) {
            if (container instanceof ViewGroup) {
                final ViewGroup parent = (ViewGroup)container;

                for (int i = 0; i < parent.getChildCount(); i++) this.start(parent.getChildAt(i), queue);
            } else {
                if (container == this.container) {
                    container.setVisibility(View.VISIBLE);
                    container.startAnimation(this.animation);
                } else {
                    queue.offer(container);
                }
            }

            if (!queue.isEmpty()) {
                Flowable.interval(this.offset, TimeUnit.MILLISECONDS)
                    .compose(RxUtils.applyFlowableBackgroundToMainSchedulers())
                    .take(queue.size())
                    .subscribe(time -> {
                        final View view = queue.poll();

                        if (view != null) {
                            view.setVisibility(View.VISIBLE);
                            view.startAnimation(this.animation);
                        }
                    });
            }
        }

        private void hide(final View container) {
            if (container instanceof ViewGroup) {
                final ViewGroup parent = (ViewGroup)container;

                for (int i = 0; i < parent.getChildCount(); i++) this.hide(parent.getChildAt(i));
            } else {
                container.setVisibility(View.INVISIBLE);
            }
        }

        //endregion
    }

    private Animations() {
    }

    @NonNull
    public static Animation getAnimation(@NonNull final Context context, @AnimRes final int animationId, @IntegerRes final int durationId) {
        final Animation animation = AnimationUtils.loadAnimation(context, animationId);
        animation.setDuration(Animations.getAnimationDuration(context, durationId));
        return animation;
    }

    @NonNull
    public static Animator createDefaultAnimator(@NonNull final View view, @NonNull final Direction direction, @Nullable final Point location, @Nullable final Runnable onStart, @Nullable final Runnable onEnd) {
        final Animator animator;
        final Point    screenSize   = DeviceUtils.getScreenSize(view.getContext());
        final int      widthRadius  = screenSize.x / 2;
        final int      heightRadius = screenSize.y / 2;
        final int      centerX      = location == null ? widthRadius  : location.x;
        final int      centerY      = location == null ? heightRadius : location.y;
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

                animators.play(ObjectAnimator.ofFloat(view, Animations.SCALE_Y, 1))
                    .with(ObjectAnimator.ofFloat(view, Animations.ALPHA, 1));
            } else {
                animators.play(ObjectAnimator.ofFloat(view, Animations.SCALE_Y, 0))
                    .with(ObjectAnimator.ofFloat(view, Animations.ALPHA, 0));
            }

            animator = animators;
        }

        animator.setDuration(Animations.getAnimationDuration(view.getContext(), android.R.integer.config_mediumAnimTime));
        animator.setInterpolator(AnimationUtils.loadInterpolator(view.getContext(), direction == Direction.FORWARD ? android.R.interpolator.decelerate_cubic : android.R.interpolator.accelerate_quint));

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

    @NonNull
    public static Iterable<Animator> createDefaultAnimators(@NonNull final View view) {
        return Arrays.asList(
            ObjectAnimator.ofFloat(view, Animations.ALPHA, 0f, 1f),
            ObjectAnimator.ofFloat(view, "translationX", -view.getMeasuredWidth(), 0f)
        );
    }

    public static void animateViewGroup(@NonNull final ViewGroup container) {
        new Animations.Builder(container)
            .delay(Constants.ANIMATION_DELAY)
            .offset(Constants.ANIMATION_OFFSET)
            .animate(R.anim.fade_in_up)
            .start(true);
    }

    public static void startShimmerAnimation(@NonNull final View view) {
        if (view instanceof ViewGroup) {
            final ViewGroup parent = (ViewGroup)view;

            for (int i = 0; i < parent.getChildCount(); i++) Animations.startShimmerAnimation(parent.getChildAt(i));

            if (view instanceof ShimmerLayout) ((ShimmerLayout)view).startShimmerAnimation();
        }
    }

    public static void stopShimmerAnimation(@NonNull final View view) {
        if (view instanceof ViewGroup) {
            final ViewGroup parent = (ViewGroup)view;

            for (int i = 0; i < parent.getChildCount(); i++) Animations.stopShimmerAnimation(parent.getChildAt(i));

            if (view instanceof ShimmerLayout) ((ShimmerLayout)view).stopShimmerAnimation();
        }
    }

    public static boolean isEnabled() {
        return !DevUtils.isRunningInstrumentedTest() && (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || ValueAnimator.areAnimatorsEnabled());
    }

    private static int getAnimationDuration(@NonNull final Context context, @IntegerRes final int durationId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) return (int)(context.getResources().getInteger(durationId) * Settings.Global.getFloat(context.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1f));

        return context.getResources().getInteger(durationId);
    }
}
