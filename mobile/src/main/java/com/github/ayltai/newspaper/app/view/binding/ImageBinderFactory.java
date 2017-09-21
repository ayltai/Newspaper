package com.github.ayltai.newspaper.app.view.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.app.data.model.FeaturedItem;
import com.github.ayltai.newspaper.app.view.ItemPresenter;
import com.github.ayltai.newspaper.app.widget.ImageView;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.view.binding.PresentationBinderFactory;

public final class ImageBinderFactory extends PresentationBinderFactory<Item, ImageView, ItemPresenter<ImageView>> {
    @Override
    public int getPartType() {
        return ImageView.VIEW_TYPE;
    }

    @NonNull
    @Override
    protected ItemPresenter<ImageView> createPresenter() {
        return new ItemPresenter<>();
    }

    @Override
    public boolean isNeeded(@Nullable final Item model) {
        return !(model instanceof FeaturedItem) && model != null && !model.getImages().isEmpty();
    }
}
