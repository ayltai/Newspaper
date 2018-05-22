package com.github.ayltai.newspaper.app;

import android.animation.Animator;
import android.app.Activity;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.View;

import com.github.ayltai.newspaper.analytics.ViewEvent;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.view.DetailsPresenter;
import com.github.ayltai.newspaper.app.view.MainPresenter;
import com.github.ayltai.newspaper.app.widget.DetailsView;
import com.github.ayltai.newspaper.app.widget.MainView;
import com.github.ayltai.newspaper.util.Animations;
import com.github.ayltai.newspaper.view.ModelPresenter;
import com.github.ayltai.newspaper.view.Presenter;
import com.github.ayltai.newspaper.view.RxFlow;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.List;

import flow.Direction;

final class MainFlow extends RxFlow {
    MainFlow(@NonNull final Activity activity) {
        super(activity);
    }

    @NonNull
    @Override
    protected Object getDefaultKey() {
        return MainView.KEY;
    }

    @NonNull
    @Override
    protected List<Presenter.Factory> getFactories() {
        return Arrays.asList(
            new MainPresenter.Factory(),
            new DetailsPresenter.Factory()
        );
    }

    @Nullable
    @Override
    protected Animator getAnimator(@NonNull final View view, @NonNull final Direction direction, @Nullable final Point location, @Nullable final Runnable onStart, @Nullable final Runnable onEnd) {
        if (direction == Direction.FORWARD || direction == Direction.BACKWARD) return Animations.createDefaultAnimator(view, direction, location, onStart, onEnd);

        return super.getAnimator(view, direction, location, onStart, onEnd);
    }

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

            if (key instanceof DetailsView.Key && presenter instanceof ModelPresenter) {
                final ModelPresenter modelPresenter = (ModelPresenter)presenter;
                modelPresenter.setModel(((DetailsView.Key)key).getItem());
                modelPresenter.bindModel();
            }
        }

        return Pair.create(presenter, view);
    }
}
