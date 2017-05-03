package com.github.ayltai.newspaper.item;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.model.Item;

public final class ItemUpdatedEvent {
    //region Variables

    private final int  index;
    private final Item item;

    //endregion

    public ItemUpdatedEvent(final int index, @NonNull final Item item) {
        this.index = index;
        this.item  = item;
    }

    //region Properties

    public int getIndex() {
        return this.index;
    }

    public Item getItem() {
        return this.item;
    }

    //endregion
}
