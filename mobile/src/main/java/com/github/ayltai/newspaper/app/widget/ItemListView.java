package com.github.ayltai.newspaper.app.widget;

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

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.view.ItemListPresenter;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.util.ContextUtils;
import com.github.ayltai.newspaper.util.ViewUtils;
import com.github.ayltai.newspaper.widget.ListView;

public abstract class ItemListView extends ListView<Item> implements ItemListPresenter.View {
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
        return 0;
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

    private void init() {
        final int actionBarSize = ContextUtils.getDimensionPixelSize(this.getContext(), R.attr.actionBarSize);
        final int padding       = this.getContext().getResources().getDimensionPixelSize(R.dimen.space16);

        this.swipeRefreshLayout.setProgressViewOffset(false, actionBarSize, actionBarSize + padding * 2);
    }
}
