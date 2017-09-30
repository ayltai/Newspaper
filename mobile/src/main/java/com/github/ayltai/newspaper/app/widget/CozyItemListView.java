package com.github.ayltai.newspaper.app.widget;

import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.view.ItemListAdapter;
import com.github.ayltai.newspaper.app.view.binding.FeaturedBinderFactory;
import com.github.ayltai.newspaper.app.view.binding.FooterBinderFactory;
import com.github.ayltai.newspaper.app.view.binding.HeaderBinderFactory;
import com.github.ayltai.newspaper.app.view.binding.ImageBinderFactory;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.view.UniversalAdapter;

public final class CozyItemListView extends ItemListView {
    //region Constructors

    public CozyItemListView(@NonNull final Context context) {
        super(context);
    }

    public CozyItemListView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public CozyItemListView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public CozyItemListView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr, @StyleRes final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    //endregion

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
            .addBinderFactory(new HeaderBinderFactory())
            .addBinderFactory(new ImageBinderFactory())
            .addBinderFactory(new FooterBinderFactory())
            .build();
    }
}
