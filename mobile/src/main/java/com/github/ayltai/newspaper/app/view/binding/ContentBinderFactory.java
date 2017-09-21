package com.github.ayltai.newspaper.app.view.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.app.view.ItemPresenter;
import com.github.ayltai.newspaper.app.widget.ContentView;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.view.binding.PresentationBinderFactory;

public final class ContentBinderFactory extends PresentationBinderFactory<Item, ContentView, ItemPresenter<ContentView>> {
    @Override
    public int getPartType() {
        return ContentView.VIEW_TYPE;
    }

    @NonNull
    @Override
    protected ItemPresenter<ContentView> createPresenter() {
        return new ItemPresenter<>();
    }

    @Override
    public boolean isNeeded(@Nullable final Item model) {
        return model != null;
    }
}
