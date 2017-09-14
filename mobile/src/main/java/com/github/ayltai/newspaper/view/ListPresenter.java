package com.github.ayltai.newspaper.view;

import java.util.List;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.util.Irrelevant;

import io.reactivex.Flowable;
import io.reactivex.Observable;

public abstract class ListPresenter<M, V extends ListPresenter.View<M>> extends BindingPresenter<List<M>, V> {
    public interface View<M> extends Presenter.View {
        void bind(@NonNull List<M> models);

        void clear();

        void showEmptyView();

        void showLoadingView();

        void showEndOfList();

        @NonNull
        Flowable<Irrelevant> pullToRefreshes();

        @NonNull
        Flowable<Irrelevant> infiniteLoads();

        @NonNull
        Flowable<Integer> bestVisibleItemPositionChanges();
    }

    public abstract Observable<List<M>> load();

    protected void resetState() {
    }

    @Override
    public void bindModel(final List<M> models) {
        super.bindModel(models);

        if (this.getView() != null) {
            if (models.isEmpty()) {
                this.getView().showEndOfList();
            }

            this.getView().bind(models);
        }
    }

    @CallSuper
    @Override
    public void onViewAttached(@NonNull final V view, final boolean isFirstAttached) {
        super.onViewAttached(view, isFirstAttached);

        view.showLoadingView();

        if (isFirstAttached) {
            final Observable<List<M>> observable = this.load();

            this.manageDisposable(observable.subscribe(
                this::bindModel,
                error -> {
                    if (BuildConfig.DEBUG) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
                }
            ));

            this.subscribePullToRefreshes(view);
        }
    }

    private void subscribePullToRefreshes(@NonNull final V view) {
        this.manageDisposable(view.pullToRefreshes().subscribe(
            irrelevant -> {
                if (BuildConfig.DEBUG) Log.d(this.getClass().getSimpleName(), "Pull-to-refresh");

                this.resetState();

                final Observable<List<M>> observable = this.load();

                this.manageDisposable(observable.subscribe(
                    models -> {
                        view.clear();
                        this.bindModel(models);
                    },
                    error -> {
                        if (BuildConfig.DEBUG) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
                    }));
            },
            error -> {
                if (BuildConfig.DEBUG) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
            }
        ));
    }
}
