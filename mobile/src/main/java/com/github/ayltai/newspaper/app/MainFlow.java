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
import com.github.ayltai.newspaper.app.view.DetailsListPresenter;
import com.github.ayltai.newspaper.app.view.DetailsPresenter;
import com.github.ayltai.newspaper.app.view.MainPresenter;
import com.github.ayltai.newspaper.app.widget.DetailsListView;
import com.github.ayltai.newspaper.app.widget.DetailsView;
import com.github.ayltai.newspaper.app.widget.MainView;
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
        return MainView.KEY;
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
            } else if (key instanceof DetailsListView.Key) {
                presenter = new DetailsListPresenter();
                view      = new DetailsListView(this.getContext());
            }
        }

        if (presenter != null && view != null) {
            presenter.onViewDetached();

            if (key instanceof DetailsListView.Key) {
                final DetailsListPresenter detailsListPresenter = ((DetailsListPresenter)presenter);
                final DetailsListView.Key  detailsListViewKey   = (DetailsListView.Key)key;

                detailsListPresenter.setCategory(detailsListViewKey.getCategory());
                detailsListPresenter.setItemPosition(detailsListViewKey.getItemPosition());
            } else if (key instanceof DetailsView.Key && presenter instanceof ModelPresenter) {
                ((ModelPresenter)presenter).bindModel(((DetailsView.Key)key).getItem());
            }
        }

        return Pair.create(presenter, view);
    }
}
