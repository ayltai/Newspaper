package com.github.ayltai.newspaper;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.google.firebase.analytics.FirebaseAnalytics;

import com.crashlytics.android.answers.Answers;
import com.github.ayltai.newspaper.util.LogUtils;

import rx.Observable;

public abstract class Presenter<V extends Presenter.View> {
    public interface View {
        @Nullable
        Observable<Void> attachments();

        @Nullable
        Observable<Void> detachments();

        @NonNull
        Context getContext();
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

    @VisibleForTesting
    public LogUtils log() {
        return LogUtils.getInstance();
    }

    @VisibleForTesting
    public Answers answers() {
        return Answers.getInstance();
    }

    @VisibleForTesting
    public FirebaseAnalytics analytics() {
        return FirebaseAnalytics.getInstance(this.view.getContext());
    }
}
