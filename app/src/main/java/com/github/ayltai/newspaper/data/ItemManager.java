package com.github.ayltai.newspaper.data;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.util.LogUtils;

import io.reactivex.Single;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class ItemManager {
    private static final String ERROR_REALM = "Realm is closed";

    //region Variables

    private final Realm realm;

    //endregion

    @Inject
    public ItemManager(@NonNull final Realm realm) {
        this.realm = realm;
    }

    @Nullable
    public Item getItem(@NonNull final String link) {
        final RealmResults<Item> items = this.realm.where(Item.class).equalTo(Item.FIELD_LINK, link).findAll();

        return items.isEmpty() ? null : items.first();
    }

    @NonNull
    public List<Item> getItems(@NonNull final List<String> sources, @NonNull final List<String> categories) {
        RealmQuery<Item> query = this.realm.where(Item.class);

        if (!categories.isEmpty()) {
            if (categories.size() == 1 && Constants.CATEGORY_BOOKMARK.equals(categories.get(0))) {
                query = query.equalTo(Item.FIELD_BOOKMARKED, true);
            } else {
                final List<String> instantCategories = new ArrayList<>(categories.size() * 2);
                for (final String category : categories) instantCategories.add(Constants.CATEGORY_INSTANT + category);
                instantCategories.addAll(categories);

                query = query.in(Item.FIELD_CATEGORY, instantCategories.toArray(new String[instantCategories.size()]));
            }
        }

        if (!sources.isEmpty()) query = query.in(Item.FIELD_SOURCE, sources.toArray(new String[sources.size()]));

        final RealmResults<Item> items = query.findAllSorted(Item.FIELD_PUBLISH_DATE, Sort.DESCENDING);

        return items.isEmpty() ? new ArrayList<>() : items;
    }

    @NonNull
    public Single<List<Item>> getItemsSingle(@NonNull final List<String> sources, @NonNull final List<String> categories) {
        if (this.realm.isClosed()) throw new IllegalStateException(ItemManager.ERROR_REALM);

        return Single.create(emitter -> {
            final Runnable runnable = () -> {
                final List<Item> items = this.getItems(sources, categories);

                emitter.onSuccess(items.isEmpty() ? new ArrayList<>() : items);
            };

            this.deleteItems(Constants.HOUSEKEEP_TIME,
                runnable::run,
                e -> runnable.run());
        });
    }

    private void deleteItems(final long housekeepTime, @Nullable final Realm.Transaction.OnSuccess onSuccess, @Nullable final Realm.Transaction.OnError onError) {
        this.realm.executeTransactionAsync(realm -> realm.where(Item.class)
            .notEqualTo(Item.FIELD_BOOKMARKED, true)
            .lessThan(Item.FIELD_PUBLISH_DATE, System.currentTimeMillis() - housekeepTime)
            .findAll()
            .deleteAllFromRealm(),
            onSuccess,
            e -> {
                LogUtils.getInstance().w(this.getClass().getSimpleName(), e.getMessage(), e);

                if (onError != null) onError.onError(e);
            });
    }
}
