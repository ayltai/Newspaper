package com.github.ayltai.newspaper.data;

import java.util.List;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.util.Irrelevant;

import io.reactivex.Single;
import io.realm.Realm;
import io.realm.RealmQuery;

public final class ItemManager extends DataManager {
    public ItemManager(@NonNull final Realm realm) {
        super(realm);
    }

    @NonNull
    public Single<List<Item>> getItems(@Nullable final String[] sources, @Nullable final String category) {
        return Single.create(emitter -> {
            final RealmQuery<Item> query = this.getRealm().where(Item.class);

            if (sources != null) query.in(Item.FIELD_SOURCE, sources);
            if (!TextUtils.isEmpty(category)) query.equalTo(Item.FIELD_CATEGORY, category);

            emitter.onSuccess(query.findAll());
        });
    }

    @NonNull
    public Single<Irrelevant> putItems(@NonNull final List<Item> items) {
        return Single.create(emitter -> {
            this.getRealm().beginTransaction();
            this.getRealm().insertOrUpdate(items);
            this.getRealm().commitTransaction();
        });
    }
}
