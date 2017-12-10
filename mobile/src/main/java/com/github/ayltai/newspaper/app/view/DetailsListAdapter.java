package com.github.ayltai.newspaper.app.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.widget.DetailsView;
import com.github.ayltai.newspaper.view.SimpleUniversalAdapter;
import com.github.ayltai.newspaper.view.binding.BinderFactory;
import com.github.ayltai.newspaper.view.binding.FullBinderFactory;
import com.github.ayltai.newspaper.widget.SimpleViewHolder;

public final class DetailsListAdapter extends SimpleUniversalAdapter<Item, View, SimpleViewHolder<View>> {
    public static final class Builder {
        private final Collection<BinderFactory<Item>> factories = new ArrayList<>();
        private final Context                         context;

        public Builder(final Context context) {
            this.context = context;
        }

        @NonNull
        public DetailsListAdapter.Builder addBinderFactory(@NonNull final BinderFactory<Item> factory) {
            this.factories.add(factory);

            return this;
        }

        @NonNull
        public DetailsListAdapter build() {
            return new DetailsListAdapter(this.context, Collections.singletonList(new FullBinderFactory<Item>() {
                @NonNull
                @Override
                public Collection<BinderFactory<Item>> getParts(@Nullable final Item model) {
                    return DetailsListAdapter.Builder.this.factories;
                }

                @Override
                public boolean isNeeded(@Nullable final Item model) {
                    return true;
                }
            }));
        }
    }

    private final Context context;

    private DetailsListAdapter(@NonNull final Context context, @NonNull final List<FullBinderFactory<Item>> factories) {
        super(factories);

        this.context = context;
    }

    @NonNull
    @Override
    public SimpleViewHolder<View> onCreateViewHolder(final ViewGroup parent, final int viewType) {
        if (viewType == DetailsView.VIEW_TYPE) return new SimpleViewHolder<>(new DetailsView(this.context));

        throw new IllegalArgumentException("Unsupported view type: " + viewType);
    }
}
