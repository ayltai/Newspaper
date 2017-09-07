package com.github.ayltai.newspaper.view.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.view.Presenter;

public abstract class PresentationBinderFactory<M, V extends Presenter.View, P extends PresentationBinder<M, V>> implements PartBinderFactory<M, V> {
    @NonNull
    protected abstract P createPresenter();

    @NonNull
    @Override
    public Binder<V> create(@Nullable final M model) {
        final P presenter = this.createPresenter();

        if (this.isNeeded(model)) presenter.bindModel(model);

        return presenter;
    }
}
