package com.github.ayltai.newspaper.app.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.github.ayltai.newspaper.app.widget.FooterView;
import com.github.ayltai.newspaper.app.widget.HeaderView;
import com.github.ayltai.newspaper.app.widget.ImageView;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.util.ViewUtils;
import com.github.ayltai.newspaper.view.SimpleUniversalAdapter;
import com.github.ayltai.newspaper.view.binding.BinderFactory;
import com.github.ayltai.newspaper.view.binding.FullBinderFactory;
import com.github.ayltai.newspaper.widget.SimpleViewHolder;

public final class ItemListAdapter extends SimpleUniversalAdapter<Item, View, SimpleViewHolder<View>> {
    public static final class Builder {
        private final Collection<BinderFactory<Item>> factories = new ArrayList<>();
        private final Context                         context;

        public Builder(@NonNull final Context context) {
            this.context = context;
        }

        @NonNull
        public ItemListAdapter.Builder addBinderFactory(@NonNull final BinderFactory<Item> factory) {
            this.factories.add(factory);

            return this;
        }

        @NonNull
        public ItemListAdapter build() {
            return new ItemListAdapter(this.context, Collections.singletonList(new FullBinderFactory<Item>() {
                @NonNull
                @Override
                public Collection<BinderFactory<Item>> getParts(@Nullable final Item model) {
                    return ItemListAdapter.Builder.this.factories;
                }

                @Override
                public boolean isNeeded(@Nullable final Item model) {
                    return true;
                }
            }));
        }
    }

    private final Context context;

    public ItemListAdapter(@NonNull final Context context, @NonNull final List<FullBinderFactory<Item>> factories) {
        super(factories);

        this.context = context;
    }

    @NonNull
    @Override
    protected Iterable<Animator> getItemAnimators(@NonNull final View view) {
        return ViewUtils.createDefaultAnimators(view);
    }

    @Override
    public SimpleViewHolder<View> onCreateViewHolder(final ViewGroup parent, final int viewType) {
        switch (viewType) {
            case HeaderView.VIEW_TYPE:
                return new SimpleViewHolder<>(new HeaderView(this.context));

            case ImageView.VIEW_TYPE:
                return new SimpleViewHolder<>(new ImageView(this.context));

            case FooterView.VIEW_TYPE:
                return new SimpleViewHolder<>(new FooterView(this.context));

            default:
                throw new IllegalArgumentException("Unsupported view type: " + viewType);
        }
    }
}
