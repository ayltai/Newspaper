package com.github.ayltai.newspaper.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.setting.Settings;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import rx.Observable;

public final class FavoriteManager {
    private final Context context;
    private final Realm   realm;

    public FavoriteManager(@NonNull final Context context, @NonNull final Realm realm) {
        this.context = context;
        this.realm   = realm;
    }

    @NonNull
    public Observable<Favorite> getFavorite() {
        if (this.realm.isClosed()) throw new IllegalStateException("Realm is closed");

        return Observable.create(subscriber -> {
            final RealmResults<Favorite> favorites = this.realm.where(Favorite.class).findAll();

            if (favorites.isEmpty()) {
                subscriber.onNext(this.createFromSettings());
            } else {
                subscriber.onNext(this.syncWithSettings(favorites.first()));
            }
        });
    }

    @NonNull
    private Favorite createFromSettings() {
        final String[] urls  = this.context.getResources().getStringArray(R.array.pref_category_values);
        final String[] names = this.context.getResources().getStringArray(R.array.pref_category_entries);

        final Map<String, String> map = new HashMap<>();
        for (int i = 0; i < urls.length; i++) map.put(urls[i], names[i]);

        final RealmList<Source> sources    = new RealmList<>();
        final Set<String>       categories = Settings.getCategories(this.context);

        for (final String url : urls) {
            if (categories.contains(url)) sources.add(new Source(url, map.get(url)));
        }

        sources.add(new Source(Constants.SOURCE_BOOKMARK, this.context.getString(R.string.action_bookmark)));

        final Favorite favorite = new Favorite(sources);
        final Feed     feed     = new Feed(Constants.SOURCE_BOOKMARK, new RealmList<>());

        this.realm.beginTransaction();
        this.realm.copyToRealmOrUpdate(favorite);
        this.realm.copyToRealmOrUpdate(feed);
        this.realm.commitTransaction();

        return favorite;
    }

    @NonNull
    private Favorite syncWithSettings(@NonNull final Favorite favorite) {
        final List<Source> sources    = new ArrayList<>();
        final Set<String>  categories = Settings.getCategories(this.context);

        for (final Source source : favorite.getSources()) {
            if (Constants.SOURCE_BOOKMARK.equals(source.getUrl())) continue;

            if (!categories.contains(source.getUrl())) sources.add(source);
        }

        final Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();

        favorite.getSources().removeAll(sources);
        realm.copyToRealmOrUpdate(favorite);

        realm.commitTransaction();
        realm.close();

        return favorite;
    }
}
