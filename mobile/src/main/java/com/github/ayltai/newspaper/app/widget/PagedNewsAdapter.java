package com.github.ayltai.newspaper.app.widget;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.app.ComponentFactory;
import com.github.ayltai.newspaper.app.config.UserConfig;
import com.github.ayltai.newspaper.app.data.model.Category;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.view.ItemListAdapter;
import com.github.ayltai.newspaper.app.view.ItemListPresenter;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.Views;
import com.github.ayltai.newspaper.widget.VerticalListView;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

class PagedNewsAdapter extends PagerAdapter implements Filterable, LifecycleObserver {
    private final class MainFilter extends Filter {
        @Nullable
        @Override
        protected Filter.FilterResults performFiltering(@Nullable final CharSequence searchText) {
            PagedNewsAdapter.this.searchText = searchText;

            for (int i = 0; i < PagedNewsAdapter.this.getCount(); i++) {
                final VerticalListView<Item> listView = PagedNewsAdapter.this.getItem(i);

                if (listView instanceof ItemListView && listView.getAdapter() instanceof Filterable && ((Filterable)listView.getAdapter()).getFilter() instanceof ItemListAdapter.ItemListFilter) {
                    ((ItemListView)listView).setSearchText(searchText);

                    final ItemListAdapter.ItemListFilter filter = (ItemListAdapter.ItemListFilter)((Filterable)listView.getAdapter()).getFilter();

                    filter.setCategories(new ArrayList<>(Category.fromDisplayName(PagedNewsAdapter.this.categories.get(i))));
                    filter.setSources(PagedNewsAdapter.this.userConfig == null ? Collections.emptySet() : PagedNewsAdapter.this.userConfig.getSources());
                    filter.setFeatured(true);

                    PagedNewsAdapter.this.filterResults.put(i, filter.performFiltering(searchText));
                }
            }

            return (FilterResults)PagedNewsAdapter.this.filterResults.get(PagedNewsAdapter.this.position);
        }

        @Override
        protected void publishResults(@Nullable final CharSequence searchText, @Nullable final FilterResults filterResults) {
            for (int i = 0; i < PagedNewsAdapter.this.getCount(); i++) {
                final VerticalListView<Item> listView = PagedNewsAdapter.this.getItem(i);

                if (listView != null && listView.getAdapter() instanceof Filterable && ((Filterable)listView.getAdapter()).getFilter() instanceof ItemListAdapter.ItemListFilter) {
                    final FilterResults                  results = (FilterResults)PagedNewsAdapter.this.filterResults.get(i);
                    final ItemListAdapter.ItemListFilter filter  = (ItemListAdapter.ItemListFilter)((Filterable)listView.getAdapter()).getFilter();

                    filter.publishResults(searchText, results);

                    if (listView.getAdapter().getItemCount() == 0) {
                        listView.showEmptyView();
                    } else {
                        listView.hideEmptyView();
                    }
                }
            }
        }
    }

    private final List<String>                           categories    = new ArrayList<>();
    private final SparseArrayCompat<SoftReference<View>> views         = new SparseArrayCompat<>();
    private final SparseArrayCompat<Object>              filterResults = new SparseArrayCompat<>();

    @Nullable
    private final UserConfig userConfig;

    private CompositeDisposable disposables;
    private Filter              filter;
    private int                 position;
    private CharSequence        searchText;

    PagedNewsAdapter(@NonNull final Context context) {
        final Activity activity = Views.getActivity(context);
        this.userConfig = activity == null
            ? null
            : ComponentFactory.getInstance()
                .getConfigComponent(activity)
                .userConfig();

        final List<String> categories = this.userConfig == null ? Collections.emptyList() : this.userConfig.getCategories();
        for (final String category : categories) {
            final String name = Category.toDisplayName(category);
            if (!this.categories.contains(name)) this.categories.add(name);
        }
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return this.filter == null ? this.filter = new PagedNewsAdapter.MainFilter() : this.filter;
    }

    @Override
    public int getCount() {
        return this.categories.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull final View view, @NonNull final Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public CharSequence getPageTitle(final int position) {
        return this.categories.get(position);
    }

    @Nullable
    public VerticalListView<Item> getItem(final int position) {
        final SoftReference<View> view = this.views.get(position);

        if (view == null) return null;
        return (VerticalListView<Item>)view.get();
    }

    public void setCurrentPosition(final int position) {
        this.position = position;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        final String                 category   = this.categories.get(position);
        final List<String>           categories = new ArrayList<>(Category.fromDisplayName(category));
        final ItemListPresenter      presenter  = new ItemListPresenter(categories);
        final VerticalListView<Item> listView   = this.getItem(position);
        final ItemListView           view       = listView == null ? this.userConfig == null || this.userConfig.getViewStyle() == Constants.VIEW_STYLE_COZY ? new CozyItemListView(container.getContext()) : new CompactItemListView(container.getContext()) : (ItemListView)listView;

        if (this.disposables == null) this.disposables = new CompositeDisposable();

        this.disposables.add(view.attachments().subscribe(
            isFirstTimeAttachment -> presenter.onViewAttached(view, isFirstTimeAttachment),
            error -> {
                if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
            }
        ));

        this.disposables.add(view.detachments().subscribe(
            irrelevant -> presenter.onViewDetached(),
            error -> {
                if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
            }
        ));

        this.views.put(position, new SoftReference<>(view));
        container.addView(view);

        if (!TextUtils.isEmpty(this.searchText)) {
            view.setCategories(categories);
            view.setSources(this.userConfig == null ? Collections.emptySet() : this.userConfig.getSources());
            view.setSearchText(this.searchText);
        }

        return view;
    }

    @Override
    public void destroyItem(@NonNull final ViewGroup container, final int position, @NonNull final Object object) {
        final SoftReference<View> reference = this.views.get(position);

        if (reference != null) {
            final View view = reference.get();

            if (view != null) {
                this.views.remove(position);
                container.removeView(view);

                this.filterResults.remove(position);
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void dispose() {
        if (this.disposables != null && !this.disposables.isDisposed()) {
            this.disposables.dispose();
            this.disposables = null;
        }
    }
}
