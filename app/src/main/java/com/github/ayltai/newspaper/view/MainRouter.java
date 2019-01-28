package com.github.ayltai.newspaper.view;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import android.animation.Animator;
import android.app.Activity;
import android.graphics.Point;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.AnimationUtils;
import com.github.ayltai.newspaper.widget.MainView;

import flow.Direction;

@Singleton
public final class MainRouter extends Router {
    public MainRouter(@Nonnull @NonNull @lombok.NonNull final Activity activity) {
        super(activity);
    }

    @Nonnull
    @NonNull
    @Override
    protected Object getDefaultKey() {
        return MainView.KEY;
    }

    @Override
    protected int getContainerId() {
        return R.id.container;
    }

    @Nonnull
    @NonNull
    @Override
    protected List<Presenter.Factory> getFactories() {
        return Arrays.asList(
            new MainPresenter.Factory(),
            new DetailedItemPresenter.Factory(),
            new AboutPresenter.Factory()
        );
    }

    @Nullable
    @Override
    protected Animator getAnimator(@Nonnull @NonNull @lombok.NonNull final View view, @Nonnull @NonNull @lombok.NonNull final Direction direction, @Nullable final Point position, @Nullable final Runnable onStart, @Nullable final Runnable onEnd) {
        if (direction == Direction.FORWARD || direction == Direction.BACKWARD) return AnimationUtils.createDefaultAnimator(view, direction, position, onStart, onEnd);

        return super.getAnimator(view, direction, position, onStart, onEnd);
    }

    @Nonnull
    @NonNull
    @Override
    protected Pair<Presenter, Presenter.View> onDispatch(@Nullable final Object key) {
        Presenter      presenter = null;
        Presenter.View view      = null;

        final Pair<SoftReference<Presenter>, SoftReference<Presenter.View>> pair = this.getCache().get(key);

        if (pair != null && pair.first != null && pair.second != null) {
            presenter = pair.first.get();
            view      = pair.second.get();
        }

        if (presenter == null || view == null && key != null) {
            for (final Presenter.Factory factory : this.getFactories()) {
                if (factory.isSupported(key)) {
                    presenter = factory.createPresenter();
                    view      = factory.createView(this.getContext());

                    break;
                }
            }
        }

        if (presenter != null && view != null) {
            presenter.onViewDetached();

            if (key instanceof ModelKey && presenter instanceof ModelPresenter) {
                final ModelPresenter modelPresenter = (ModelPresenter)presenter;
                modelPresenter.setModel(((ModelKey)key).getModel());
                modelPresenter.bindModel();
            }
        }

        return Pair.create(presenter, view);
    }
}
