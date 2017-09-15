package com.github.ayltai.newspaper.app.view.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.app.view.ImagePresenter;
import com.github.ayltai.newspaper.app.widget.ImageView;
import com.github.ayltai.newspaper.app.view.ItemPresenter;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.view.binding.PresentationBinderFactory;

public final class ImageBinderFactory extends PresentationBinderFactory<Item, ItemPresenter.View, ImagePresenter> {
    @Override
    public int getPartType() {
        return ImageView.VIEW_TYPE;
    }

    @NonNull
    @Override
    protected ImagePresenter createPresenter() {
        return new ImagePresenter();
    }

    @Override
    public boolean isNeeded(@Nullable final Item model) {
        return model != null && !model.getImages().isEmpty();
    }
}
