package com.github.ayltai.newspaper.app.view.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.app.view.ItemPresenter;
import com.github.ayltai.newspaper.app.view.MetaPresenter;
import com.github.ayltai.newspaper.app.widget.MetaView;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.view.binding.PresentationBinderFactory;

public final class MetaBinderFactory extends PresentationBinderFactory<Item, ItemPresenter.View, MetaPresenter> {
    @Override
    public int getPartType() {
        return MetaView.VIEW_TYPE;
    }

    @NonNull
    @Override
    protected MetaPresenter createPresenter() {
        return new MetaPresenter();
    }

    @Override
    public boolean isNeeded(@Nullable final Item model) {
        return model != null;
    }
}
