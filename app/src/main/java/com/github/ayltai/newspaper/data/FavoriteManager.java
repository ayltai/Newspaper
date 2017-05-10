package com.github.ayltai.newspaper.data;

import javax.inject.Inject;

import android.content.Context;
import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.model.SourceFactory;
import com.github.ayltai.newspaper.setting.Settings;

import io.reactivex.Single;
import io.realm.RealmList;

public final class FavoriteManager {
    private final Context context;

    @Inject
    public FavoriteManager(@NonNull final Context context) {
        this.context = context;
    }

    @NonNull
    public Single<Favorite> getFavorite() {
        return Single.create(emitter -> emitter.onSuccess(this.createFromSettings()));
    }

    @NonNull
    private Favorite createFromSettings() {
        final Favorite favorite = new Favorite(new RealmList<>());

        for (final String source : Settings.getSources(this.context)) favorite.getSources().add(SourceFactory.getInstance(this.context).getSource(source));

        return favorite;
    }
}
