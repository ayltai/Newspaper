package com.github.ayltai.newspaper.app;

import java.lang.ref.SoftReference;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AnimationUtils;

import com.github.ayltai.newspaper.analytics.ViewEvent;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.view.DetailsPresenter;
import com.github.ayltai.newspaper.app.view.MainPresenter;
import com.github.ayltai.newspaper.app.widget.DetailsView;
import com.github.ayltai.newspaper.app.widget.MainView;
import com.github.ayltai.newspaper.util.DeviceUtils;
import com.github.ayltai.newspaper.view.ModelPresenter;
import com.github.ayltai.newspaper.view.Presenter;
import com.github.ayltai.newspaper.view.RxFlow;

import flow.Direction;

final class MainFlow extends RxFlow {
    private static final String SCALE_Y = "scaleY";
    private static final String ALPHA   = "alpha";

    MainFlow(@NonNull final Activity activity) {
        super(activity);
    }

    @NonNull
    @Override
    protected Object getDefaultKey() {
        return MainView.KEY;
    }

    @Nullable
    @Override
    protected Animator getAnimator(@NonNull final View view, @NonNull final Direction direction, @Nullable final Point location, @Nullable final Runnable onStart, @Nullable final Runnable onEnd) {
        if (direction == Direction.FORWARD || direction == Direction.BACKWARD) {
            final Point    size   = DeviceUtils.getScreenSize(this.getContext());
            final int      pivotX = location == null ? size.x / 2 : location.x;
            final int      pivotY = location == null ? size.y / 2 : location.y;
            final Animator animator;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final float radius = (float)Math.hypot(size.x / 2.0, size.y / 2.0);

                animator = ViewAnimationUtils.createCircularReveal(view, pivotX, pivotY, direction == Direction.FORWARD ? 0 : radius, direction == Direction.FORWARD ? radius : 0);
            } else {
                final AnimatorSet animators = new AnimatorSet();

                if (direction == Direction.FORWARD) {
                    view.setScaleY(0);
                    view.setAlpha(0);

                    animators.play(ObjectAnimator.ofFloat(view, MainFlow.SCALE_Y, 1))
                        .with(ObjectAnimator.ofFloat(view, MainFlow.ALPHA, 1));
                } else {
                    animators.play(ObjectAnimator.ofFloat(view, MainFlow.SCALE_Y, 0))
                        .with(ObjectAnimator.ofFloat(view, MainFlow.ALPHA, 0));
                }

                animator = animators;
            }

            animator.setDuration(this.getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime));
            animator.setInterpolator(AnimationUtils.loadInterpolator(this.getContext(), direction == Direction.FORWARD ? android.R.interpolator.decelerate_cubic : android.R.interpolator.accelerate_quint));

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

        return super.getAnimator(view, direction, location, onStart, onEnd);
    }

    @SuppressWarnings({ "unchecked", "CyclomaticComplexity" })
    @NonNull
    @Override
    protected Pair<Presenter, Presenter.View> onDispatch(@Nullable final Object key) {
        if (key instanceof DetailsView.Key) {
            final Item item = ((DetailsView.Key)key).getItem();

            ComponentFactory.getInstance()
                .getAnalyticsComponent(this.getContext())
                .eventLogger()
                .logEvent(new ViewEvent()
                    .setScreenName(DetailsView.class.getSimpleName())
                    .setSource(item.getSource())
                    .setCategory(item.getCategory()));
        }

        Presenter      presenter = null;
        Presenter.View view      = null;

        final Pair<SoftReference<Presenter>, SoftReference<Presenter.View>> pair = this.getCache().get(key);

        if (pair != null && pair.first != null && pair.second != null) {
            presenter = pair.first.get();
            view      = pair.second.get();
        }

        if (presenter == null || view == null) {
            if (key instanceof MainView.Key) {
                presenter = new MainPresenter();
                view      = new MainView(this.getContext());
            } else if (key instanceof DetailsView.Key) {
                presenter = new DetailsPresenter();
                view      = new DetailsView(this.getContext());
            }
        }

        if (presenter != null && view != null) {
            presenter.onViewDetached();

            if (key instanceof DetailsView.Key && presenter instanceof ModelPresenter) ((ModelPresenter)presenter).bindModel(((DetailsView.Key)key).getItem());
        }

        return Pair.create(presenter, view);
    }
}
