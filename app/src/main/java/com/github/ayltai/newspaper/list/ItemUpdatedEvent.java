package com.github.ayltai.newspaper.list;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.rss.Item;

public final class ItemUpdatedEvent {
    private final int  index;
    private final Item item;

    public ItemUpdatedEvent(final int index, @NonNull final Item item) {
        this.index = index;
        this.item  = item;
    }

    public int getIndex() {
        return this.index;
    }

    public Item getItem() {
        return this.item;
    }
}
