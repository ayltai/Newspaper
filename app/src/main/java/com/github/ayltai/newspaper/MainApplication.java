package com.github.ayltai.newspaper;

import com.github.ayltai.newspaper.util.LogUtils;
import com.github.ayltai.newspaper.util.TestUtils;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.fresco.FrescoImageLoader;
import com.optimizely.Optimizely;
import com.optimizely.integration.DefaultOptimizelyEventListener;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public final class MainApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        BigImageViewer.initialize(FrescoImageLoader.with(this.getApplicationContext()));

        if (!TestUtils.isRunningUnitTest()) {
            Realm.init(this);
            Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                .migration((realm, oldVersion, newVersion) -> { })
                .schemaVersion(BuildConfig.VERSION_CODE).build());
        }

        if (!TestUtils.isRunningInstrumentalTest()) {
            Optimizely.startOptimizelyAsync(this.getString(R.string.com_optimizely_api_key), this, new DefaultOptimizelyEventListener() {
                @Override
                public void onOptimizelyStarted() {
                    if (BuildConfig.DEBUG)
                        LogUtils.getInstance().d(this.getClass().getSimpleName(), "Optimizely started");
                }

                @Override
                public void onOptimizelyFailedToStart(final String errorMessage) {
                    LogUtils.getInstance().w(this.getClass().getSimpleName(), "Failed to start Optimizely for reason: " + errorMessage);
                }
            });
        }
    }
}
