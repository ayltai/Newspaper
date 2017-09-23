package com.github.ayltai.newspaper.app.view.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.app.view.ItemPresenter;
import com.github.ayltai.newspaper.app.widget.MetaView;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.data.model.NewsItem;
import com.github.ayltai.newspaper.view.binding.PresentationBinderFactory;

public final class MetaBinderFactory extends PresentationBinderFactory<Item, MetaView, ItemPresenter<MetaView>> {
    @Override
    public int getPartType() {
        return MetaView.VIEW_TYPE;
    }

    @NonNull
    @Override
    protected ItemPresenter<MetaView> createPresenter() {
        return new ItemPresenter<>();
    }

    @Override
    public boolean isNeeded(@Nullable final Item model) {
        return model instanceof NewsItem;
    }
}
