package com.github.ayltai.newspaper.view;

import javax.annotation.Nonnull;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import lombok.Getter;
import lombok.Setter;

public abstract class ModelPresenter<M, V extends Presenter.View> extends BasePresenter<V> {
    @Getter
    @Setter
    private M model;

    @UiThread
    public abstract void bindModel();

    @CallSuper
    @Override
    public void onViewAttached(@Nonnull @NonNull @lombok.NonNull final V view, final boolean isFirstAttachment) {
        super.onViewAttached(view, isFirstAttachment);

        this.bindModel();
    }
}
