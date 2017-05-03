package com.github.ayltai.newspaper;

import com.github.ayltai.newspaper.util.ImageUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public final class MainApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        ImageUtils.initFresco(this);

        if (!TestUtils.isRunningUnitTest()) {
            Realm.init(this);
            Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                .migration((realm, oldVersion, newVersion) -> {
                    // Does nothing
                })
                .schemaVersion(BuildConfig.VERSION_CODE)
                .build());
        }
    }
}
