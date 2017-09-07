package com.github.ayltai.newspaper.view;

import java.util.List;

import android.support.annotation.NonNull;
import android.view.View;

import com.github.ayltai.newspaper.view.binding.FullBinderFactory;
import com.github.ayltai.newspaper.widget.SimpleViewHolder;

public abstract class SimpleUniversalAdapter<M, V extends View, T extends SimpleViewHolder<V>> extends UniversalAdapter<M, V, T> {
    protected SimpleUniversalAdapter(@NonNull final List<FullBinderFactory<M>> fullBinderFactories) {
        super(fullBinderFactories);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(final T holder, final int position) {
        this.getBinder(position).bindView((V)holder.itemView);

        super.onBindViewHolder(holder, position);
    }
}
