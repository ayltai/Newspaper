package com.github.ayltai.newspaper.view;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

public abstract class ModelPresenter<M, V extends Presenter.View> extends Presenter<V> {
    private M model;

    public M getModel() {
        return this.model;
    }

    public void setModel(final M model) {
        this.model = model;
    }

    @UiThread
    public abstract void bindModel();

    @CallSuper
    @Override
    public void onViewAttached(@NonNull final V view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        this.bindModel();
    }
}
