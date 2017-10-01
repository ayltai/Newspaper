package com.github.ayltai.newspaper.view.binding;

import android.support.annotation.Nullable;

public interface BinderFactory<M> {
    /**
     * Returns {@code true} if a {@code Binder} is needed for this specific model.
     * @param model The model that will be bound the a view if needed.
     * @return {@code true} if a {@code Binder} is needed for this specific model.
     */
    boolean isNeeded(@Nullable M model);
}
