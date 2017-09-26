package com.github.ayltai.newspaper.app.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.github.ayltai.newspaper.app.widget.ContentView;
import com.github.ayltai.newspaper.app.widget.FeaturedView;
import com.github.ayltai.newspaper.app.widget.FooterView;
import com.github.ayltai.newspaper.app.widget.HeaderView;
import com.github.ayltai.newspaper.app.widget.ImageView;
import com.github.ayltai.newspaper.app.widget.MetaView;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.util.Animations;
import com.github.ayltai.newspaper.view.SimpleUniversalAdapter;
import com.github.ayltai.newspaper.view.binding.BinderFactory;
import com.github.ayltai.newspaper.view.binding.FullBinderFactory;
import com.github.ayltai.newspaper.widget.SimpleViewHolder;

public final class ItemListAdapter extends SimpleUniversalAdapter<Item, View, SimpleViewHolder<View>> implements Filterable {
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

    private Filter filter;

    private ItemListAdapter(@NonNull final Context context, @NonNull final List<FullBinderFactory<Item>> factories) {
        super(factories);

        this.context = context;
    }

    @NonNull
    @Override
    protected Iterable<Animator> getItemAnimators(@NonNull final View view) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !ValueAnimator.areAnimatorsEnabled() ? super.getItemAnimators(view) : Animations.createDefaultAnimators(view);
    }

    @Override
    public SimpleViewHolder<View> onCreateViewHolder(final ViewGroup parent, final int viewType) {
        switch (viewType) {
            case FeaturedView.VIEW_TYPE:
                return new SimpleViewHolder<>(new FeaturedView(this.context));

            case HeaderView.VIEW_TYPE:
                return new SimpleViewHolder<>(new HeaderView(this.context));

            case ImageView.VIEW_TYPE:
                return new SimpleViewHolder<>(new ImageView(this.context));

            case FooterView.VIEW_TYPE:
                return new SimpleViewHolder<>(new FooterView(this.context));

            case ContentView.VIEW_TYPE:
                return new SimpleViewHolder<>(new ContentView(this.context));

            case MetaView.VIEW_TYPE:
                return new SimpleViewHolder<>(new MetaView(this.context));

            default:
                throw new IllegalArgumentException("Unsupported view type: " + viewType);
        }
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return this.filter == null ? this.filter = null : this.filter;
    }
}
