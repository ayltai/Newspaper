package com.github.ayltai.newspaper.app.view.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.app.view.ItemPresenter;
import com.github.ayltai.newspaper.app.widget.HeaderView;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.view.binding.PresentationBinderFactory;

public final class HeaderBinderFactory extends PresentationBinderFactory<Item, HeaderView, ItemPresenter<HeaderView>> {
    @Override
    public int getPartType() {
        return HeaderView.VIEW_TYPE;
    }

    @NonNull
    @Override
    protected ItemPresenter<HeaderView> createPresenter() {
        return new ItemPresenter<>();
    }

    @Override
    public boolean isNeeded(@Nullable final Item model) {
        return model instanceof NewsItem;
    }
}
