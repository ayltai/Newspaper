package com.github.ayltai.newspaper.view.binding;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.view.BindingPresenter;
import com.github.ayltai.newspaper.view.Presenter;

public abstract class PresentationBinder<M, V extends Presenter.View> extends BindingPresenter<M, V> implements Binder<V> {
    @CallSuper
    @Override
    public void bindView(@NonNull final V view) {
        super.bindModel(this.getModel());

        this.onViewAttached(view, false);

        if (this.getModel() != null) this.bindModel(this.getModel());
    }
}
