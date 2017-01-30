package com.github.ayltai.newspaper.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

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

    @Inject
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
                subscriber.onNext(this.syncWithSettings(this.syncWithSettings(favorites.first())));
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
        final List<Source> oldSources = new ArrayList<>();
        final List<Source> newSources = new ArrayList<>();
        final Set<String>  categories = Settings.getCategories(this.context);

        for (final Source source : favorite.getSources()) {
            if (Constants.SOURCE_BOOKMARK.equals(source.getUrl())) continue;

            if (!categories.contains(source.getUrl())) oldSources.add(source);
        }

        final String[]     names = this.context.getResources().getStringArray(R.array.pref_category_entries);
        final List<String> urls  = Arrays.asList(this.context.getResources().getStringArray(R.array.pref_category_values));

        for (final String category : categories) {
            if (!favorite.contains(category)) {
                final int index = urls.indexOf(category);
                newSources.add(new Source(urls.get(index), names[index]));
            }
        }

        this.realm.beginTransaction();

        favorite.getSources().removeAll(oldSources);
        favorite.getSources().addAll(newSources);

        FavoriteManager.sort(urls, favorite);
        this.localizeNames(urls, names, favorite);

        this.realm.copyToRealmOrUpdate(favorite);
        this.realm.commitTransaction();

        return favorite;
    }

    private void localizeNames(@NonNull final List<String> urls, @NonNull final String[] names, @NonNull final Favorite favorite) {
        for (int i = 0; i < favorite.getSources().size(); i++) {
            if (Constants.SOURCE_BOOKMARK.equals(favorite.getSources().get(i).getName())) {
                favorite.getSources().get(i).setName(this.context.getString(R.string.action_bookmark));
            } else {
                final int index = urls.indexOf(favorite.getSources().get(i).getUrl());

                if (index > -1) favorite.getSources().get(i).setName(names[index]);
            }
        }
    }

    private static void sort(@NonNull final List<String> urls, @NonNull final Favorite favorite) {
        Collections.sort(favorite.getSources(), (s1, s2) -> {
            if (Constants.SOURCE_BOOKMARK.equals(s1.getUrl())) return 1;
            if (Constants.SOURCE_BOOKMARK.equals(s2.getUrl())) return 1;

            return urls.indexOf(s1.getUrl()) - urls.indexOf(s2.getUrl());
        });

        for (int i = 0; i < favorite.getSources().size(); i++) {
            if (Constants.SOURCE_BOOKMARK.equals(favorite.getSources().get(i).getUrl())) {
                favorite.getSources().add(favorite.getSources().remove(i));
                break;
            }
        }
    }
}
