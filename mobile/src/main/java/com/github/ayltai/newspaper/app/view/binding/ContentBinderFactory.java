package com.github.ayltai.newspaper.app.view.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.app.view.ContentPresenter;
import com.github.ayltai.newspaper.app.view.ItemPresenter;
import com.github.ayltai.newspaper.app.widget.ContentView;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.view.binding.PresentationBinderFactory;

public final class ContentBinderFactory extends PresentationBinderFactory<Item, ItemPresenter.View, ContentPresenter> {
    @Override
    public int getPartType() {
        return ContentView.VIEW_TYPE;
    }

    @NonNull
    @Override
    protected ContentPresenter createPresenter() {
        return new ContentPresenter();
    }

    @Override
    public boolean isNeeded(@Nullable final Item model) {
        return model != null;
    }
}
