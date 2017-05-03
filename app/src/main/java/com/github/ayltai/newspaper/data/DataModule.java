package com.github.ayltai.newspaper.data;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;

@Module
public final class DataModule {
    @Provides
    static Realm provideRealm() {
        return Realm.getDefaultInstance();
    }

    @Provides
    FavoriteManager provideFavoriteManager(final Realm realm) {
        return new FavoriteManager(realm);
    }

    @Provides
    ItemManager provideItemManager(final Realm realm) {
        return new ItemManager(realm);
    }
}
