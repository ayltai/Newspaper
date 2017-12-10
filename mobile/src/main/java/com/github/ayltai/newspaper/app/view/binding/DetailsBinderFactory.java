package com.github.ayltai.newspaper.app.view.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.view.DetailsPresenter;
import com.github.ayltai.newspaper.app.view.ItemPresenter;
import com.github.ayltai.newspaper.app.widget.DetailsView;
import com.github.ayltai.newspaper.view.binding.BindingPresenterFactory;

public final class DetailsBinderFactory extends BindingPresenterFactory<Item, DetailsPresenter.View, ItemPresenter<DetailsPresenter.View>> {
    @Override
    public int getPartType() {
        return DetailsView.VIEW_TYPE;
    }

    @NonNull
    @Override
    protected ItemPresenter<DetailsPresenter.View> createPresenter() {
        return new DetailsPresenter();
    }

    @Override
    public boolean isNeeded(@Nullable final Item model) {
        return model instanceof NewsItem;
    }
}
