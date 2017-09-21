package com.github.ayltai.newspaper.view;

import android.support.annotation.UiThread;

public class BindingPresenter<M, V extends Presenter.View> extends ObservablePresenter<V> {
    private M model;

    protected M getModel() {
        return this.model;
    }

    protected void setModel(final M model) {
        this.model = model;
    }

    @UiThread
    public void bindModel(final M model) {
        this.setModel(model);
    }
}
