package com.github.ayltai.newspaper.app.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.github.ayltai.newspaper.analytics.ClickEvent;
import com.github.ayltai.newspaper.app.ComponentFactory;
import com.github.ayltai.newspaper.app.data.ItemListLoader;
import com.github.ayltai.newspaper.app.data.model.FeaturedItem;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.Lists;
import com.github.ayltai.newspaper.view.ListPresenter;

import io.reactivex.Flowable;
import io.reactivex.Single;

public class ItemListPresenter extends ListPresenter<Item, ListPresenter.View<Item>> {
    protected final List<String> categories;

    private boolean forceRefresh = false;

    public ItemListPresenter(@NonNull final List<String> categories) {
        this.categories = categories;
    }

    @Override
    protected void onPullToRefresh() {
        super.onPullToRefresh();

        if (this.getView() != null) ComponentFactory.getInstance()
            .getAnalyticsComponent(this.getView().getContext())
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("Pull-To-Refresh"));
    }

    @Override
    protected void resetState() {
        super.resetState();

        this.forceRefresh = true;
    }

    @NonNull
    @Override
    public Flowable<List<Item>> load() {
        if (this.getView() == null) return Flowable.just(Collections.emptyList());

        final Activity activity = this.getView().getActivity();
        if (activity == null) return Flowable.just(Collections.emptyList());

        final ItemListLoader.Builder builder = new ItemListLoader.Builder((AppCompatActivity)activity).forceRefresh(this.forceRefresh);
        for (final String category : this.categories) builder.addCategory(category);
        for (final String source : ComponentFactory.getInstance().getConfigComponent(activity).userConfig().getSources()) builder.addSource(source);

        return builder.build()
            .map(items -> Lists.transform(items, item -> (Item)item))
            .map(items -> {
                final List<Item> featuredItems = new ArrayList<>(items);
                final Item       featuredItem  = FeaturedItem.create(featuredItems);

                if (featuredItem != null) featuredItems.add(0, featuredItem);

                return featuredItems;
            });
    }

    @NonNull
    @Override
    public Single<Irrelevant> clearAll() {
        return Single.just(Irrelevant.INSTANCE);
    }
}
