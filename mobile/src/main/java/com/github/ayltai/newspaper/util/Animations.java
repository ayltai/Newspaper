package com.github.ayltai.newspaper.util;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

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

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;

import io.reactivex.Flowable;
import io.supercharge.shimmerlayout.ShimmerLayout;

public final class Animations {
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

            Flowable.interval(this.offset, TimeUnit.MILLISECONDS)
                .compose(RxUtils.applyFlowableBackgroundToMainSchedulers())
                .take(queue.size())
                .subscribe(time -> {
                    if (!queue.isEmpty()) {
                        final View view = queue.poll();

                        view.setVisibility(View.VISIBLE);
                        view.startAnimation(this.animation);
                    }
                });
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

    public static boolean isEnabled() {
        return !DevUtils.isRunningInstrumentedTest() && (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || ValueAnimator.areAnimatorsEnabled());
    }
}
