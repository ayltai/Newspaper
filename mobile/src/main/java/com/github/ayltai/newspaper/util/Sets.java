package com.github.ayltai.newspaper.util;

import java.util.Collections;
import java.util.Set;

import android.support.annotation.NonNull;

import gnu.trove.set.hash.THashSet;

public final class Sets {
    private Sets() {
    }

    @NonNull
    public static <T> Set<T> from(@NonNull final T[] items) {
        final Set<T> set = new THashSet<>();
        Collections.addAll(set, items);
        return set;
    }
}
