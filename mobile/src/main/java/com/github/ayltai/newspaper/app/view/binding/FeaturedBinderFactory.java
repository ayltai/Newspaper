package com.github.ayltai.newspaper.app.view.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.app.data.model.FeaturedItem;
import com.github.ayltai.newspaper.app.view.FeaturedPresenter;
import com.github.ayltai.newspaper.app.widget.FeaturedView;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.view.binding.PresentationBinderFactory;

public class FeaturedBinderFactory extends PresentationBinderFactory<Item, FeaturedView, FeaturedPresenter> {
    @Override
    public int getPartType() {
        return FeaturedView.VIEW_TYPE;
    }

    @NonNull
    @Override
    protected FeaturedPresenter createPresenter() {
        return new FeaturedPresenter();
    }

    @Override
    public boolean isNeeded(@Nullable final Item model) {
        return model instanceof FeaturedItem;
    }
}
