package com.github.ayltai.newspaper.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.rss.Item;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Feed extends RealmObject {
    static final String FIELD_URL = "url";

    //region Fields

    @PrimaryKey
    private String          url;
    private RealmList<Item> items;

    //endregion

    //region Constructors

    public Feed() {
    }

    public Feed(@NonNull final String url, @NonNull final RealmList<Item> items) {
        this.url   = url;
        this.items = items;
    }

    //endregion

    //region Properties

    @NonNull
    public final String getUrl() {
        return this.url;
    }

    @NonNull
    public final RealmList<Item> getItems() {
        return this.items;
    }

    //endregion

    public final boolean contains(@Nullable final Item item) {
        if (item == null) return false;

        for (final Item i : this.items) {
            if (i.getGuid() != null && item.getGuid() != null && i.getGuid().equals(item.getGuid())) return true;
            if (item.equals(i)) return true;
        }

        return false;
    }

    public final int indexOf(@Nullable final Item item) {
        if (item == null) return -1;

        for (int i = 0; i < this.items.size(); i++) {
            final Item j = this.items.get(i);

            if (j.getGuid() != null && item.getGuid() != null && j.getGuid().equals(item.getGuid())) return i;
            if (item.equals(j)) return i;
        }

        return -1;
    }
}
