package com.github.ayltai.newspaper.app.view;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.github.ayltai.newspaper.app.ComponentFactory;
import com.github.ayltai.newspaper.app.data.DetailsListLoader;
import com.github.ayltai.newspaper.app.data.model.Category;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.util.Lists;
import com.github.ayltai.newspaper.view.HorizontalListPresenter;
import com.github.ayltai.newspaper.view.ListPresenter;

import io.reactivex.Flowable;

public class DetailsListPresenter extends HorizontalListPresenter<Item, ListPresenter.View<Item>> {
    private String category;
    private int    itemPosition;

    public void setCategory(@Nullable final String category) {
        this.category = category;
    }

    public void setItemPosition(final int itemPosition) {
        this.itemPosition = itemPosition;
    }

    @NonNull
    @Override
    public Flowable<List<Item>> load() {
        if (this.getView() == null) return Flowable.just(Collections.emptyList());

        final Activity activity = this.getView().getActivity();
        if (activity == null) return Flowable.just(Collections.emptyList());

        final DetailsListLoader.Builder builder = new DetailsListLoader.Builder((AppCompatActivity)activity).setPosition(this.itemPosition);
        for (final String category : Category.fromDisplayName(this.category)) builder.addCategory(category);
        for (final String source : ComponentFactory.getInstance().getConfigComponent(activity).userConfig().getSources()) builder.addSource(source);

        return builder.build()
            .map(items -> Lists.transform(items, item -> (Item)item));
    }

    @Override
    public void onViewAttached(@NonNull final ListPresenter.View<Item> view, final boolean isFirstAttached) {
        super.onViewAttached(view, isFirstAttached);

        view.scrollTo(this.itemPosition, false);
    }
}
