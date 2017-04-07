package com.github.ayltai.newspaper;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.util.LogUtils;

import rx.Observable;

public abstract class Presenter<V extends Presenter.View> extends BasePresenter<V> {
    public interface View {
        @Nullable
        Observable<Void> attachments();

        @Nullable
        Observable<Void> detachments();

        @NonNull
        Context getContext();
    }


    private boolean isViewAttached;

    protected Presenter() {
    }

    protected boolean isViewAttached() {
        return this.isViewAttached;
    }

    @Nullable
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

    @NonNull
    public RxBus bus() {
        return RxBus.getInstance();
    }

    @NonNull
    public LogUtils log() {
        return LogUtils.getInstance();
    }
}
