package com.github.ayltai.newspaper.app;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.view.binding.PresentationBinderFactory;

public final class HeaderBinderFactory extends PresentationBinderFactory<Item, ItemPresenter.View, HeaderPresenter> {
    @Override
    public int getPartType() {
        return HeaderView.VIEW_TYPE;
    }

    @NonNull
    @Override
    protected HeaderPresenter createPresenter() {
        return new HeaderPresenter();
    }

    @Override
    public boolean isNeeded(@Nullable final Item model) {
        return model != null;
    }
}
