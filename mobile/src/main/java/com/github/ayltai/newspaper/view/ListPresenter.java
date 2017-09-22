package com.github.ayltai.newspaper.view;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.Flowable;

public abstract class ListPresenter<M, V extends ListPresenter.View<M>> extends BindingPresenter<List<M>, V> {
    public interface View<M> extends Presenter.View {
        void bind(@NonNull List<M> models);

        void clear();

        void up();

        void refresh();

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

    public abstract Flowable<List<M>> load();

    protected void resetState() {
    }

    @Override
    public void bindModel(final List<M> models) {
        super.bindModel(models);

        if (this.getView() != null) {
            if (models.isEmpty()) this.getView().showEndOfList();

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
                        if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
                    }));
        }

        this.subscribePullToRefreshes(view);
    }

    private void subscribePullToRefreshes(@NonNull final V view) {
        this.manageDisposable(view.pullToRefreshes().subscribe(
            irrelevant -> {
                if (TestUtils.isLoggable()) Log.d(this.getClass().getSimpleName(), "Pull-to-refresh");

                this.resetState();

                final AtomicBoolean hasCleared = new AtomicBoolean(false);

                this.manageDisposable(this.load().subscribe(
                    models -> {
                        if (!hasCleared.get()) {
                            hasCleared.set(true);
                            view.clear();
                        }

                        this.bindModel(models);
                    },
                    error -> {
                        if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
                    }));
            },
            error -> {
                if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
            }
        ));
    }
}
