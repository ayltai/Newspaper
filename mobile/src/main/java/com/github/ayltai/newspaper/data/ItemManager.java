package com.github.ayltai.newspaper.data;

import java.util.ArrayList;
import java.util.List;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.data.model.NewsItem;

import io.reactivex.Single;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

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
    public Single<List<NewsItem>> putItems(@NonNull final List<NewsItem> newsItems) {
        return Single.create(emitter -> {
            this.getRealm().beginTransaction();

            final List<NewsItem> newItems = new ArrayList<>();

            for (final NewsItem newsItem : newsItems) {
                final RealmResults<NewsItem> items = this.getRealm().where(NewsItem.class)
                    .equalTo(NewsItem.FIELD_LINK, newsItem.getLink())
                    .findAll();

                if (items.isEmpty()) {
                    this.getRealm().insert(newsItem);

                    newItems.add(RealmObject.isManaged(newsItem) ? this.getRealm().copyFromRealm(newsItem) : newsItem);
                } else {
                    final NewsItem item = items.first();

                    if (item.isFullDescription()) {
                        newItems.add(this.getRealm().copyFromRealm(item));
                    } else {
                        this.getRealm().insertOrUpdate(newsItem);

                        newItems.add(RealmObject.isManaged(newsItem) ? this.getRealm().copyFromRealm(newsItem) : newsItem);
                    }
                }
            }

            this.getRealm().commitTransaction();

            emitter.onSuccess(newItems);
        });
    }
}
