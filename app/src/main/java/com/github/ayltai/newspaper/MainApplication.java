package com.github.ayltai.newspaper;

import android.app.Application;

import com.appsee.Appsee;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.facebook.common.logging.FLog;
import com.github.ayltai.newspaper.setting.Settings;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.fresco.FrescoImageLoader;
import com.optimizely.Optimizely;
import com.optimizely.integration.DefaultOptimizelyEventListener;
import com.squareup.leakcanary.LeakCanary;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public final class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (!LeakCanary.isInAnalyzerProcess(this)) LeakCanary.install(this);

        Fabric.with(this, new Crashlytics(), new Answers());
        Appsee.start(this.getString(R.string.com_appsee_apikey));
        BigImageViewer.initialize(FrescoImageLoader.with(this.getApplicationContext()));
        Realm.init(this);
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder().schemaVersion(BuildConfig.VERSION_CODE).build());

        Optimizely.startOptimizelyAsync(this.getString(R.string.com_optimizely_api_key), this, new DefaultOptimizelyEventListener() {
            @Override
            public void onOptimizelyStarted() {
                if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getName(), "Optimizely started");
            }

            @Override
            public void onOptimizelyFailedToStart(final String errorMessage) {
                LogUtils.getInstance().w(this.getClass().getName(), "Failed to start Optimizely for reason: " + errorMessage);
            }
        });

        if (BuildConfig.DEBUG) {
            FLog.setMinimumLoggingLevel(FLog.WARN);
            Optimizely.enableEditor();
        }

        Appsee.setUserId(Settings.getUserId(this));
    }
}
