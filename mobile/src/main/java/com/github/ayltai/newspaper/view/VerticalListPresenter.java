package com.github.ayltai.newspaper.view;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.data.DataManager;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.RxUtils;

import io.reactivex.Flowable;
import io.reactivex.Single;

public abstract class VerticalListPresenter<M, V extends VerticalListPresenter.View<M>> extends ListPresenter<M, V> {
    public interface View<M> extends ListPresenter.View<M> {
        void clear();

        void clearAll();

        void up();

        void refresh();

        void showEmptyView();

        void hideEmptyView();

        void showLoadingView();

        void hideLoadingView();

        @NonNull
        Flowable<Irrelevant> clears();

        @NonNull
        Flowable<Irrelevant> pullToRefreshes();
    }

    @NonNull
    public abstract Single<Irrelevant> clearAll();

    protected void onPullToRefresh() {
    }

    protected void resetState() {
        this.scrollPosition = 0;
    }

    @Override
    public void bindModel(final List<M> models) {
        super.bindModel(models);

        if (this.getView() != null) {
            if (models.isEmpty()) this.getView().showEmptyView();

            this.getView().clear();
            this.getView().bind(models);
        }
    }

    @CallSuper
    @Override
    public void onViewAttached(@NonNull final V view, final boolean isFirstAttached) {
        super.onViewAttached(view, isFirstAttached);

        if (isFirstAttached) {
            view.showLoadingView();

            this.manageDisposable(this.load()
                .compose(RxUtils.applyFlowableBackgroundToMainSchedulers())
                .subscribe(
                    this::bindModel,
                    error -> {
                        if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                    }));
        } else {
            view.scrollTo(this.scrollPosition, true);
        }

        this.subscribeClears(view);
        this.subscribePullToRefreshes(view);

        this.manageDisposable(view.bestVisibleItemPositionChanges().subscribe(scrollPosition -> this.scrollPosition = scrollPosition));
    }

    private void subscribeClears(@NonNull final V view) {
        this.manageDisposable(view.clears().subscribe(
            irrelevant -> {
                if (DevUtils.isLoggable()) Log.d(this.getClass().getSimpleName(), "Clear");

                this.resetState();

                this.clearAll()
                    .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER))
                    .map(dummy -> Collections.<M>emptyList())
                    .compose(RxUtils.applySingleBackgroundToMainSchedulers())
                    .subscribe(
                        this::bindModel,
                        error -> {
                            if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                        }
                    );
            },
            error -> {
                if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
            }
        ));
    }

    private void subscribePullToRefreshes(@NonNull final V view) {
        this.manageDisposable(view.pullToRefreshes().subscribe(
            irrelevant -> {
                if (DevUtils.isLoggable()) Log.d(this.getClass().getSimpleName(), "Pull-to-refresh");

                this.onPullToRefresh();
                this.resetState();

                final AtomicBoolean hasCleared = new AtomicBoolean(false);

                this.manageDisposable(this.load()
                    .compose(RxUtils.applyFlowableBackgroundToMainSchedulers())
                    .subscribe(
                        models -> {
                            if (!hasCleared.get()) {
                                hasCleared.set(true);
                                view.clear();
                            }

                            this.bindModel(models);
                        },
                        error -> {
                            if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                        }));
            },
            error -> {
                if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
            }
        ));
    }
}
