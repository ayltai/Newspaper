package com.github.ayltai.newspaper.app.widget;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
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
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.view.ItemListPresenter;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.util.ViewUtils;
import com.github.ayltai.newspaper.widget.ListView;
import com.jakewharton.rxbinding2.view.RxView;

import io.reactivex.disposables.Disposable;

public abstract class ItemListView extends ListView<Item> implements ItemListPresenter.View, Disposable, LifecycleObserver {
    private Button emptyAction;

    //region Constructors

    public ItemListView(@NonNull final Context context) {
        super(context);
        this.init();
    }

    public ItemListView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public ItemListView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public ItemListView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr, @StyleRes final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init();
    }

    //endregion

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

    @Override
    protected int getInfiniteLoadingThreshold() {
        return ListView.NO_INFINITE_LOADING;
    }

    @Override
    public void showLoadingView() {
        super.showLoadingView();

        ViewUtils.startShimmerAnimation(this.loadingView);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    @Override
    public void dispose() {
        if (this.adapter instanceof Disposable) {
            final Disposable disposable = (Disposable)this.adapter;
            if (!disposable.isDisposed()) disposable.dispose();
        }
    }

    @CallSuper
    @Override
    protected void onAttachedToWindow() {
        this.manageDisposable(RxView.clicks(this.emptyAction).subscribe(irrelevant -> this.refresh()));

        super.onAttachedToWindow();
    }

    private void init() {
        ((TextView)this.emptyView.findViewById(R.id.empty_title)).setText(R.string.empty_news_title);
        ((TextView)this.emptyView.findViewById(R.id.empty_description)).setText(R.string.empty_news_description);

        this.emptyAction = this.emptyView.findViewById(R.id.empty_action);
        this.emptyAction.setText(R.string.empty_news_action);

        final LifecycleOwner owner = this.getLifecycleOwner();
        if (owner != null) owner.getLifecycle().addObserver(this);
    }
}
