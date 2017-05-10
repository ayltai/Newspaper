package com.github.ayltai.newspaper.data;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.model.Item;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Emitter;
import rx.Observable;

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
                final List<String> instantCategories = new ArrayList<>(categories.size());
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
    public Observable<List<Item>> getItemsObservable(@NonNull final List<String> sources, @NonNull final List<String> categories) {
        if (this.realm.isClosed()) throw new IllegalStateException(ItemManager.ERROR_REALM);

        return Observable.create(emitter -> {
            final List<Item> items = this.getItems(sources, categories);

            emitter.onNext(items.isEmpty() ? new ArrayList<>() : items);
        }, Emitter.BackpressureMode.BUFFER);
    }
}
