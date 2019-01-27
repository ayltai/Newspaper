package com.github.ayltai.newspaper.widget;

import javax.annotation.Nonnull;

import android.content.Context;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;

public class CozyListView extends ListView {
    public CozyListView(@Nonnull @NonNull @lombok.NonNull final Context context) {
        super(context);
    }

    @LayoutRes
    @Override
    protected int getLayoutId() {
        return R.layout.view_list_cozy;
    }

    @IdRes
    @Override
    protected int getRecyclerViewId() {
        return R.id.recyclerView;
    }

    @IdRes
    @Override
    protected int getSwipeRefreshLayoutId() {
        return R.id.swipeRefreshLayout;
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

    @IdRes
    @Override
    protected int getLoadingViewId() {
        return R.id.loading;
    }

    @Nonnull
    @NonNull
    @Override
    protected ListAdapter createAdapter() {
        return new ListAdapter(Constants.STYLE_COMFORTABLE);
    }
}
