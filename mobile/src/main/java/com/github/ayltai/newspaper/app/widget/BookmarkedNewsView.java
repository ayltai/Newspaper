package com.github.ayltai.newspaper.app.widget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.util.Log;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.app.view.BookmarkedItemListPresenter;
import com.github.ayltai.newspaper.app.view.ItemListPresenter;
import com.github.ayltai.newspaper.config.UserConfig;
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
        final ItemListView      view      = UserConfig.getViewStyle(this.getContext()) == Constants.VIEW_STYLE_COZY ? new CozyItemListView(this.getContext()) : new CompactItemListView(this.getContext());
        final ItemListPresenter presenter = new BookmarkedItemListPresenter(UserConfig.getCategories(this.getContext()));

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
}
