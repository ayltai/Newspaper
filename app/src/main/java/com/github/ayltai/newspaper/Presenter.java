package com.github.ayltai.newspaper;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.Observable;

public abstract class Presenter<V extends Presenter.View> {
    public interface View {
        @Nullable Observable<Void> attachments();

        @Nullable Observable<Void> detachments();
    }

    private V       view;
    private boolean isViewAttached;

    protected Presenter() {
    }

    protected boolean isViewAttached() {
        return this.isViewAttached;
    }

    public V getView() {
        return this.view;
    }

    @CallSuper
    public void onViewAttached(@NonNull final V view) {
        this.view           = view;
        this.isViewAttached = true;
    }

    @CallSuper
    public void onViewDetached() {
        this.isViewAttached = false;
    }
}
