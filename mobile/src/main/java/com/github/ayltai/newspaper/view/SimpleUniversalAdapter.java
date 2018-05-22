package com.github.ayltai.newspaper.view;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.view.View;

import com.github.ayltai.newspaper.view.binding.Binder;
import com.github.ayltai.newspaper.view.binding.FullBinderFactory;
import com.github.ayltai.newspaper.widget.SimpleViewHolder;

import java.util.List;
import java.util.Map;

import io.reactivex.disposables.Disposable;

public abstract class SimpleUniversalAdapter<M, V extends View, T extends SimpleViewHolder<V>> extends UniversalAdapter<M, V, T> implements Disposable {
    private final Map<T, Binder<V>> bindings = new ArrayMap<>();

    protected SimpleUniversalAdapter(@NonNull final List<FullBinderFactory<M>> fullBinderFactories) {
        super(fullBinderFactories);
    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    @Override
    public void dispose() {
        for (final Binder<V> binder : this.bindings.values()) this.dispose(binder);
    }

    private void dispose(final Binder<V> binder) {
        if (binder instanceof Disposable) {
            final Disposable disposable = (Disposable)binder;
            if (!disposable.isDisposed()) disposable.dispose();
        }
    }

    @Override
    public void onBindViewHolder(final T holder, final int position) {
        this.dispose(this.bindings.get(holder));

        final Binder<V> binder = this.getBinder(holder.getAdapterPosition());
        binder.bindView((V)holder.itemView);

        this.bindings.put(holder, binder);

        super.onBindViewHolder(holder, position);
    }
}
