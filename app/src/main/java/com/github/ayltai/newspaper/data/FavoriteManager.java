package com.github.ayltai.newspaper.data;

import javax.inject.Inject;

import android.content.Context;
import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.model.SourceFactory;
import com.github.ayltai.newspaper.setting.Settings;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import rx.Emitter;
import rx.Observable;

public final class FavoriteManager {
    //region Variables

    private final Context context;
    private final Realm   realm;

    //endregion

    @Inject
    public FavoriteManager(@NonNull final Context context, @NonNull final Realm realm) {
        this.context = context;
        this.realm   = realm;
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
        final Favorite favorite = new Favorite(new RealmList<>());

        for (final String source : Settings.getSources(this.context)) favorite.getSources().add(SourceFactory.getInstance(this.context).getSource(source));

        return favorite;
    }
}
