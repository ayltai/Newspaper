package com.github.ayltai.newspaper.view;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import com.github.ayltai.newspaper.util.Irrelevant;

import io.reactivex.Flowable;

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

    private V view;

    @Nullable
    public V getView() {
        return this.view;
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
    }
}
