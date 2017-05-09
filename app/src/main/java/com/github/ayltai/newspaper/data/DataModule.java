package com.github.ayltai.newspaper.data;

import android.content.Context;
import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;

@Module
public final class DataModule {
    private final Context context;

    public DataModule(@NonNull final Context context) {
        this.context = context;
    }

    @Provides
    Context provideContext() {
        return this.context;
    }

    @Provides
    static Realm provideRealm() {
        return Realm.getDefaultInstance();
    }

    @Provides
    FavoriteManager provideFavoriteManager() {
        return new FavoriteManager(this.context);
    }

    @Provides
    ItemManager provideItemManager(final Realm realm) {
        return new ItemManager(realm);
    }
}
