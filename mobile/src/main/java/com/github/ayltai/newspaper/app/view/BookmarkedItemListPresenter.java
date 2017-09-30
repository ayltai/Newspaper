package com.github.ayltai.newspaper.app.view;

import java.util.Collections;
import java.util.List;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.app.data.ItemManager;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.config.UserConfig;
import com.github.ayltai.newspaper.data.DataManager;
import com.github.ayltai.newspaper.util.Lists;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.StringUtils;

import io.reactivex.Flowable;

public class BookmarkedItemListPresenter extends ItemListPresenter {
    public BookmarkedItemListPresenter(@NonNull final List<String> categories) {
        super(categories);
    }

    @NonNull
    @Override
    public Flowable<List<Item>> load() {
        if (this.getView() == null) return Flowable.just(Collections.emptyList());

        return ItemManager.create(this.getView().getContext())
            .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER))
            .flatMap(manager -> manager.getBookmarkedItems(UserConfig.getSources(this.getView().getContext()).toArray(StringUtils.EMPTY_ARRAY), this.categories.toArray(StringUtils.EMPTY_ARRAY))
                .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER)))
            .map(items -> Lists.transform(items, item -> (Item)item))
            .flattenAsFlowable(Collections::singletonList);
    }
}
