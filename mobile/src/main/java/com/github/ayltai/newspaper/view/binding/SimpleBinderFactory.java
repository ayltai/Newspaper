package com.github.ayltai.newspaper.view.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.widget.SimplePartView;

public abstract class SimpleBinderFactory<M, V extends SimplePartView> implements PartBinderFactory<M, V> {
    @NonNull
    @Override
    public Binder<V> create(@Nullable final M model) {
        return view -> {
        };
    }
}
