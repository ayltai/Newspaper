package com.github.ayltai.newspaper.app.widget;

import java.util.List;
import java.util.Set;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.Filterable;
import android.widget.TextView;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.view.ItemListAdapter;
import com.github.ayltai.newspaper.util.Animations;
import com.github.ayltai.newspaper.view.ListPresenter;
import com.github.ayltai.newspaper.widget.ListView;
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView;

import io.reactivex.disposables.Disposable;

public abstract class ItemListView extends ListView<Item> implements ListPresenter.View<Item>, Disposable, LifecycleObserver {
    //region Supports initial searching

    private List<String> categories;
    private Set<String>  sources;
    private CharSequence searchText;

    //endregion

    protected ItemListView(@NonNull final Context context) {
        super(context);
        this.init();
    }

    //region Search properties

    public void setCategories(@NonNull final List<String> categories) {
        this.categories = categories;
    }

    public void setSources(@NonNull final Set<String> sources) {
        this.sources = sources;
    }

    public void setSearchText(final CharSequence searchText) {
        this.searchText = searchText;
    }

    //endregion

    //region Properties

    @Override
    public boolean isDisposed() {
        return false;
    }

    @LayoutRes
    @Override
    protected int getSwipeRefreshLayoutId() {
        return R.id.swipeRefreshLayout;
    }

    @IdRes
    @Override
    protected int getRecyclerViewId() {
        return R.id.recyclerView;
    }

    @IdRes
    @Override
    protected int getLoadingViewId() {
        return R.id.loading;
    }

    @IdRes
    @Override
    protected int getEmptyViewId() {
        return android.R.id.empty;
    }

    @StringRes
    @Override
    protected int getEmptyTitle() {
        return R.string.empty_news_title;
    }

    @StringRes
    @Override
    protected int getEmptyDescription() {
        return R.string.empty_news_description;
    }

    //endregion

    //region Methods

    @Override
    public void bind(@NonNull final List<Item> models) {
        super.bind(models);

        if (!TextUtils.isEmpty(this.searchText) && this.adapter instanceof Filterable) {
            final ItemListAdapter.ItemListFilter filter = (ItemListAdapter.ItemListFilter)((Filterable)this.adapter).getFilter();

            if (filter != null) {
                filter.setCategories(this.categories);
                filter.setSources(this.sources);
                filter.setFeatured(true);

                // FIXME: performFiltering() should be executed on a background thread, but Filter.FilterResults class is protected
                filter.publishResults(this.searchText, filter.performFiltering(this.searchText));

                if (this.adapter.getItemCount() == 0) this.showEmptyView();
            }
        }
    }

    @Override
    public void scrollTo(final int scrollPosition) {
        if (scrollPosition > 0) this.recyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void showLoadingView() {
        super.showLoadingView();

        if (this.loadingView != null) {
            this.findViewById(R.id.scrolling_background).setVisibility(View.GONE);

            if (Animations.isEnabled()) Animations.startShimmerAnimation(this.loadingView);
        }
    }

    @Override
    public void hideLoadingView() {
        super.hideLoadingView();

        final View view = this.findViewById(R.id.scrolling_background);
        if (view != null) view.setVisibility(View.VISIBLE);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    @Override
    public void dispose() {
        if (this.adapter instanceof Disposable) {
            final Disposable disposable = (Disposable)this.adapter;
            if (!disposable.isDisposed()) disposable.dispose();
        }
    }

    //endregion

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        final View view = this.findViewById(R.id.scrolling_background);
        if (view != null) this.manageDisposable(RxRecyclerView.scrollEvents(this.recyclerView).subscribe(event -> view.setTranslationY(view.getTranslationY() - event.dy())));
    }

    private void init() {
        if (this.getEmptyTitle() > 0) ((TextView)this.emptyView.findViewById(R.id.empty_title)).setText(this.getEmptyTitle());
        if (this.getEmptyDescription() > 0) ((TextView)this.emptyView.findViewById(R.id.empty_description)).setText(this.getEmptyDescription());

        final LifecycleOwner owner = this.getLifecycleOwner();
        if (owner != null) owner.getLifecycle().addObserver(this);
    }
}
