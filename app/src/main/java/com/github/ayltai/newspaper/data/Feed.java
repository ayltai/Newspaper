package com.github.ayltai.newspaper.data;

import java.util.ArrayList;
import java.util.List;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.rss.Item;
import com.github.ayltai.newspaper.util.ItemUtils;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class Feed extends RealmObject {
    static final String FIELD_URL = "url";

    @Ignore
    private final List<String> images = new ArrayList<>();

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

    @NonNull
    public final List<String> getImages() {
        return this.images;
    }

    //endregion

    public final void updateImages() {
        this.images.clear();

        for (final Item item : this.items) {
            if (item.getMediaUrl() != null && ItemUtils.hasOriginalMediaUrl(item.getMediaUrl())) this.images.add(ItemUtils.getOriginalMediaUrl(item.getMediaUrl()));
        }
    }

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

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) return true;

        if (obj instanceof Feed) {
            final Feed feed = (Feed)obj;

            if (this.url.equals(feed.url) && this.items.size() == feed.items.size()) {
                for (int i = 0; i < this.items.size(); i++) {
                    if (!this.items.get(i).equals(feed.items.get(i))) return false;
                }

                return true;
            }
        }

        return false;

    }

    @SuppressWarnings("checkstyle:magicnumber")
    @Override
    public final int hashCode() {
        return 31 * this.url.hashCode() + this.items.hashCode();
    }
}
