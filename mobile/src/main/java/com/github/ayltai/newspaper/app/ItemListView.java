package com.github.ayltai.newspaper.app;

import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;

import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.util.ViewUtils;
import com.github.ayltai.newspaper.view.UniversalAdapter;
import com.github.ayltai.newspaper.widget.ListView;

public final class ItemListView extends ListView<Item> implements ItemListPresenter.View {
    //region Constructors

    public ItemListView(@NonNull final Context context) {
        super(context);
    }

    public ItemListView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public ItemListView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public ItemListView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr, @StyleRes final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    //endregion

    @LayoutRes
    @Override
    protected int getLayoutId() {
        return 0;
    }

    @LayoutRes
    @Override
    protected int getSwipeRefreshLayoutId() {
        return 0;
    }

    @IdRes
    @Override
    protected int getRecyclerViewId() {
        return 0;
    }

    @IdRes
    @Override
    protected int getLoadingViewId() {
        return 0;
    }

    @IdRes
    @Override
    protected int getEmptyViewId() {
        return 0;
    }

    @Override
    protected int getInfiniteLoadingThreshold() {
        return ListView.NO_INFINITE_LOADING;
    }

    @Override
    protected UniversalAdapter<Item, ?, ?> createAdapter() {
        return new ItemListAdapter.Builder(this.getContext())
            .addBinderFactory(new HeaderBinderFactory())
            .build();
    }

    @Override
    public void showLoadingView() {
        super.showLoadingView();

        ViewUtils.startShimmerAnimation(this.loadingView);
    }
}
