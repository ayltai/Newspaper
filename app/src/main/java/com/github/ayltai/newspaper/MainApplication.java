package com.github.ayltai.newspaper;

import com.github.ayltai.newspaper.util.ImageUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public final class MainApplication extends BaseApplication {
    private static final int DATABASE_SCHEMA_UPGRADE_11 = 11;
    private static final int DATABASE_SCHEMA_UPGRADE_12 = 12;

    private static final String TABLE_ITEM = "Item";

    @Override
    public void onCreate() {
        super.onCreate();

        ImageUtils.initFresco(this);

        if (!TestUtils.isRunningUnitTest()) {
            Realm.init(this);
            Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                .migration((realm, oldVersion, newVersion) -> {
                    if (oldVersion == MainApplication.DATABASE_SCHEMA_UPGRADE_11) {
                        realm.getSchema().get(MainApplication.TABLE_ITEM).addField("isFullDescription", boolean.class);
                    } else if (oldVersion == MainApplication.DATABASE_SCHEMA_UPGRADE_12) {
                        realm.getSchema().get(MainApplication.TABLE_ITEM).removeField("guid");
                    }
                })
                .schemaVersion(BuildConfig.VERSION_CODE)
                .build());
        }
    }
}
