package com.github.ayltai.newspaper.view;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.core.util.Pair;

import com.github.ayltai.newspaper.util.AnimationUtils;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.RxUtils;

import flow.Direction;
import flow.Flow;
import flow.KeyDispatcher;
import flow.KeyParceler;
import flow.State;
import flow.TraversalCallback;
import io.reactivex.disposables.Disposable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Router implements Disposable {
    @Getter(AccessLevel.PROTECTED)
    @Nonnull
    @NonNull
    private final Map<Object, Pair<SoftReference<Presenter>, SoftReference<Presenter.View>>> cache       = Collections.synchronizedMap(new ArrayMap<>());
    private final Map<Object, Disposable>                                                    disposables = new ArrayMap<>();

    @Nonnull
    @NonNull
    private final Activity activity;

    private boolean isDisposed;

    @Override
    public boolean isDisposed() {
        return this.isDisposed;
    }

    @Nonnull
    @NonNull
    protected abstract Object getDefaultKey();

    @IdRes
    protected abstract int getContainerId();

    @Nonnull
    @NonNull
    protected Context getContext() {
        return this.activity;
    }

    @Nonnull
    @NonNull
    protected abstract List<Presenter.Factory> getFactories();

    @Nullable
    protected Animator getAnimator(@Nonnull @NonNull @lombok.NonNull final View view, @Nonnull @NonNull @lombok.NonNull final Direction direction, @Nullable final Point position, @Nullable final Runnable onStart, @Nullable final Runnable onEnd) {
        return null;
    }

    @Nonnull
    @NonNull
    public Context attachNewBase(@Nonnull @NonNull @lombok.NonNull final Context newBase) {
        return Flow.configure(newBase, this.activity)
            .keyParceler(new KeyParceler() {
                @Nonnull
                @NonNull
                @Override
                public Parcelable toParcelable(@Nonnull @NonNull @lombok.NonNull final Object key) {
                    return (Parcelable)key;
                }

                @Nonnull
                @NonNull
                @Override
                public Object toKey(@Nonnull @NonNull @lombok.NonNull final Parcelable parcelable) {
                    return parcelable;
                }
            })
            .dispatcher(KeyDispatcher.configure(this.activity, (outgoingState, incomingState, direction, incomingContexts, callback) -> {
                if (outgoingState != null) outgoingState.save(((ViewGroup)this.activity.findViewById(android.R.id.content)).getChildAt(0));

                final Pair<Presenter, Presenter.View> pair      = this.onDispatch(incomingState.getKey());
                final Presenter                       presenter = pair.first;
                final Presenter.View                  view      = pair.second;

                if (presenter != null && view != null) {
                    if (direction != Direction.BACKWARD && !DevUtils.isRunningInstrumentedTest()) this.cache.put(incomingState.getKey(), Pair.create(new SoftReference<>(presenter), new SoftReference<>(view)));

                    this.subscribe(presenter, view);
                    this.dispatch((View)view, outgoingState, incomingState, direction, callback);
                }
            }).build())
            .defaultKey(this.getDefaultKey())
            .install();
    }

    public boolean handleBack() {
        final ViewGroup container = this.activity.findViewById(this.getContainerId());
        if (container == null || container.getChildCount() == 0) return Flow.get(this.activity).goBack();

        return ((Presenter.View)container.getChildAt(0)).handleBack() || Flow.get(this.activity).goBack();
    }

    @Override
    public void dispose() {
        this.isDisposed = true;

        this.cache.clear();

        synchronized (this.disposables) {
            for (final Disposable disposable : this.disposables.values()) disposable.dispose();

            this.disposables.clear();
        }
    }

    @Nonnull
    @NonNull
    protected abstract Pair<Presenter, Presenter.View> onDispatch(@Nullable Object key);

    private void dispatch(@Nonnull @NonNull @lombok.NonNull final View toView, @Nullable final State outgoingState, @Nonnull @NonNull @lombok.NonNull final State incomingState, @Nonnull @NonNull @lombok.NonNull final Direction direction, @Nonnull @NonNull @lombok.NonNull final TraversalCallback callback) {
        incomingState.restore(toView);

        final ViewGroup container = this.activity.findViewById(this.getContainerId());
        final View      fromView  = container.getChildCount() == 0 ? null : container.getChildAt(0);

        if (fromView == toView) {
            final Pair<SoftReference<Presenter>, SoftReference<Presenter.View>> pair = this.cache.get(incomingState.getKey());

            if (pair != null && pair.first != null && pair.second != null) {
                final Presenter.View view = pair.second.get();

                this.subscribe(pair.first.get(), view);

                view.onAttachedToWindow();
            }
        } else {
            if (direction == Direction.BACKWARD) {
                container.addView(toView, 0, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                this.dispatch(container, fromView, toView, direction, outgoingState != null && outgoingState.getKey() instanceof Locatable ? ((Locatable)outgoingState.getKey()).getLocation() : null);
            } else {
                container.addView(toView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                this.dispatch(container, fromView, toView, direction, incomingState.getKey() instanceof Locatable ? ((Locatable)incomingState.getKey()).getLocation() : null);
            }
        }

        callback.onTraversalCompleted();
    }

    private void dispatch(@Nonnull @NonNull @lombok.NonNull final ViewGroup container, @Nullable final View fromView, @Nonnull @NonNull @lombok.NonNull final View toView, @Nonnull @NonNull @lombok.NonNull final Direction direction, @Nullable final Point position) {
        if (fromView != null) {
            if (AnimationUtils.areAnimatorsEnabled()) {
                final Animator animator = this.getAnimator(direction == Direction.BACKWARD ? fromView : toView, direction, position, null, () -> container.removeView(fromView));
                if (animator == null) {
                    container.removeView(fromView);
                } else {
                    animator.start();
                }
            } else {
                container.removeView(fromView);
            }
        }
    }

    private <V extends Presenter.View> void subscribe(@Nullable final Presenter<V> presenter, @Nullable final V view) {
        if (presenter != null && view != null) {
            this.manageDisposable(view.attaches(), view.attaches().subscribe(
                isFirstAttachment -> presenter.onViewAttached(view, isFirstAttachment),
                RxUtils::handleError
            ));

            this.manageDisposable(view.detaches(), view.detaches().subscribe(
                irrelevant -> presenter.onViewDetached(),
                RxUtils::handleError
            ));
        }
    }

    private void manageDisposable(@Nonnull @NonNull @lombok.NonNull final Object key, @Nonnull @NonNull @lombok.NonNull final Disposable disposable) {
        synchronized (this.disposables) {
            if (this.disposables.containsKey(key)) this.disposables.get(key).dispose();

            this.disposables.put(key, disposable);
        }
    }
}
