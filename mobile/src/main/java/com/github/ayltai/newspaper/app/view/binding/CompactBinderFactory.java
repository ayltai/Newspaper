package com.github.ayltai.newspaper.app.view.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.view.ItemPresenter;
import com.github.ayltai.newspaper.app.widget.CompactItemView;
import com.github.ayltai.newspaper.view.binding.BindingPresenterFactory;

public final class CompactBinderFactory extends BindingPresenterFactory<Item, CompactItemView, ItemPresenter<CompactItemView>> {
    @Override
    public int getPartType() {
        return CompactItemView.VIEW_TYPE;
    }

    @NonNull
    @Override
    protected ItemPresenter<CompactItemView> createPresenter() {
        return new ItemPresenter<>();
    }

    @Override
    public boolean isNeeded(@Nullable final Item model) {
        return model instanceof NewsItem;
    }
}
