package com.github.ayltai.newspaper.view.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.view.Presenter;

public abstract class PresentationBinderFactory<M, V extends Presenter.View, P extends PresentationBinder<M, V>> implements PartBinderFactory<M, V> {
    @NonNull
    protected abstract P createPresenter();

    @Nullable
    @Override
    public Binder<V> create(@Nullable final M model) {
        if (this.isNeeded(model)) {
            final P presenter = this.createPresenter();
            presenter.bindModel(model);

            return presenter;
        }

        return new NoOpBinder<>();
    }
}
