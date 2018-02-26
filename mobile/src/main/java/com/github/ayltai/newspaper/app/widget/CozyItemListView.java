package com.github.ayltai.newspaper.app.widget;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.view.ItemListAdapter;
import com.github.ayltai.newspaper.app.view.binding.CozyBinderFactory;
import com.github.ayltai.newspaper.app.view.binding.FeaturedBinderFactory;
import com.github.ayltai.newspaper.view.UniversalAdapter;

public class CozyItemListView extends ItemListView {
    public CozyItemListView(@NonNull final Context context) {
        super(context);
    }

    @LayoutRes
    @Override
    protected int getLayoutId() {
        return R.layout.view_list_cozy;
    }

    @NonNull
    @Override
    protected UniversalAdapter<Item, ?, ?> createAdapter() {
        return new ItemListAdapter.Builder(this.getContext())
            .addBinderFactory(new FeaturedBinderFactory())
            .addBinderFactory(new CozyBinderFactory())
            .build();
    }
}
