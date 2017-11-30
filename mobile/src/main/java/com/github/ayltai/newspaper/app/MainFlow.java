package com.github.ayltai.newspaper.app;

import java.lang.ref.SoftReference;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.animation.Animation;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.analytics.ViewEvent;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.screen.DetailsPresenter;
import com.github.ayltai.newspaper.app.screen.DetailsScreen;
import com.github.ayltai.newspaper.app.screen.MainPresenter;
import com.github.ayltai.newspaper.app.screen.MainScreen;
import com.github.ayltai.newspaper.util.Animations;
import com.github.ayltai.newspaper.view.ModelPresenter;
import com.github.ayltai.newspaper.view.Presenter;
import com.github.ayltai.newspaper.view.RxFlow;

import flow.Direction;

final class MainFlow extends RxFlow {
    MainFlow(@NonNull final Activity activity) {
        super(activity);
    }

    @NonNull
    @Override
    protected Object getDefaultKey() {
        return MainScreen.KEY;
    }

    @Nullable
    @Override
    protected Animation getAnimation(@NonNull final Direction direction, @Nullable final Runnable onStart, @Nullable final Runnable onEnd) {
        if (direction == Direction.FORWARD) return Animations.getAnimation(this.getContext(), R.anim.reveal_enter, android.R.integer.config_mediumAnimTime, onStart, onEnd);
        if (direction == Direction.BACKWARD) return Animations.getAnimation(this.getContext(), R.anim.reveal_exit, android.R.integer.config_mediumAnimTime, onStart, onEnd);

        return super.getAnimation(direction, onStart, onEnd);
    }

    @SuppressWarnings({ "unchecked", "CyclomaticComplexity" })
    @NonNull
    @Override
    protected Pair<Presenter, Presenter.View> onDispatch(@Nullable final Object key) {
        if (key instanceof DetailsScreen.Key) {
            final Item item = ((DetailsScreen.Key)key).getItem();

            ComponentFactory.getInstance()
                .getAnalyticsComponent(this.getContext())
                .eventLogger()
                .logEvent(new ViewEvent()
                    .setScreenName(DetailsScreen.class.getSimpleName())
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
            if (key instanceof MainScreen.Key) {
                presenter = new MainPresenter();
                view      = new MainScreen(this.getContext());
            } else if (key instanceof DetailsScreen.Key) {
                presenter = new DetailsPresenter();
                view      = new DetailsScreen(this.getContext());
            }
        }

        if (presenter != null && view != null) {
            presenter.onViewDetached();

            if (key instanceof DetailsScreen.Key && presenter instanceof ModelPresenter) ((ModelPresenter)presenter).bindModel(((DetailsScreen.Key)key).getItem());
        }

        return Pair.create(presenter, view);
    }
}
