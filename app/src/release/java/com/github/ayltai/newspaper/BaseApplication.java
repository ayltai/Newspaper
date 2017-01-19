package com.github.ayltai.newspaper;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import io.fabric.sdk.android.Fabric;

public abstract class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new Crashlytics(), new Answers());
    }
}
