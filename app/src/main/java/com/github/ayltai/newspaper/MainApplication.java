package com.github.ayltai.newspaper;

import android.app.Application;

import com.appsee.Appsee;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.facebook.common.logging.FLog;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.fresco.FrescoImageLoader;
import com.optimizely.Optimizely;
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
        Optimizely.startOptimizelyWithAPIToken(this.getString(R.string.com_optimizely_api_key), this);
        Appsee.start(this.getString(R.string.com_appsee_apikey));
        BigImageViewer.initialize(FrescoImageLoader.with(this.getApplicationContext()));
        Realm.init(this);
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder().schemaVersion(BuildConfig.VERSION_CODE).build());

        if (BuildConfig.DEBUG) FLog.setMinimumLoggingLevel(FLog.WARN);
    }
}
