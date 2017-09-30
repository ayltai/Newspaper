package com.github.ayltai.newspaper.app.widget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.view.NewsPresenter;
import com.github.ayltai.newspaper.widget.ObservableView;

public abstract class NewsView extends ObservableView implements NewsPresenter.View {
    private ItemListView listView;

    //region Constructors

    public NewsView(@NonNull final Context context) {
        super(context);
        this.init();
    }

    public NewsView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public NewsView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    public NewsView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr, @StyleRes final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init();
    }

    //endregion

    //region Methods

    @NonNull
    public abstract ItemListView createItemListView();

    @Override
    public void up() {
        this.listView.up();
    }

    @Override
    public void refresh() {
        this.listView.refresh();
    }

    @Override
    public void search(@Nullable final CharSequence newText) {
        if (this.listView.getAdapter() instanceof Filterable) {
            final Filter filter = ((Filterable)this.listView.getAdapter()).getFilter();
            if (filter != null) filter.filter(newText);
        }
    }

    private void init() {
        final ViewGroup view = (ViewGroup)LayoutInflater.from(this.getContext()).inflate(R.layout.view_news, this, true);

        this.listView = this.createItemListView();

        view.addView(this.listView);
    }

    //endregion
}
