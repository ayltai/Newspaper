package com.github.ayltai.newspaper.widget;

import java.util.List;

import javax.annotation.Nonnull;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.view.ListPresenter;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public abstract class ListView extends BaseView implements ListPresenter.View {
    //region Variables

    protected final FlowableProcessor<Irrelevant> refreshActions = PublishProcessor.create();
    protected final FlowableProcessor<Irrelevant> clearActions   = PublishProcessor.create();

    protected ListAdapter        adapter;
    protected RecyclerView       recyclerView;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected View               emptyView;
    protected View               loadingView;

    //endregion

    protected ListView(@Nonnull @NonNull @lombok.NonNull final Context context) {
        super(context);

        this.adapter = this.createAdapter();

        final View view = LayoutInflater.from(this.getContext()).inflate(this.getLayoutId(), this, false);

        this.swipeRefreshLayout = view.findViewById(this.getSwipeRefreshLayoutId());
        this.loadingView        = view.findViewById(R.id.loading);
        this.emptyView          = view.findViewById(android.R.id.empty);

        this.recyclerView = view.findViewById(this.getRecyclerViewId());
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        this.recyclerView.setAdapter(this.adapter);

        this.addView(view);
        this.updateLayout(BaseView.LAYOUT_SCREEN);
    }

    @Nullable
    public RecyclerView.Adapter<?> getAdapter() {
        return this.adapter;
    }

    @LayoutRes
    protected abstract int getLayoutId();

    @IdRes
    protected abstract int getRecyclerViewId();

    @IdRes
    protected abstract int getSwipeRefreshLayoutId();

    @IdRes
    protected abstract int getEmptyViewId();

    @StringRes
    protected abstract int getEmptyTitle();

    @StringRes
    protected abstract int getEmptyDescription();

    @IdRes
    protected abstract int getLoadingViewId();

    @Nonnull
    @NonNull
    protected abstract ListAdapter createAdapter();

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> refreshActions() {
        return this.refreshActions;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> clearActions() {
        return this.clearActions;
    }

    @Override
    public void bind(@Nonnull @NonNull @lombok.NonNull final List<Item> items) {
        if (this.adapter.getItemCount() == 0 && items.isEmpty()) {
            this.showEmptyView();
        } else {
            this.hideEmptyView();
            this.hideLoadingView();
        }

        this.adapter.setItems(items);

        this.swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void refresh() {
        this.showLoadingView();
        this.swipeRefreshLayout.setRefreshing(true);

        if (this.recyclerView.getAdapter() != null && this.recyclerView.getAdapter().getItemCount() > 0) this.recyclerView.scrollToPosition(0);

        this.refreshActions.onNext(Irrelevant.INSTANCE);
    }

    @Override
    public void clear() {
        this.adapter.clear();
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
}
