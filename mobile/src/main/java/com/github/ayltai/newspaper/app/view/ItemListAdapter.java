package com.github.ayltai.newspaper.app.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.app.data.ItemManager;
import com.github.ayltai.newspaper.app.data.model.FeaturedItem;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.widget.CompactItemView;
import com.github.ayltai.newspaper.app.widget.CozyItemView;
import com.github.ayltai.newspaper.app.widget.FeaturedView;
import com.github.ayltai.newspaper.data.DataManager;
import com.github.ayltai.newspaper.util.Animations;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.StringUtils;
import com.github.ayltai.newspaper.view.SimpleUniversalAdapter;
import com.github.ayltai.newspaper.view.binding.BinderFactory;
import com.github.ayltai.newspaper.view.binding.FullBinderFactory;
import com.github.ayltai.newspaper.widget.DelegatingFilter;
import com.github.ayltai.newspaper.widget.SimpleViewHolder;

public final class ItemListAdapter extends SimpleUniversalAdapter<Item, View, SimpleViewHolder<View>> implements Filterable {
    public static final class Builder {
        private final Collection<BinderFactory<Item>> factories = new ArrayList<>();
        private final Context                         context;
        private final String                          category;

        private boolean isHistorical;
        private boolean isBookmarked;

        public Builder(@NonNull final Context context, @NonNull final String category) {
            this.context  = context;
            this.category = category;
        }

        @NonNull
        public ItemListAdapter.Builder setIsHistorical(final boolean isHistorical) {
            this.isHistorical = isHistorical;

            return this;
        }

        @NonNull
        public ItemListAdapter.Builder setIsBookmarked(final boolean isBookmarked) {
            this.isBookmarked = isBookmarked;

            return this;
        }

        @NonNull
        public ItemListAdapter.Builder addBinderFactory(@NonNull final BinderFactory<Item> factory) {
            this.factories.add(factory);

            return this;
        }

        @NonNull
        public ItemListAdapter build() {
            return new ItemListAdapter(this.context, this.category, this.isHistorical, this.isBookmarked, Collections.singletonList(new FullBinderFactory<Item>() {
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

    public final class ItemListFilter extends DelegatingFilter {
        private List<String> categories;
        private Set<String>  sources;
        private boolean      isHistorical;
        private boolean      isBookmarked;
        private boolean      isFeatured;

        public void setCategories(@NonNull final List<String> categories) {
            this.categories = categories;
        }

        public void setSources(@NonNull final Set<String> sources) {
            this.sources = sources;
        }

        public void setHistorical(final boolean isHistorical) {
            this.isHistorical = isHistorical;
        }

        public void setBookmarked(final boolean isBookmarked) {
            this.isBookmarked = isBookmarked;
        }

        public void setFeatured(final boolean isFeatured) {
            this.isFeatured = isFeatured;
        }

        @SuppressWarnings("IllegalCatch")
        @NonNull
        @Override
        public FilterResults performFiltering(@Nullable final CharSequence searchText) {
            final FilterResults results = new FilterResults();

            try {
                final List<NewsItem> items = ItemManager.create(ItemListAdapter.this.context)
                    .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER))
                    .flatMap(manager -> {
                        if (this.isHistorical) return manager.getHistoricalItems(searchText, this.sources.toArray(StringUtils.EMPTY_ARRAY), this.categories.toArray(StringUtils.EMPTY_ARRAY))
                                .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER));

                        if (this.isBookmarked) return manager.getBookmarkedItems(searchText, this.sources.toArray(StringUtils.EMPTY_ARRAY), this.categories.toArray(StringUtils.EMPTY_ARRAY))
                                .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER));

                        return manager.getItems(searchText, this.sources.toArray(StringUtils.EMPTY_ARRAY), this.categories.toArray(StringUtils.EMPTY_ARRAY))
                            .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER));
                    })
                    .blockingGet();

                results.values = items;
                results.count  = items.size();
            } catch (final Throwable e) {
                if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), RxJava2Debug.getEnhancedStackTrace(e));
            }

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void publishResults(@Nullable final CharSequence searchText, @Nullable final FilterResults results) {
            ItemListAdapter.this.clear();

            if (results != null && results.values != null) {
                final List<Item> items = (List<Item>)results.values;
                if (results.count > 0) {
                    Collections.sort(items);

                    if (this.isFeatured && TextUtils.isEmpty(searchText)) {
                        final List<Item> featuredItems = new ArrayList<>(items);
                        featuredItems.add(0, FeaturedItem.create(items));

                        ItemListAdapter.this.onDataSetChanged(featuredItems);
                    } else {
                        ItemListAdapter.this.onDataSetChanged(items);
                    }
                }
            }
        }
    }

    //region Variables

    private final Context context;
    private final String  category;
    private final boolean isHistorical;
    private final boolean isBookmarked;

    private Filter filter;

    //endregion

    private ItemListAdapter(@NonNull final Context context, @NonNull final String category, final boolean isHistorical, final boolean isBookmarked, @NonNull final List<FullBinderFactory<Item>> factories) {
        super(factories);

        this.context      = context;
        this.category     = category;
        this.isHistorical = isHistorical;
        this.isBookmarked = isBookmarked;
    }

    @NonNull
    @Override
    protected Iterable<Animator> getItemAnimators(@NonNull final View view) {
        return Animations.isEnabled() ? Animations.createDefaultAnimators(view) : super.getItemAnimators(view);
    }

    @NonNull
    @Override
    public SimpleViewHolder<View> onCreateViewHolder(final ViewGroup parent, final int viewType) {
        switch (viewType) {
            case FeaturedView.VIEW_TYPE:
                return new SimpleViewHolder<>(new FeaturedView(this.context));

            case CozyItemView.VIEW_TYPE:
                return new SimpleViewHolder<>(new CozyItemView(this.context));

            case CompactItemView.VIEW_TYPE:
                return new SimpleViewHolder<>(new CompactItemView(this.context));

            default:
                throw new IllegalArgumentException("Unsupported view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder<View> holder, final int position) {
        final ItemPresenter presenter = (ItemPresenter)this.getBinder(position);
        presenter.setCategory(this.category);
        presenter.setIsHistorical(this.isHistorical);
        presenter.setIsBookmarked(this.isBookmarked);
        presenter.setItemPosition(((ItemPresenter)this.getBinder(0)).getModel() instanceof FeaturedItem ? position - 1 : position);

        super.onBindViewHolder(holder, position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return this.filter == null ? this.filter = new ItemListAdapter.ItemListFilter() : this.filter;
    }
}
