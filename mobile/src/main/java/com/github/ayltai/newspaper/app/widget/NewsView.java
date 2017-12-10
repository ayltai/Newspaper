package com.github.ayltai.newspaper.app.widget;

import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.ComponentFactory;
import com.github.ayltai.newspaper.app.config.UserConfig;
import com.github.ayltai.newspaper.app.view.BaseNewsView;
import com.github.ayltai.newspaper.widget.BaseView;

public abstract class NewsView extends BaseView implements BaseNewsView {
    protected final String  category;
    protected final boolean isHistorical;
    protected final boolean isBookmarked;

    protected ItemListView listView;

    protected List<String> categories;
    protected Set<String>  sources;

    protected NewsView(@NonNull final Context context, @Nullable final String category, final boolean isHistorical, final boolean isBookmarked) {
        super(context);

        this.category     = category;
        this.isHistorical = isHistorical;
        this.isBookmarked = isBookmarked;

        this.init();
    }

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
    public void clear() {
        this.listView.clearAll();
    }

    @Override
    protected void init() {
        super.init();

        final ViewGroup view = (ViewGroup)LayoutInflater.from(this.getContext()).inflate(R.layout.view_news, this, true);

        this.listView = this.createItemListView();

        view.addView(this.listView);

        final Activity activity = this.getActivity();

        if (activity != null) {
            final UserConfig userConfig = ComponentFactory.getInstance()
                .getConfigComponent(activity)
                .userConfig();

            this.categories = userConfig.getCategories();
            this.sources    = userConfig.getSources();
        }
    }

    //endregion
}
