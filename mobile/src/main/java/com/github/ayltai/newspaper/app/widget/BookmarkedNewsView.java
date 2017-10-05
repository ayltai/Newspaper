package com.github.ayltai.newspaper.app.widget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Filter;
import android.widget.Filterable;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.analytics.AnalyticsModule;
import com.github.ayltai.newspaper.analytics.DaggerAnalyticsComponent;
import com.github.ayltai.newspaper.analytics.SearchEvent;
import com.github.ayltai.newspaper.app.config.UserConfig;
import com.github.ayltai.newspaper.app.view.BookmarkedItemListPresenter;
import com.github.ayltai.newspaper.app.view.ItemListAdapter;
import com.github.ayltai.newspaper.app.view.ItemListPresenter;
import com.github.ayltai.newspaper.util.TestUtils;

public final class BookmarkedNewsView extends NewsView {
    //region Constructors

    public BookmarkedNewsView(@NonNull final Context context) {
        super(context);
    }

    public BookmarkedNewsView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public BookmarkedNewsView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BookmarkedNewsView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr, @StyleRes final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    //endregion

    @NonNull
    @Override
    public ItemListView createItemListView() {
        final ItemListPresenter presenter = new BookmarkedItemListPresenter(UserConfig.getCategories(this.getContext()));

        final ItemListView view = UserConfig.getViewStyle(this.getContext()) == Constants.VIEW_STYLE_COZY
            ? new CozyItemListView(this.getContext()) {
                @Override
                protected int getLayoutId() {
                    return R.layout.view_list_cozy_local;
                }

                @Override
                    protected int getLoadingViewId() {
                    return 0;
                }
                }
            : new CompactItemListView(this.getContext()) {
                @Override
                protected int getLayoutId() {
                    return R.layout.view_list_compact_local;
                }

                @Override
                protected int getLoadingViewId() {
                    return 0;
                }
            };

        view.attachments().subscribe(
            isFirstTimeAttachment -> presenter.onViewAttached(view, isFirstTimeAttachment),
            error -> {
                if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
            }
        );

        view.detachments().subscribe(
            irrelevant -> presenter.onViewDetached(),
            error -> {
                if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
            }
        );

        return view;
    }

    @Override
    public void search(@Nullable final CharSequence newText) {
        if (this.listView.getAdapter() instanceof Filterable) {
            final Filter filter = ((Filterable)this.listView.getAdapter()).getFilter();

            if (filter instanceof ItemListAdapter.ItemListFilter) {
                final ItemListAdapter.ItemListFilter itemListFilter = (ItemListAdapter.ItemListFilter)filter;

                itemListFilter.setCategories(this.categories);
                itemListFilter.setSources(this.sources);
                itemListFilter.setBookmarked(true);
            }

            if (filter != null) filter.filter(newText);
        }

        if (!TextUtils.isEmpty(newText)) DaggerAnalyticsComponent.builder()
            .analyticsModule(new AnalyticsModule(this.getContext()))
            .build()
            .eventLogger()
            .logEvent(new SearchEvent()
                .setQuery(newText.toString())
                .setScreenName(this.getClass().getSimpleName()));
    }
}
