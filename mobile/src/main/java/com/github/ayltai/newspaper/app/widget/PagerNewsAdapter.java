package com.github.ayltai.newspaper.app.widget;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

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

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.app.data.model.Category;
import com.github.ayltai.newspaper.app.view.ItemListAdapter;
import com.github.ayltai.newspaper.app.view.ItemListPresenter;
import com.github.ayltai.newspaper.config.UserConfig;
import com.github.ayltai.newspaper.util.TestUtils;
import com.github.ayltai.newspaper.widget.ListView;

import io.reactivex.disposables.CompositeDisposable;

class PagerNewsAdapter extends PagerAdapter implements Filterable, LifecycleObserver {
    private final class MainFilter extends Filter {
        @Nullable
        @Override
        protected FilterResults performFiltering(@Nullable final CharSequence searchText) {
            PagerNewsAdapter.this.searchText = searchText;

            for (int i = 0; i < PagerNewsAdapter.this.getCount(); i++) {
                final ListView listView = PagerNewsAdapter.this.getItem(i);
                if (listView != null && listView.getAdapter() instanceof Filterable && ((Filterable)listView.getAdapter()).getFilter() instanceof ItemListAdapter.ItemListFilter) {
                    final ItemListAdapter.ItemListFilter filter = (ItemListAdapter.ItemListFilter)((Filterable)listView.getAdapter()).getFilter();

                    filter.setCategories(new ArrayList<>(Category.fromDisplayName(UserConfig.getCategories(listView.getContext()).get(PagerNewsAdapter.this.position))));
                    filter.setSources(UserConfig.getSources(listView.getContext()));
                    filter.setFeatured(true);

                    PagerNewsAdapter.this.filterResults.put(i, filter.performFiltering(searchText));
                }
            }

            return (FilterResults)PagerNewsAdapter.this.filterResults.get(PagerNewsAdapter.this.position);
        }

        @Override
        protected void publishResults(@Nullable final CharSequence searchText, @Nullable final FilterResults filterResults) {
            for (int i = 0; i < PagerNewsAdapter.this.getCount(); i++) {
                final ListView listView = PagerNewsAdapter.this.getItem(i);

                if (listView != null && listView.getAdapter() instanceof Filterable && ((Filterable)listView.getAdapter()).getFilter() instanceof ItemListAdapter.ItemListFilter) {
                    final FilterResults                  results = (FilterResults)PagerNewsAdapter.this.filterResults.get(i);
                    final ItemListAdapter.ItemListFilter filter  = (ItemListAdapter.ItemListFilter)((Filterable)listView.getAdapter()).getFilter();

                    filter.setCategories(new ArrayList<>(Category.fromDisplayName(UserConfig.getCategories(listView.getContext()).get(PagerNewsAdapter.this.position))));
                    filter.setSources(UserConfig.getSources(listView.getContext()));
                    filter.setFeatured(true);
                    filter.publishResults(searchText, results);
                }
            }
        }
    }

    private final List<String>                           categories    = new ArrayList<>();
    private final SparseArrayCompat<SoftReference<View>> views         = new SparseArrayCompat<>();
    private final SparseArrayCompat<Object>              filterResults = new SparseArrayCompat<>();

    private CompositeDisposable disposables;
    private Filter              filter;
    private int                 position;
    private CharSequence        searchText;

    PagerNewsAdapter(@NonNull final Context context) {
        final List<String> categories = UserConfig.getCategories(context);
        for (final String category : categories) {
            final String name = Category.toDisplayName(category);
            if (!this.categories.contains(name)) this.categories.add(name);
        }
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return this.filter == null ? this.filter = new PagerNewsAdapter.MainFilter() : this.filter;
    }

    @Override
    public int getCount() {
        return this.categories.size();
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public CharSequence getPageTitle(final int position) {
        return this.categories.get(position);
    }

    @Nullable
    public ListView getItem(final int position) {
        final SoftReference<View> view = this.views.get(position);

        if (view == null) return null;
        return (ListView)view.get();
    }

    public void setCurrentPosition(final int position) {
        this.position = position;
    }

    @NonNull
    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        final List<String>      categories = new ArrayList<>(Category.fromDisplayName(this.categories.get(position)));
        final ItemListPresenter presenter  = new ItemListPresenter(categories);
        final ItemListView      view       = UserConfig.getViewStyle(container.getContext()) == Constants.VIEW_STYLE_COZY ? new CozyItemListView(container.getContext()) : new CompactItemListView(container.getContext());

        if (this.disposables == null) this.disposables = new CompositeDisposable();

        this.disposables.add(view.attachments().subscribe(
            isFirstTimeAttachment -> presenter.onViewAttached(view, isFirstTimeAttachment),
            error -> {
                if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
            }
        ));

        this.disposables.add(view.detachments().subscribe(
            irrelevant -> presenter.onViewDetached(),
            error -> {
                if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
            }
        ));

        this.views.put(position, new SoftReference<>(view));
        container.addView(view);

        if (!TextUtils.isEmpty(this.searchText) && view.getAdapter() instanceof Filterable && ((Filterable)view.getAdapter()).getFilter() != null) ((Filterable)view.getAdapter()).getFilter().filter(this.searchText);

        return view;
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
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
