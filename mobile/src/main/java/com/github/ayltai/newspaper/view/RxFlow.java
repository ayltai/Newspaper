package com.github.ayltai.newspaper.view;

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
import android.view.animation.Animation;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.Animations;
import com.github.ayltai.newspaper.util.DevUtils;

import flow.Direction;
import flow.Flow;
import flow.KeyDispatcher;
import flow.KeyParceler;
import flow.State;
import flow.TraversalCallback;
import io.reactivex.disposables.Disposable;

public abstract class RxFlow {
    private final Map<Object, Pair<SoftReference<Presenter>, SoftReference<Presenter.View>>> cache       = Collections.synchronizedMap(new ArrayMap<>());
    private final Map<Object, Disposable>                                                    disposables = Collections.synchronizedMap(new ArrayMap<>());

    private final Activity activity;

    public RxFlow(@NonNull final Activity activity) {
        this.activity = activity;
    }

    @NonNull
    protected abstract Object getDefaultKey();

    @NonNull
    protected Context getContext() {
        return this.activity;
    }

    @NonNull
    protected Map<Object, Pair<SoftReference<Presenter>, SoftReference<Presenter.View>>> getCache() {
        return this.cache;
    }

    @Nullable
    protected Animation getAnimation(@NonNull final Direction direction, @Nullable final Runnable onStart, @Nullable final Runnable onEnd) {
        return null;
    }

    @NonNull
    public Context attachNewBase(@NonNull final Context newBase) {
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

                final Pair<Presenter, Presenter.View> pair      = this.onDispatch(incomingState.getKey());
                final Presenter                       presenter = pair.first;
                final Presenter.View                  view      = pair.second;

                if (presenter != null && view != null) {
                    this.cache.put(incomingState.getKey(), Pair.create(new SoftReference<>(presenter), new SoftReference<>(view)));

                    this.subscribe(presenter, view);
                    this.dispatch((View)view, incomingState, direction, callback);
                }
            }).build())
            .defaultKey(this.getDefaultKey())
            .install();
    }

    /**
     * Attempts to handle back button pressed event.
     * @return {@code true} if back button pressed event is handled; otherwise, {@code false}.
     */
    public boolean onBackPressed() {
        return Flow.get(this.activity).goBack();
    }

    public void onDestroy() {
        this.cache.clear();

        for (final Disposable disposable : this.disposables.values()) disposable.dispose();
        this.disposables.clear();
    }

    @NonNull
    protected abstract Pair<Presenter, Presenter.View> onDispatch(@Nullable Object key);

    @SuppressWarnings("CyclomaticComplexity")
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
                        final Animation animation = this.getAnimation(Direction.FORWARD, null, () -> container.removeView(fromView));
                        if (animation != null) toView.startAnimation(animation);
                    } else {
                        container.removeView(fromView);
                    }
                }
            } else if (direction == Direction.BACKWARD) {
                container.addView(toView, 0);

                if (fromView != null) {
                    if (Animations.isEnabled()) {
                        final Animation animation = this.getAnimation(Direction.BACKWARD, null, () -> container.removeView(fromView));
                        if (animation != null) fromView.startAnimation(animation);
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
                    if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                }
            ));

            if (view.detachments() != null) this.manageDisposable(view.detachments(), view.detachments().subscribe(
                irrelevant -> presenter.onViewDetached(),
                error -> {
                    if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                }
            ));
        }
    }

    private void manageDisposable(@NonNull final Object key, @NonNull final Disposable disposable) {
        if (this.disposables.containsKey(key)) this.disposables.get(key).dispose();

        this.disposables.put(key, disposable);
    }
}
