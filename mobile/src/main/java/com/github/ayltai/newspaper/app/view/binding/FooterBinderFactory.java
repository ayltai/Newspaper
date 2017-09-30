package com.github.ayltai.newspaper.app.view.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.app.view.ItemPresenter;
import com.github.ayltai.newspaper.app.widget.FooterView;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.view.binding.PresentationBinderFactory;

public class FooterBinderFactory extends PresentationBinderFactory<Item, FooterView, ItemPresenter<FooterView>> {
    @Override
    public int getPartType() {
        return FooterView.VIEW_TYPE;
    }

    @NonNull
    @Override
    protected ItemPresenter<FooterView> createPresenter() {
        return new ItemPresenter<>();
    }

    @Override
    public boolean isNeeded(@Nullable final Item model) {
        return model instanceof NewsItem && (model.getTitle() != null && !model.getTitle().isEmpty() || model.getDescription() != null && !model.getDescription().isEmpty());
    }
}
