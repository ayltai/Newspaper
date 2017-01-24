package com.github.ayltai.newspaper;

import android.app.Application;

import com.facebook.common.logging.FLog;
import com.facebook.stetho.Stetho;
import com.github.ayltai.newspaper.util.TestUtils;
import com.squareup.leakcanary.LeakCanary;

public abstract class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (!TestUtils.isRunningTest()) {
            if (!LeakCanary.isInAnalyzerProcess(this)) LeakCanary.install(this);

            FLog.setMinimumLoggingLevel(FLog.WARN);
            Stetho.initializeWithDefaults(this);
        }
    }
}
