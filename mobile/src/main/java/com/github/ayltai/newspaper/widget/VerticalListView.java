package com.github.ayltai.newspaper.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.view.VerticalListPresenter;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public abstract class VerticalListView<M> extends ListView<M> implements VerticalListPresenter.View<M> {
    //region Subscriptions

    protected final FlowableProcessor<Irrelevant> pullToRefreshes = PublishProcessor.create();
    protected final FlowableProcessor<Irrelevant> clears          = PublishProcessor.create();

    //endregion

    //region Components

    protected SwipeRefreshLayout swipeRefreshLayout;
    protected View               loadingView;
    protected View               emptyView;

    //endregion

    protected VerticalListView(@NonNull final Context context) {
        super(context);
    }

    //region Properties

    @IdRes
    protected abstract int getSwipeRefreshLayoutId();

    @IdRes
    protected abstract int getLoadingViewId();

    @IdRes
    protected abstract int getEmptyViewId();

    @StringRes
    protected abstract int getEmptyTitle();

    @StringRes
    protected abstract int getEmptyDescription();

    //endregion

    //region Methods

    @Override
    public void bind(@NonNull final List<M> models) {
        if (DevUtils.isLoggable()) {
            for (final M model : models) Log.v(this.getClass().getSimpleName(), model == null ? null : model.toString());
        }

        if (this.adapter.getItemCount() == 0 && models.isEmpty()) {
            this.showEmptyView();
        } else {
            this.hideEmptyView();
            this.hideLoadingView();
        }

        if (this.adapter.getItemCount() == 0) {
            final Collection<M> items = new ArrayList<>(models);
            items.add(null);

            this.adapter.onItemRangeInserted(items, 0);
        } else {
            this.adapter.onItemRangeInserted(models, this.adapter.getItemCount() - 1);
        }

        this.swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void clear() {
        this.adapter.clear();
    }

    @Override
    public void clearAll() {
        this.adapter.clear();

        this.clears.onNext(Irrelevant.INSTANCE);
    }

    @Override
    public void up() {
        this.recyclerView.scrollToPosition(0);
    }

    @Override
    public void refresh() {
        this.showLoadingView();
        this.swipeRefreshLayout.setRefreshing(true);

        if (this.recyclerView.getAdapter() != null && this.recyclerView.getAdapter().getItemCount() > 0) this.recyclerView.scrollToPosition(0);

        this.pullToRefreshes.onNext(Irrelevant.INSTANCE);
    }

    @Override
    public void showEmptyView() {
        this.recyclerView.setVisibility(View.GONE);

        if (this.loadingView != null) this.loadingView.setVisibility(View.GONE);
        if (this.emptyView != null) this.emptyView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideEmptyView() {
        this.recyclerView.setVisibility(View.VISIBLE);

        if (this.loadingView != null) this.loadingView.setVisibility(View.GONE);
        if (this.emptyView != null) this.emptyView.setVisibility(View.GONE);
    }

    @Override
    public void showLoadingView() {
        this.recyclerView.setVisibility(View.GONE);

        if (this.loadingView != null) this.loadingView.setVisibility(View.VISIBLE);
        if (this.emptyView != null) this.emptyView.setVisibility(View.GONE);
    }

    @Override
    public void hideLoadingView() {
        this.recyclerView.setVisibility(View.VISIBLE);

        if (this.loadingView != null) this.loadingView.setVisibility(View.GONE);
        if (this.emptyView != null) this.emptyView.setVisibility(View.GONE);
    }

    //endregion

    //region Events

    @NonNull
    @Override
    public Flowable<Irrelevant> clears() {
        return this.clears;
    }

    @NonNull
    @Override
    public Flowable<Irrelevant> pullToRefreshes() {
        return this.pullToRefreshes;
    }

    //endregion

    @Override
    protected void init() {
        super.init();

        this.swipeRefreshLayout = this.findViewById(this.getSwipeRefreshLayoutId());
        this.swipeRefreshLayout.setColorSchemeResources(R.color.refreshColor1, R.color.refreshColor2, R.color.refreshColor3, R.color.refreshColor4);
        this.swipeRefreshLayout.setOnRefreshListener(() -> this.pullToRefreshes.onNext(Irrelevant.INSTANCE));

        if (this.getLoadingViewId() != 0) this.loadingView = this.findViewById(this.getLoadingViewId());
        if (this.getEmptyViewId() != 0) this.emptyView = this.findViewById(this.getEmptyViewId());

        this.emptyView.setVisibility(View.GONE);
    }
}
