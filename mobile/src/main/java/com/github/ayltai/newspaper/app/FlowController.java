package com.github.ayltai.newspaper.app;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.analytics.ViewEvent;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.screen.DetailsPresenter;
import com.github.ayltai.newspaper.app.screen.DetailsScreen;
import com.github.ayltai.newspaper.app.screen.MainPresenter;
import com.github.ayltai.newspaper.app.screen.MainScreen;
import com.github.ayltai.newspaper.util.Animations;
import com.github.ayltai.newspaper.util.TestUtils;
import com.github.ayltai.newspaper.view.BindingPresenter;
import com.github.ayltai.newspaper.view.Presenter;

import flow.Direction;
import flow.Flow;
import flow.KeyDispatcher;
import flow.KeyParceler;
import flow.State;
import flow.TraversalCallback;
import io.reactivex.disposables.Disposable;

final class FlowController {
    private final Map<Object, Pair<SoftReference<Presenter>, SoftReference<Presenter.View>>> cache       = Collections.synchronizedMap(new ArrayMap<>());
    private final Map<Object, Disposable>                                                    disposables = Collections.synchronizedMap(new ArrayMap<>());

    private final Activity activity;

    FlowController(@NonNull final Activity activity) {
        this.activity = activity;
    }

    @SuppressWarnings({ "unchecked", "CyclomaticComplexity" })
    @NonNull
    Context attachNewBase(@NonNull final Context newBase) {
        return Flow.configure(newBase, this.activity)
            .keyParceler(new KeyParceler() {
                @NonNull
                @Override
                public Parcelable toParcelable(@NonNull final Object key) {
                    return (Parcelable)key;
                }

                @NonNull
                @Override
                public Object toKey(@NonNull final Parcelable parcelable) {
                    return parcelable;
                }
            })
            .dispatcher(KeyDispatcher.configure(this.activity, (outgoingState, incomingState, direction, incomingContexts, callback) -> {
                if (outgoingState != null) outgoingState.save(((ViewGroup)this.activity.findViewById(android.R.id.content)).getChildAt(0));

                if (incomingState.getKey() instanceof DetailsScreen.Key) {
                    final Item item = ((DetailsScreen.Key)incomingState.getKey()).getItem();

                    ComponentFactory.getInstance()
                        .getAnalyticsComponent(this.activity)
                        .eventLogger()
                        .logEvent(new ViewEvent()
                            .setScreenName(DetailsScreen.class.getSimpleName())
                            .setSource(item.getSource())
                            .setCategory(item.getCategory()));
                }

                Presenter      presenter = null;
                Presenter.View view      = null;

                final Pair<SoftReference<Presenter>, SoftReference<Presenter.View>> pair = this.cache.get(incomingState.getKey());

                if (pair != null && pair.first != null && pair.second != null) {
                    presenter = pair.first.get();
                    view      = pair.second.get();
                }

                if (presenter == null || view == null) {
                    if (incomingState.getKey() instanceof MainScreen.Key) {
                        presenter = new MainPresenter();
                        view      = new MainScreen(this.activity);
                    } else if (incomingState.getKey() instanceof DetailsScreen.Key) {
                        presenter = new DetailsPresenter();
                        view      = new DetailsScreen(this.activity);
                    }
                }

                if (presenter != null && view != null) {
                    if (pair == null) this.cache.put(incomingState.getKey(), Pair.create(new SoftReference<>(presenter), new SoftReference<>(view)));

                    presenter.onViewDetached();

                    if (incomingState.getKey() instanceof DetailsScreen.Key && presenter instanceof BindingPresenter) ((BindingPresenter)presenter).bindModel(((DetailsScreen.Key)incomingState.getKey()).getItem());

                    this.subscribe(presenter, view);
                    this.dispatch((View)view, incomingState, direction, callback);
                }
            }).build())
            .defaultKey(MainScreen.KEY)
            .install();
    }

    /**
     * Attempts to handle back button pressed event.
     * @return {@code true} if back button pressed event is handled; otherwise, {@code false}.
     */
    boolean onBackPressed() {
        return Flow.get(this.activity).goBack();
    }

    void onDestroy() {
        this.cache.clear();

        for (final Disposable disposable : this.disposables.values()) disposable.dispose();
        this.disposables.clear();
    }

    private void dispatch(@NonNull final View toView, @NonNull final State incomingState, @NonNull final Direction direction, @NonNull final TraversalCallback callback) {
        incomingState.restore(toView);

        final ViewGroup container = this.activity.findViewById(R.id.container);
        final View      fromView  = container.getChildCount() == 0 ? null : container.getChildAt(0);

        if (fromView == toView) {
            final Pair<SoftReference<Presenter>, SoftReference<Presenter.View>> pair = this.cache.get(incomingState.getKey());

            if (pair != null && pair.first != null && pair.second != null) {
                final Presenter.View view = pair.second.get();

                this.subscribe(pair.first.get(), view);

                view.onAttachedToWindow();
            }
        } else {
            if (direction == Direction.FORWARD) {
                container.addView(toView);

                if (fromView != null) {
                    if (Animations.isEnabled()) {
                        toView.startAnimation(Animations.getAnimation(this.activity, R.anim.reveal_enter, android.R.integer.config_mediumAnimTime, null, () -> {
                            if (fromView != null) container.removeView(fromView);
                        }));
                    } else {
                        container.removeView(fromView);
                    }
                }
            } else if (direction == Direction.BACKWARD) {
                container.addView(toView, 0);

                if (fromView != null) {
                    if (Animations.isEnabled()) {
                        fromView.startAnimation(Animations.getAnimation(this.activity, R.anim.reveal_exit, android.R.integer.config_mediumAnimTime, null, () -> container.removeView(fromView)));
                    } else {
                        container.removeView(fromView);
                    }
                }
            } else if (direction == Direction.REPLACE) {
                container.addView(toView);
                if (fromView != null) container.removeView(fromView);
            }
        }

        callback.onTraversalCompleted();
    }

    @SuppressWarnings("unchecked")
    private void subscribe(@Nullable final Presenter presenter, @Nullable final Presenter.View view) {
        if (presenter != null && view != null) {
            if (view.attachments() != null) this.manageDisposable(view.attachments(), view.attachments().subscribe(
                isFirstTimeAttachment -> presenter.onViewAttached(view, isFirstTimeAttachment),
                error -> {
                    if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
                }
            ));

            if (view.detachments() != null) this.manageDisposable(view.detachments(), view.detachments().subscribe(
                irrelevant -> presenter.onViewDetached(),
                error -> {
                    if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
                }
            ));
        }
    }

    private void manageDisposable(@NonNull final Object key, @NonNull final Disposable disposable) {
        if (this.disposables.containsKey(key)) this.disposables.get(key).dispose();

        this.disposables.put(key, disposable);
    }
}
