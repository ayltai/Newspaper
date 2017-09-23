package com.github.ayltai.newspaper.app;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.github.ayltai.newspaper.app.screen.DetailsPresenter;
import com.github.ayltai.newspaper.app.screen.DetailsScreen;
import com.github.ayltai.newspaper.app.screen.MainPresenter;
import com.github.ayltai.newspaper.app.screen.MainScreen;
import com.github.ayltai.newspaper.util.TestUtils;
import com.github.ayltai.newspaper.view.BindingPresenter;
import com.github.ayltai.newspaper.view.Presenter;
import com.github.ayltai.newspaper.view.ScreenPresenter;

import flow.Flow;
import flow.KeyDispatcher;
import flow.KeyParceler;
import flow.State;
import flow.TraversalCallback;
import io.reactivex.disposables.Disposable;

final class FlowController {
    private final Map<Object, Pair<WeakReference<Presenter>, WeakReference<Presenter.View>>> cache       = Collections.synchronizedMap(new ArrayMap<>());
    private final Map<Object, Disposable>                                                    disposables = Collections.synchronizedMap(new ArrayMap<>());

    private final Activity activity;

    FlowController(@NonNull final Activity activity) {
        this.activity = activity;
    }

    @SuppressWarnings("unchecked")
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

                Presenter      presenter = null;
                Presenter.View view      = null;

                final Pair<WeakReference<Presenter>, WeakReference<Presenter.View>> pair = this.cache.get(incomingState.getKey());

                if (pair != null) {
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
                    if (pair == null) this.cache.put(incomingState.getKey(), Pair.create(new WeakReference<>(presenter), new WeakReference<>(view)));

                    presenter.onViewDetached();

                    if (incomingState.getKey() instanceof DetailsScreen.Key && presenter instanceof BindingPresenter) ((BindingPresenter)presenter).bindModel(((DetailsScreen.Key)incomingState.getKey()).getItem());

                    this.subscribe(presenter, view);
                    this.dispatch((View)view, incomingState, callback);
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
        if (Flow.get(this.activity).goBack()) return true;

        final View view = ((ViewGroup)this.activity.findViewById(android.R.id.content)).getChildAt(0);
        return view instanceof ScreenPresenter.View && ((ScreenPresenter.View)view).goBack();
    }

    void onDestroy() {
        this.cache.clear();

        for (final Disposable disposable : this.disposables.values()) disposable.dispose();
        this.disposables.clear();
    }

    private void dispatch(@NonNull final View view, @NonNull final State incomingState, @NonNull final TraversalCallback callback) {
        incomingState.restore(view);

        this.activity.setContentView(view);

        callback.onTraversalCompleted();
    }

    @SuppressWarnings("unchecked")
    private void subscribe(@NonNull final Presenter presenter, @NonNull final Presenter.View view) {
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

    private void manageDisposable(@NonNull final Object key, @NonNull final Disposable disposable) {
        if (this.disposables.containsKey(key)) this.disposables.get(key).dispose();

        this.disposables.put(key, disposable);
    }
}
