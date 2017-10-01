package com.github.ayltai.newspaper.view.binding;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.github.ayltai.newspaper.view.BindingPresenter;
import com.github.ayltai.newspaper.view.Presenter;

import io.reactivex.disposables.Disposable;

public abstract class PresentationBinder<M, V extends Presenter.View> extends BindingPresenter<M, V> implements Binder<V>, Disposable {
    @Override
    public boolean isDisposed() {
        return false;
    }

    @UiThread
    @CallSuper
    @Override
    public void bindView(@NonNull final V view) {
        this.onViewDetached();
        this.onViewAttached(view, false);
        this.bindModel(this.getModel());
    }

    @Override
    public void dispose() {
        this.onViewDetached();
    }
}
