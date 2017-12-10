package com.github.ayltai.newspaper.view;

import android.support.annotation.UiThread;

public class ModelPresenter<M, V extends Presenter.View> extends Presenter<V> {
    private M model;

    public M getModel() {
        return this.model;
    }

    public void setModel(final M model) {
        this.model = model;
    }

    @UiThread
    public void bindModel(final M model) {
        this.setModel(model);
    }
}
