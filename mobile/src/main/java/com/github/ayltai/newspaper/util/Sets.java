package com.github.ayltai.newspaper.util;

import java.util.Collections;
import java.util.Set;

import android.support.annotation.NonNull;
import android.support.v4.util.ArraySet;

public final class Sets {
    private Sets() {
    }

    @NonNull
    public static <T> Set<T> from(@NonNull final T[] items) {
        final Set<T> set = new ArraySet<>();
        Collections.addAll(set, items);
        return set;
    }
}
