package com.github.ayltai.newspaper;

import android.app.Application;

import com.facebook.common.logging.FLog;
import com.facebook.stetho.Stetho;
import com.optimizely.Optimizely;
import com.squareup.leakcanary.LeakCanary;

public abstract class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (!LeakCanary.isInAnalyzerProcess(this)) LeakCanary.install(this);

        FLog.setMinimumLoggingLevel(FLog.WARN);
        Optimizely.enableEditor();
        Stetho.initializeWithDefaults(this);
    }
}
