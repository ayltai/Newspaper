package com.github.ayltai.newspaper.data;

import javax.inject.Inject;

import android.content.Context;
import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.model.SourceFactory;
import com.github.ayltai.newspaper.setting.Settings;

import io.realm.RealmList;
import rx.Emitter;
import rx.Observable;

public final class FavoriteManager {
    private final Context context;

    @Inject
    public FavoriteManager(@NonNull final Context context) {
        this.context = context;
    }

    @NonNull
    public Observable<Favorite> getFavorite() {
        return Observable.create(emitter -> emitter.onNext(this.createFromSettings()), Emitter.BackpressureMode.BUFFER);
    }

    @NonNull
    private Favorite createFromSettings() {
        final Favorite favorite = new Favorite(new RealmList<>());

        for (final String source : Settings.getSources(this.context)) favorite.getSources().add(SourceFactory.getInstance(this.context).getSource(source));

        return favorite;
    }
}
