package com.github.ayltai.newspaper.app.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.data.DaggerDataComponent;
import com.github.ayltai.newspaper.data.DataManager;
import com.github.ayltai.newspaper.data.DataModule;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.RxUtils;

import io.reactivex.Single;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public final class ItemManager extends DataManager {
    @NonNull
    public static Single<ItemManager> create(@NonNull final Context context) {
        return Single.<Realm>create(emitter -> emitter.onSuccess(DaggerDataComponent.builder()
            .dataModule(new DataModule(context))
            .build()
            .realm()))
            .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER))
            .map(ItemManager::create);
    }

    @NonNull
    public static ItemManager create(@NonNull final Realm realm) {
        return new ItemManager(realm);
    }

    private ItemManager(@NonNull final Realm realm) {
        super(realm);
    }

    @NonNull
    public Single<List<NewsItem>> getItems(@NonNull final String[] sources, @NonNull final String[] categories) {
        return this.getItems(null, sources, categories);
    }

    @NonNull
    public Single<List<NewsItem>> getItems(@Nullable final CharSequence searchText, @NonNull final String[] sources, @NonNull final String[] categories) {
        return Single.create(emitter -> {
            if (!this.getRealm().isInTransaction()) this.getRealm().beginTransaction();
            this.clearObsoleteItems();
            this.getRealm().commitTransaction();

            final RealmQuery<NewsItem> query = this.getRealm()
                .where(NewsItem.class)
                .in(NewsItem.FIELD_SOURCE, sources)
                .in(NewsItem.FIELD_CATEGORY, categories);

            if (!TextUtils.isEmpty(searchText)) query.beginGroup()
                .contains(NewsItem.FIELD_TITLE, searchText.toString(), Case.INSENSITIVE)
                .or()
                .contains(NewsItem.FIELD_DESCRIPTION, searchText.toString(), Case.INSENSITIVE)
                .endGroup();

            if (!emitter.isDisposed()) emitter.onSuccess(this.getRealm().copyFromRealm(query.findAll()));
        });
    }

    @NonNull
    public Single<List<NewsItem>> getHistoricalItems(@NonNull final String[] sources, @NonNull final String[] categories) {
        return this.getHistoricalItems(null, sources, categories);
    }

    @NonNull
    public Single<List<NewsItem>> getHistoricalItems(@Nullable final CharSequence searchText, @NonNull final String[] sources, @NonNull final String[] categories) {
        return Single.create(emitter -> {
            final RealmQuery<NewsItem> query = this.getRealm()
                .where(NewsItem.class)
                .greaterThan(NewsItem.FIELD_LAST_ACCESSED_DATE, 0)
                .in(NewsItem.FIELD_SOURCE, sources)
                .in(NewsItem.FIELD_CATEGORY, categories);

            if (!TextUtils.isEmpty(searchText)) query.beginGroup()
                .contains(NewsItem.FIELD_TITLE, searchText.toString(), Case.INSENSITIVE)
                .or()
                .contains(NewsItem.FIELD_DESCRIPTION, searchText.toString(), Case.INSENSITIVE)
                .endGroup();

            if (!emitter.isDisposed()) emitter.onSuccess(this.getRealm().copyFromRealm(query.findAllSorted(NewsItem.FIELD_LAST_ACCESSED_DATE, Sort.DESCENDING)));
        });
    }

    @NonNull
    public Single<List<NewsItem>> getBookmarkedItems(@NonNull final String[] sources, @NonNull final String[] categories) {
        return this.getBookmarkedItems(null, sources, categories);
    }

    @NonNull
    public Single<List<NewsItem>> getBookmarkedItems(@Nullable final CharSequence searchText, @NonNull final String[] sources, @NonNull final String[] categories) {
        return Single.create(emitter -> {
            final RealmQuery<NewsItem> query = this.getRealm()
                .where(NewsItem.class)
                .equalTo(NewsItem.FIELD_BOOKMARKED, true)
                .in(NewsItem.FIELD_SOURCE, sources)
                .in(NewsItem.FIELD_CATEGORY, categories);

            if (!TextUtils.isEmpty(searchText)) query.beginGroup()
                .contains(NewsItem.FIELD_TITLE, searchText.toString(), Case.INSENSITIVE)
                .or()
                .contains(NewsItem.FIELD_DESCRIPTION, searchText.toString(), Case.INSENSITIVE)
                .endGroup();

            if (!emitter.isDisposed()) emitter.onSuccess(this.getRealm().copyFromRealm(query.findAllSorted(NewsItem.FIELD_LAST_ACCESSED_DATE, Sort.DESCENDING)));
        });
    }

    @SuppressWarnings("CyclomaticComplexity")
    @NonNull
    public Single<List<NewsItem>> putItems(@NonNull final List<NewsItem> newsItems) {
        return Single.create(emitter -> {
            if (!this.getRealm().isInTransaction()) this.getRealm().beginTransaction();

            final List<NewsItem> newItems = new ArrayList<>();

            for (final NewsItem newsItem : newsItems) {
                final RealmResults<NewsItem> items = this.getRealm()
                    .where(NewsItem.class)
                    .equalTo(NewsItem.FIELD_LINK, newsItem.getLink())
                    .findAll();

                if (items.isEmpty()) {
                    this.getRealm().insert(newsItem);

                    newItems.add(RealmObject.isManaged(newsItem) ? this.getRealm().copyFromRealm(newsItem) : newsItem);
                } else {
                    final NewsItem item = items.first();

                    if (item.isFullDescription()) {
                        if (newsItem.isFullDescription()) {
                            final Date newsItemLastAccessedDate = newsItem.getLastAccessedDate();
                            final Date itemLastAccessedDate     = item.getLastAccessedDate();

                            if (newsItemLastAccessedDate != null && itemLastAccessedDate != null && newsItemLastAccessedDate.getTime() > itemLastAccessedDate.getTime()) item.setLastAccessedDate(newsItem.getLastAccessedDate());
                            item.setBookmarked(newsItem.isBookmarked());

                            this.getRealm().insertOrUpdate(item);
                        }

                        newItems.add(RealmObject.isManaged(item) ? this.getRealm().copyFromRealm(item) : item);
                    } else {
                        this.getRealm().insertOrUpdate(newsItem);

                        newItems.add(RealmObject.isManaged(newsItem) ? this.getRealm().copyFromRealm(newsItem) : newsItem);
                    }
                }
            }

            if (this.getRealm().isInTransaction()) this.getRealm().commitTransaction();

            if (!emitter.isDisposed()) emitter.onSuccess(newItems);
        });
    }

    @NonNull
    public Single<Irrelevant> clearHistories() {
        return Single.create(emitter -> {
            if (!this.getRealm().isInTransaction()) this.getRealm().beginTransaction();

            final RealmResults<NewsItem> items = this.getRealm()
                .where(NewsItem.class)
                .greaterThan(NewsItem.FIELD_LAST_ACCESSED_DATE, 0)
                .findAll();

            for (final NewsItem item : items) item.setLastAccessedDate(null);

            this.getRealm().insertOrUpdate(items);

            this.clearObsoleteItems();

            if (this.getRealm().isInTransaction()) this.getRealm().commitTransaction();

            if (!emitter.isDisposed()) emitter.onSuccess(Irrelevant.INSTANCE);
        });
    }

    @NonNull
    public Single<Irrelevant> clearBookmarks() {
        return Single.create(emitter -> {
            if (!this.getRealm().isInTransaction()) this.getRealm().beginTransaction();

            final RealmResults<NewsItem> items = this.getRealm()
                .where(NewsItem.class)
                .equalTo(NewsItem.FIELD_BOOKMARKED, true)
                .findAll();

            for (final NewsItem item : items) item.setBookmarked(false);

            this.getRealm().insertOrUpdate(items);

            this.clearObsoleteItems();

            if (this.getRealm().isInTransaction()) this.getRealm().commitTransaction();

            if (!emitter.isDisposed()) emitter.onSuccess(Irrelevant.INSTANCE);
        });
    }

    private void clearObsoleteItems() {
        this.getRealm()
            .where(NewsItem.class)
            .lessThan(NewsItem.FIELD_PUBLISH_DATE, System.currentTimeMillis() - Constants.HOUSEKEEP_TIME)
            .beginGroup()
            .notEqualTo(NewsItem.FIELD_BOOKMARKED, true)
            .or()
            .equalTo(NewsItem.FIELD_LAST_ACCESSED_DATE, 0)
            .endGroup()
            .findAll()
            .deleteAllFromRealm();
    }
}
