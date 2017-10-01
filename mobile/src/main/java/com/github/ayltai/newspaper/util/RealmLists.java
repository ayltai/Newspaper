package com.github.ayltai.newspaper.util;

import android.support.annotation.NonNull;

import io.realm.RealmList;
import io.realm.RealmObject;

public final class RealmLists {
    private RealmLists() {
    }

    @NonNull
    public static <T extends RealmObject> String toString(@NonNull final RealmList<T> items) {
        final StringBuilder builder = new StringBuilder();

        for (final T item : items) {
            if (builder.length() > 0) builder.append(", ");
            builder.append(item.toString());
        }

        return builder.toString();
    }
}
