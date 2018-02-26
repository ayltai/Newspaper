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
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Filterable;
import android.widget.TextView;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.view.ItemListAdapter;
import com.github.ayltai.newspaper.util.Animations;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.widget.SmartLayoutManager;
import com.github.ayltai.newspaper.widget.VerticalListView;

import io.reactivex.disposables.Disposable;

public abstract class ItemListView extends VerticalListView<Item> implements Disposable, LifecycleObserver {
    private static final class OnScrollListener extends RecyclerView.OnScrollListener {
        private final View view;

        OnScrollListener(@NonNull final View view) {
            this.view = view;
        }

        @Override
        public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
            view.setTranslationY(view.getTranslationY() - dy);
        }
    }

    //region Supports initial searching

    private List<String> categories;
    private Set<String>  sources;
    private CharSequence searchText;

    //endregion

    private OnScrollListener onScrollListener;

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
    public void scrollTo(final int scrollPosition, final boolean smoothScroll) {
        if (scrollPosition > 0) this.recyclerView.smoothScrollToPosition(scrollPosition);
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

        if (this.loadingView != null && Animations.isEnabled()) Animations.stopShimmerAnimation(this.loadingView);
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

    //region Lifecycle

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        final View view = this.findViewById(R.id.scrolling_background);
        if (view != null) {
            if (this.onScrollListener == null) this.onScrollListener = new OnScrollListener(view);
            this.recyclerView.addOnScrollListener(this.onScrollListener);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        if (this.onScrollListener != null) this.recyclerView.removeOnScrollListener(this.onScrollListener);

        super.onDetachedFromWindow();
    }

    //endregion

    @Override
    protected void init() {
        this.adapter = this.createAdapter();

        final View view = LayoutInflater.from(this.getContext()).inflate(this.getLayoutId(), this, false);

        this.recyclerView = view.findViewById(this.getRecyclerViewId());
        this.recyclerView.setLayoutManager(new SmartLayoutManager(this.getContext()));
        this.recyclerView.setAdapter(this.adapter);

        this.swipeRefreshLayout = view.findViewById(this.getSwipeRefreshLayoutId());
        this.swipeRefreshLayout.setColorSchemeResources(R.color.refreshColor1, R.color.refreshColor2, R.color.refreshColor3, R.color.refreshColor4);
        this.swipeRefreshLayout.setOnRefreshListener(() -> this.pullToRefreshes.onNext(Irrelevant.INSTANCE));

        if (this.getLoadingViewId() > 0) this.loadingView = view.findViewById(this.getLoadingViewId());
        if (this.getEmptyViewId() > 0) this.emptyView = view.findViewById(this.getEmptyViewId());

        this.emptyView.setVisibility(View.GONE);

        this.addView(view);

        if (this.getEmptyTitle() > 0) ((TextView)this.emptyView.findViewById(R.id.empty_title)).setText(this.getEmptyTitle());
        if (this.getEmptyDescription() > 0) ((TextView)this.emptyView.findViewById(R.id.empty_description)).setText(this.getEmptyDescription());

        final LifecycleOwner owner = this.getLifecycleOwner();
        if (owner != null) owner.getLifecycle().addObserver(this);
    }
}
