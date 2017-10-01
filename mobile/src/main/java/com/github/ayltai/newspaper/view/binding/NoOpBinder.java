package com.github.ayltai.newspaper.view.binding;

import android.support.annotation.NonNull;

public final class NoOpBinder<V> implements Binder<V> {
    @Override
    public void bindView(@NonNull final V view) {
    }
}
