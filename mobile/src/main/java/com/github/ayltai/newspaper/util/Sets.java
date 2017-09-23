package com.github.ayltai.newspaper.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import android.support.annotation.NonNull;

public final class Sets {
    private Sets() {
    }

    @NonNull
    public static <T> Set<T> from(@NonNull final T[] items) {
        final Set<T> set = new HashSet<>();
        Collections.addAll(set, items);
        return set;
    }
}
