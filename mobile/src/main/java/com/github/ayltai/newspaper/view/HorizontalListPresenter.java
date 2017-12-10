package com.github.ayltai.newspaper.view;

import java.util.List;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.RxUtils;

public abstract class HorizontalListPresenter<M, V extends ListPresenter.View<M>> extends ListPresenter<M, V> {
    @Override
    public void bindModel(final List<M> models) {
        super.bindModel(models);

        if (this.getView() != null) this.getView().bind(models);
    }

    @CallSuper
    @Override
    public void onViewAttached(@NonNull final V view, final boolean isFirstAttached) {
        super.onViewAttached(view, isFirstAttached);

        this.manageDisposable(this.load()
            .compose(RxUtils.applyFlowableBackgroundToMainSchedulers())
            .subscribe(
                this::bindModel,
                error -> {
                    if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                }));

        view.scrollTo(this.scrollPosition, false);

        this.manageDisposable(view.bestVisibleItemPositionChanges().subscribe(scrollPosition -> this.scrollPosition = scrollPosition));
    }
}
