package com.github.ayltai.newspaper.view.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.view.Presenter;

public abstract class BindingPresenterFactory<M, V extends Presenter.View, P extends BindingPresenter<M, V>> implements PartBinderFactory<M, V> {
    @NonNull
    protected abstract P createPresenter();

    @NonNull
    @Override
    public Binder<V> create(@Nullable final M model) {
        if (this.isNeeded(model)) {
            final P presenter = this.createPresenter();
            presenter.setModel(model);
            presenter.bindModel();

            return presenter;
        }

        return new NoOpBinder<>();
    }
}
