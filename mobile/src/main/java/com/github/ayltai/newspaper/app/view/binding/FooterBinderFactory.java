package com.github.ayltai.newspaper.app.view.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.app.view.FooterPresenter;
import com.github.ayltai.newspaper.app.widget.FooterView;
import com.github.ayltai.newspaper.app.view.ItemPresenter;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.view.binding.PresentationBinderFactory;

public class FooterBinderFactory extends PresentationBinderFactory<Item, ItemPresenter.View, FooterPresenter> {
    @Override
    public int getPartType() {
        return FooterView.VIEW_TYPE;
    }

    @NonNull
    @Override
    protected FooterPresenter createPresenter() {
        return new FooterPresenter();
    }

    @Override
    public boolean isNeeded(@Nullable final Item model) {
        return model != null && (model.getTitle() != null && !model.getTitle().isEmpty() || model.getDescription() != null && !model.getDescription().isEmpty());
    }
}
