package com.github.ayltai.newspaper.app.view.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.view.ItemPresenter;
import com.github.ayltai.newspaper.app.widget.CozyItemView;
import com.github.ayltai.newspaper.view.binding.PresentationBinderFactory;

public final class CozyBinderFactory extends PresentationBinderFactory<Item, CozyItemView, ItemPresenter<CozyItemView>> {
    @Override
    public int getPartType() {
        return CozyItemView.VIEW_TYPE;
    }

    @NonNull
    @Override
    protected ItemPresenter<CozyItemView> createPresenter() {
        return new ItemPresenter<>();
    }

    @Override
    public boolean isNeeded(@Nullable final Item model) {
        return model instanceof NewsItem;
    }
}
