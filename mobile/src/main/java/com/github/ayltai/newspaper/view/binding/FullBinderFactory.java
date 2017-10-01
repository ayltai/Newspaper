package com.github.ayltai.newspaper.view.binding;

import java.util.Collection;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface FullBinderFactory<M> extends BinderFactory<M> {
    /**
     * Returns a collection of {@link BinderFactory} that should be bound to the specific model.
     * @param model The model to bind to the {@link Binder}s to be created.
     * @return A collection of {@link BinderFactory} that should be bound to the specific model.
     */
    @NonNull
    Collection<BinderFactory<M>> getParts(@Nullable M model);
}
