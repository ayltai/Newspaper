package com.github.ayltai.newspaper.item;

import java.util.List;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.model.Item;

public final class ItemsUpdatedEvent {
    private final List<Item> items;

    public ItemsUpdatedEvent(@NonNull final List<Item> items) {
        this.items = items;
    }

    public List<Item> getItems() {
        return this.items;
    }
}
