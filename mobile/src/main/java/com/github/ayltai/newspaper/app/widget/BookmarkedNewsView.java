package com.github.ayltai.newspaper.app.widget;

import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Filter;
import android.widget.Filterable;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.analytics.SearchEvent;
import com.github.ayltai.newspaper.app.ComponentFactory;
import com.github.ayltai.newspaper.app.config.UserConfig;
import com.github.ayltai.newspaper.app.view.BookmarkedItemListPresenter;
import com.github.ayltai.newspaper.app.view.ItemListAdapter;
import com.github.ayltai.newspaper.app.view.ItemListPresenter;
import com.github.ayltai.newspaper.util.DevUtils;

public final class BookmarkedNewsView extends NewsView {
    public BookmarkedNewsView(@NonNull final Context context) {
        super(context);
    }

    @NonNull
    @Override
    public ItemListView createItemListView() {
        final Activity   activity   = this.getActivity();
        final UserConfig userConfig = activity == null
            ? null
            : ComponentFactory.getInstance()
                .getConfigComponent(activity)
                .userConfig();

        final ItemListPresenter presenter = new BookmarkedItemListPresenter(userConfig == null ? Collections.emptyList() : userConfig.getCategories());

        final ItemListView view = userConfig == null || userConfig.getViewStyle() == Constants.VIEW_STYLE_COZY
            ? new CozyItemListView(this.getContext()) {
                @Override
                protected int getLayoutId() {
                    return R.layout.view_list_cozy_local;
                }

                @Override
                protected int getLoadingViewId() {
                    return 0;
                }

                @Override
                protected int getEmptyTitle() {
                    return R.string.empty_bookmark_title;
                }

                @Override
                protected int getEmptyDescription() {
                    return R.string.empty_bookmark_description;
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

                @Override
                protected int getEmptyTitle() {
                    return R.string.empty_bookmark_title;
                }

                @Override
                protected int getEmptyDescription() {
                    return R.string.empty_bookmark_description;
                }
            };

        view.attaches().subscribe(
            isFirstTimeAttachment -> presenter.onViewAttached(view, isFirstTimeAttachment),
            error -> {
                if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
            }
        );

        view.detaches().subscribe(
            irrelevant -> presenter.onViewDetached(),
            error -> {
                if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
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

        if (!TextUtils.isEmpty(newText)) ComponentFactory.getInstance()
            .getAnalyticsComponent(this.getContext())
            .eventLogger()
            .logEvent(new SearchEvent()
                .setQuery(newText.toString())
                .setScreenName(this.getClass().getSimpleName()));
    }
}
