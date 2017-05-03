package com.github.ayltai.newspaper.data;

import javax.inject.Inject;

import android.support.annotation.NonNull;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Emitter;
import rx.Observable;

public final class FavoriteManager {
    //region Variables

    private final Realm realm;

    //endregion

    @Inject
    public FavoriteManager(@NonNull final Realm realm) {
        this.realm = realm;
    }

    @NonNull
    public Observable<Favorite> getFavorite() {
        if (this.realm.isClosed()) throw new IllegalStateException("Realm is closed");

        return Observable.create(emitter -> {
            final RealmResults<Favorite> favorites = this.realm.where(Favorite.class).findAll();

            emitter.onNext(favorites.isEmpty() ? this.createFromSettings() : favorites.first());
        }, Emitter.BackpressureMode.BUFFER);
    }

    @NonNull
    private Favorite createFromSettings() {
        // TODO
        return null;
    }
}
