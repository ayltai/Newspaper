package com.github.ayltai.newspaper.app.view;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.app.ComponentFactory;
import com.github.ayltai.newspaper.app.data.ItemManager;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.data.DataManager;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.Lists;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.StringUtils;

import io.reactivex.Flowable;
import io.reactivex.Single;

public class HistoricalItemListPresenter extends ItemListPresenter {
    public HistoricalItemListPresenter(@NonNull final List<String> categories) {
        super(categories);
    }

    @NonNull
    @Override
    public Flowable<List<Item>> load() {
        if (this.getView() == null) return Flowable.just(Collections.emptyList());

        final Activity activity = this.getView().getActivity();
        if (activity == null) return Flowable.just(Collections.emptyList());

        return ItemManager.create(this.getView().getContext())
            .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER))
            .flatMap(
                manager -> manager.getHistoricalItems(ComponentFactory.getInstance()
                    .getConfigComponent(activity)
                    .userConfig()
                    .getSources()
                    .toArray(StringUtils.EMPTY_ARRAY), this.categories.toArray(StringUtils.EMPTY_ARRAY))
                .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER)))
            .map(items -> Lists.transform(items, item -> (Item)item))
            .flattenAsFlowable(Collections::singletonList);
    }

    @NonNull
    @Override
    public Single<Irrelevant> clearAll() {
        if (this.getView() == null) return Single.just(Irrelevant.INSTANCE);

        return ItemManager.create(this.getView().getContext())
            .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER))
            .flatMap(manager -> manager.clearHistories()
                .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER)));
    }
}
