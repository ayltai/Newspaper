package com.github.ayltai.newspaper.view;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.data.DataManager;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.RxUtils;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

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

    @SuppressFBWarnings("ACEM_ABSTRACT_CLASS_EMPTY_METHODS")
    protected void onPullToRefresh() {
        // Ignored
    }

    protected void resetState() {
        this.scrollPosition = 0;
    }

    @CallSuper
    @Override
    public void bindModel() {
        if (this.getView() != null && this.getModel() != null) {
            if (this.getModel().isEmpty()) this.getView().showEmptyView();

            this.getView().clear();
            this.getView().bind(this.getModel());
        }
    }

    @CallSuper
    @Override
    public void onViewAttached(@NonNull final V view, final boolean isFirstAttached) {
        super.onViewAttached(view, isFirstAttached);

        if (isFirstAttached) {
            view.showLoadingView();

            this.manageDisposable(this.load()
                .compose(RxUtils.applyFlowableSchedulers(AndroidSchedulers.mainThread()))
                .subscribe(
                    models -> {
                        this.setModel(models);
                        this.bindModel();
                    },
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
                        models -> {
                            this.setModel(models);
                            this.bindModel();
                        },
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
                    .compose(RxUtils.applyFlowableSchedulers(AndroidSchedulers.mainThread()))
                    .subscribe(
                        models -> {
                            if (!hasCleared.get()) {
                                hasCleared.set(true);
                                view.clear();
                            }

                            this.setModel(models);
                            this.bindModel();
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
