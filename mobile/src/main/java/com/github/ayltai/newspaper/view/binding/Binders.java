package com.github.ayltai.newspaper.view.binding;

import java.util.ArrayList;
import java.util.Collection;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

public final class Binders {
    private Binders() {
    }

    @NonNull
    public static <M, V> Collection<Pair<PartBinderFactory<M, V>, Binder<V>>> createBinders(@NonNull final Iterable<M> models, @NonNull final Iterable<FullBinderFactory<M>> factories) {
        final Collection<Pair<PartBinderFactory<M, V>, Binder<V>>> list = new ArrayList<>();

        for (final M model : models) list.addAll(Binders.create(model, Binders.simplify(model, factories)));

        return list;
    }

    private static <M, V> Iterable<PartBinderFactory<M, V>> simplify(@Nullable final M model, @NonNull final Iterable<FullBinderFactory<M>> factories) {
        final Collection<PartBinderFactory<M, V>> list = new ArrayList<>();

        for (final BinderFactory<M> factory : factories) list.addAll(Binders.simplify(model, factory));

        return list;
    }

    private static <M, V> Collection<PartBinderFactory<M, V>> simplify(@Nullable final M model, @NonNull final BinderFactory<M> factory) {
        final Collection<PartBinderFactory<M, V>> list = new ArrayList<>();

        if (factory.isNeeded(model)) {
            if (factory instanceof FullBinderFactory) {
                for (final BinderFactory<M> child : ((FullBinderFactory<M>)factory).getParts(model)) list.addAll(Binders.simplify(model, child));
            } else {
                list.add((PartBinderFactory<M, V>)factory);
            }
        }

        return list;
    }

    private static <M, V> Collection<Pair<PartBinderFactory<M, V>, Binder<V>>> create(@Nullable final M model, @NonNull final Iterable<PartBinderFactory<M, V>> factories) {
        final Collection<Pair<PartBinderFactory<M, V>, Binder<V>>> list = new ArrayList<>();

        for (final PartBinderFactory<M, V> factory : factories) {
            if (factory.isNeeded(model)) list.add(Pair.create(factory, factory.create(model)));
        }

        return list;
    }
}
