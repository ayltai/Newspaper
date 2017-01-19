package com.github.ayltai.newspaper;

import android.support.annotation.VisibleForTesting;

import com.crashlytics.android.answers.Answers;

public abstract class BasePresenter {
    @VisibleForTesting
    public Answers answers() {
        return Answers.getInstance();
    }
}
