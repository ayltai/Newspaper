package com.github.ayltai.newspaper.data;

import java.util.List;

import javax.annotation.Nonnull;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.util.RxUtils;

import io.reactivex.Single;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.Sort;

public final class ItemManager extends DataManager {
    @Nonnull
    @NonNull
    public static Single<ItemManager> create(@Nonnull @NonNull @lombok.NonNull final Context context) {
        return Single.defer(() -> Single.just(ItemManager.create(DaggerDataComponent.builder()
            .dataModule(new DataModule(context))
            .build()
            .realm()))
            .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER)));
    }

    @Nonnull
    @NonNull
    public static ItemManager create(@Nonnull @NonNull @lombok.NonNull final Realm realm) {
        return new ItemManager(realm);
    }

    private ItemManager(@Nonnull @NonNull @lombok.NonNull final Realm realm) {
        super(realm);
    }

    @Nonnull
    @NonNull
    public Single<List<Item>> get(@Nonnull @NonNull @lombok.NonNull final List<String> sourceNames, @Nonnull @NonNull @lombok.NonNull final List<String> categoryNames, @Nullable final String keywords) {
        return Single.defer(() -> Single.just(this.get(this.getRealm().where(Item.class), sourceNames, categoryNames, keywords)));
    }

    private List<Item> get(@Nonnull @NonNull @lombok.NonNull RealmQuery<Item> query, @Nonnull @NonNull @lombok.NonNull final List<String> sourceNames, @Nonnull @NonNull @lombok.NonNull final List<String> categoryNames, @Nullable final String keywords) {
        if (!sourceNames.isEmpty()) query = query.in(Item.FIELD_SOURCE, sourceNames.toArray(new String[0]));
        if (!categoryNames.isEmpty()) query = query.in(Item.FIELD_CATEGORY, categoryNames.toArray(new String[0]));

        if (keywords != null) query = query.beginGroup()
            .contains(Item.FIELD_TITLE, keywords)
            .or()
            .contains(Item.FIELD_DESCRIPTION, keywords)
            .endGroup();

        return this.getRealm().copyFromRealm(query.sort(Item.FIELD_PUBLISH_DATE, Sort.DESCENDING).findAll());
    }
}
