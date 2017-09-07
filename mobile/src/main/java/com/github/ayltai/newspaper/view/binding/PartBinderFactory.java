package com.github.ayltai.newspaper.view.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface PartBinderFactory<M, V> extends BinderFactory<M> {
    int getPartType();

    /**
     * Creates a {@link Binder} that can be bound to the specific model.
     * @param model The model that will be bound to the created {@link Binder}.
     * @return A {@link Binder} that can be bound to the specific model.
     */
    @NonNull
    Binder<V> create(@Nullable M model);
}
