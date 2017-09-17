package com.github.ayltai.newspaper.util;

import java.util.ArrayList;
import java.util.List;

import android.support.annotation.NonNull;

import com.squareup.haha.guava.base.Function;

public final class Lists {
    private Lists() {
    }

    /**
     * Returns a list that applies {@code function} to each element of {@code fromList}.
     * <p>The returned list is a transformed view of {@code fromList}; changes to {@code fromList} will be reflected in the returned list and vice versa.</p>
     * @param fromList The list of elements to be transformed.
     * @param function The transformation to apply to each element in {@code fromList}.
     * @return A list that applies {@code function} to each element of {@code fromList}.
     */
    public static <F, T> List<T> transform(@NonNull final List<F> fromList, Function<? super F, ? extends T> function) {
        final List<T> toList = new ArrayList<>();

        for (final F element : fromList) toList.add(function.apply(element));

        return toList;
    }
}
