package com.github.ayltai.newspaper;

import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.model.Source;
import com.github.ayltai.newspaper.model.Video;
import com.github.ayltai.newspaper.util.ImageUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObjectSchema;

public final class MainApplication extends BaseApplication {
    private static final int VERSION_14 = 14;

    @Override
    public void onCreate() {
        super.onCreate();

        ImageUtils.initFresco(this);

        if (!TestUtils.isRunningUnitTest()) {
            Realm.init(this);
            Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                .migration((realm, oldVersion, newVersion) -> {
                    if (oldVersion < MainApplication.VERSION_14) {
                        final RealmObjectSchema itemSchema = realm.getSchema().get(Item.class.getSimpleName());

                        if (!itemSchema.hasField(Item.FIELD_VIDEO)) itemSchema.addRealmObjectField(Item.FIELD_VIDEO, realm.getSchema().create(Video.class.getSimpleName()));

                        final RealmObjectSchema sourceSchema = realm.getSchema().get(Source.class.getSimpleName());
                        if (!sourceSchema.hasIndex(Source.FIELD_NAME)) sourceSchema.addPrimaryKey(Source.FIELD_NAME);
                    }
                })
                .schemaVersion(BuildConfig.VERSION_CODE)
                .build());
        }
    }
}
