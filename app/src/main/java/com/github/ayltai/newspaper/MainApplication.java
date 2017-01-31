package com.github.ayltai.newspaper;

import com.github.ayltai.newspaper.util.TestUtils;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.fresco.FrescoImageLoader;

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
    }
}
