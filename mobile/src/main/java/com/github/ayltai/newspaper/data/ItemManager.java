package com.github.ayltai.newspaper.data;

import java.util.List;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.data.model.NewsItem;
import com.github.ayltai.newspaper.util.Irrelevant;

import io.reactivex.Single;
import io.realm.Realm;
import io.realm.RealmQuery;

public final class ItemManager extends DataManager {
    public ItemManager(@NonNull final Realm realm) {
        super(realm);
    }

    @NonNull
    public Single<List<NewsItem>> getItems(@Nullable final String[] sources, @Nullable final String[] categories) {
        return Single.create(emitter -> {
            final RealmQuery<NewsItem> query = this.getRealm().where(NewsItem.class);

            if (sources != null) query.in(NewsItem.FIELD_SOURCE, sources);
            if (categories != null) query.in(NewsItem.FIELD_CATEGORY, categories);

            emitter.onSuccess(query.findAll());
        });
    }

    @NonNull
    public Single<Irrelevant> putItems(@NonNull final List<NewsItem> items) {
        return Single.create(emitter -> {
            this.getRealm().beginTransaction();
            this.getRealm().insertOrUpdate(items);
            this.getRealm().commitTransaction();
        });
    }
}
