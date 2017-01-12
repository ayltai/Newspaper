package com.github.ayltai.newspaper;

import android.app.Application;

import com.facebook.common.logging.FLog;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.fresco.FrescoImageLoader;
import com.squareup.leakcanary.LeakCanary;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public final class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (!LeakCanary.isInAnalyzerProcess(this)) LeakCanary.install(this);

        Realm.init(this);
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
            .schemaVersion(BuildConfig.VERSION_CODE)
            .build());

        BigImageViewer.initialize(FrescoImageLoader.with(this.getApplicationContext()));

        if (BuildConfig.DEBUG) FLog.setMinimumLoggingLevel(FLog.WARN);
    }
}
