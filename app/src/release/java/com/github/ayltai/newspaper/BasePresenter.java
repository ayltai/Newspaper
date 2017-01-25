package com.github.ayltai.newspaper;

import android.support.annotation.VisibleForTesting;

import com.google.firebase.analytics.FirebaseAnalytics;

import com.crashlytics.android.answers.Answers;

public abstract class BasePresenter<V extends Presenter.View> {
    protected V view;

    @VisibleForTesting
    public Answers answers() {
        return Answers.getInstance();
    }

    @VisibleForTesting
    public FirebaseAnalytics analytics() {
        return FirebaseAnalytics.getInstance(this.view.getContext());
    }
}
