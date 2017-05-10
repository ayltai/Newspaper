package com.github.ayltai.newspaper;

import com.google.firebase.analytics.FirebaseAnalytics;

import com.crashlytics.android.answers.Answers;

public abstract class BasePresenter<V extends Presenter.View> {
    protected V view;

    public Answers answers() {
        return Answers.getInstance();
    }

    public FirebaseAnalytics analytics() {
        return FirebaseAnalytics.getInstance(this.view.getContext());
    }
}
