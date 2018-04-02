package com.github.ayltai.newspaper.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.RxUtils;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;

public class Presenter<V extends Presenter.View> {
    public interface View {
        Flowable<Boolean> attachments();

        Flowable<Irrelevant> detachments();

        @NonNull
        Context getContext();

        @Nullable
        Activity getActivity();

        @Nullable
        LifecycleOwner getLifecycleOwner();

        @CallSuper
        void onAttachedToWindow();

        @CallSuper
        void onDetachedFromWindow();
    }

    private final List<Disposable> disposables = Collections.synchronizedList(new ArrayList<>());

    private V view;

    @Nullable
    public V getView() {
        return this.view;
    }

    protected void manageDisposable(@NonNull final Disposable disposable) {
        this.disposables.add(disposable);
    }

    @CallSuper
    @UiThread
    public void onViewAttached(@NonNull final V view, final boolean isFirstTimeAttachment) {
        this.view = view;
    }

    @CallSuper
    @UiThread
    public void onViewDetached() {
        this.view = null;

        RxUtils.resetDisposables(this.disposables);
    }
}
