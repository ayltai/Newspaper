package com.github.ayltai.newspaper.data;

import android.content.Context;
import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.util.DevUtils;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmConfiguration;

@Module
public final class DataModule {
    private static final int SCHEMA_VERSION = 1;

    private static boolean isInitialized;

    private final Context context;

    public DataModule(@NonNull final Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Provides
    Realm provideRealm() {
        if (!DataModule.isInitialized) {
            if (!DevUtils.isRunningUnitTest()) {
                Realm.init(this.context);

                Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                    .schemaVersion(DataModule.SCHEMA_VERSION)
                    .deleteRealmIfMigrationNeeded()
                    .compactOnLaunch()
                    .build());
            }

            DataModule.isInitialized = true;
        }

        return Realm.getDefaultInstance();
    }
}
