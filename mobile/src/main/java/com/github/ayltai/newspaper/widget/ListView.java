package com.github.ayltai.newspaper.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.TestUtils;
import com.github.ayltai.newspaper.view.ListPresenter;
import com.github.ayltai.newspaper.view.UniversalAdapter;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public abstract class ListView<M> extends ObservableView implements ListPresenter.View<M> {
    public static final int NO_INFINITE_LOADING = 0;

    //region Subscriptions

    protected final FlowableProcessor<Integer>    bestVisibleItemPositionChanges = PublishProcessor.create();
    protected final FlowableProcessor<Irrelevant> pullToRefreshes                = PublishProcessor.create();
    protected final FlowableProcessor<Irrelevant> clears                         = PublishProcessor.create();
    protected final FlowableProcessor<Irrelevant> infiniteLoads                  = PublishProcessor.create();
    protected final FlowableProcessor<Boolean>    attachments                    = PublishProcessor.create();
    protected final FlowableProcessor<Irrelevant> detachments                    = PublishProcessor.create();

    //endregion

    protected UniversalAdapter<M, ?, ?> adapter;

    //region Components

    protected SwipeRefreshLayout swipeRefreshLayout;
    protected SmartRecyclerView  recyclerView;
    protected View               loadingView;
    protected View               emptyView;

    //endregion

    private boolean isFirstAttachment = true;

    //region Constructors

    protected ListView(@NonNull final Context context) {
        super(context);
        this.init();
    }

    protected ListView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    protected ListView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    protected ListView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr, @StyleRes final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init();
    }

    //endregion

    //region Properties

    @Nullable
    public UniversalAdapter<M, ?, ?> getAdapter() {
        return this.adapter;
    }

    @LayoutRes
    protected abstract int getLayoutId();

    @IdRes
    protected abstract int getSwipeRefreshLayoutId();

    @IdRes
    protected abstract int getRecyclerViewId();

    @IdRes
    protected abstract int getLoadingViewId();

    @IdRes
    protected abstract int getEmptyViewId();

    protected abstract int getInfiniteLoadingThreshold();

    //endregion

    //region Methods

    @Override
    public void bind(@NonNull final List<M> models) {
        if (TestUtils.isLoggable()) {
            for (final M model : models) Log.v(this.getClass().getSimpleName(), model.toString());
        }

        if (this.adapter.getItemCount() == 0 && models.isEmpty()) {
            this.showEmptyView();
        } else {
            this.recyclerView.setVisibility(View.VISIBLE);

            if (this.loadingView != null) this.loadingView.setVisibility(View.GONE);
            if (this.emptyView != null) this.emptyView.setVisibility(View.GONE);
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
        this.recyclerView.smoothScrollToPosition(0);
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
    public void showLoadingView() {
        this.recyclerView.setVisibility(View.GONE);

        if (this.loadingView != null) this.loadingView.setVisibility(View.VISIBLE);
        if (this.emptyView != null) this.emptyView.setVisibility(View.GONE);
    }

    @Override
    public void showEndOfList() {
        this.adapter.onItemRemoved(this.adapter.getItemCount() - 1);
    }

    @NonNull
    protected abstract UniversalAdapter<M, ?, ?> createAdapter();

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

    @NonNull
    @Override
    public Flowable<Irrelevant> infiniteLoads() {
        return this.infiniteLoads;
    }

    @NonNull
    @Override
    public Flowable<Integer> bestVisibleItemPositionChanges() {
        return this.bestVisibleItemPositionChanges;
    }

    //endregion

    //region Lifecycle

    @NonNull
    @Override
    public Flowable<Boolean> attachments() {
        return this.attachments;
    }

    @NonNull
    @Override
    public Flowable<Irrelevant> detachments() {
        return this.detachments;
    }

    @CallSuper
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        this.manageDisposable(this.recyclerView.bestVisibleItemPositionChanges().subscribe(index -> {
            this.bestVisibleItemPositionChanges.onNext(index);

            if (this.adapter.getItemCount() - index - 1 <= this.getInfiniteLoadingThreshold()) this.infiniteLoads.onNext(Irrelevant.INSTANCE);
        }));

        this.attachments.onNext(isFirstAttachment);

        isFirstAttachment = false;
    }

    @CallSuper
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        this.detachments.onNext(Irrelevant.INSTANCE);
    }

    //endregion

    private void init() {
        this.adapter = this.createAdapter();

        final View view = LayoutInflater.from(this.getContext()).inflate(this.getLayoutId(), this, false);

        this.swipeRefreshLayout = view.findViewById(this.getSwipeRefreshLayoutId());
        this.swipeRefreshLayout.setOnRefreshListener(() -> this.pullToRefreshes.onNext(Irrelevant.INSTANCE));

        this.recyclerView = view.findViewById(this.getRecyclerViewId());
        this.recyclerView.setLayoutManager(new SmartLayoutManager(this.getContext()));
        this.recyclerView.setAdapter(this.adapter);

        if (this.getLoadingViewId() > 0) this.loadingView = view.findViewById(this.getLoadingViewId());
        if (this.getEmptyViewId() > 0) this.emptyView = view.findViewById(this.getEmptyViewId());

        this.emptyView.setVisibility(View.GONE);

        this.addView(view);
    }
}
