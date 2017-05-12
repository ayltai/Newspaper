package com.github.ayltai.newspaper.item;

import java.util.Collections;

import javax.inject.Inject;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.util.ItemUtils;

import io.realm.Realm;

public class ItemPresenter extends BaseItemPresenter {
    @Inject
    public ItemPresenter(@NonNull final Realm realm) {
        super(realm);
    }

    @Override
    protected void attachClicks() {
        if (this.getView().clicks() != null) this.disposables.add(this.getView()
            .clicks()
            .subscribe(
                dummy -> {
                    if (this.parentKey != null) this.getView().showItem(this.parentKey, this.item);
                },
                error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)));
    }

    @Override
    protected void attachBookmarks() {
        if (this.getView().bookmarks() != null) this.disposables.add(this.getView()
            .bookmarks()
            .subscribe(
                bookmark -> this.getItemManager().getItemsSingle(Collections.emptyList(), this.parentKey == null ? Collections.emptyList() : Collections.singletonList(this.parentKey.getCategory()))
                    .subscribe(
                        items -> {
                            this.update(bookmark);

                            this.bus().send(new ItemUpdatedEvent(ItemUtils.indexOf(items, this.item), this.item));
                        },
                        error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)),
                error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)));
    }

    @Override
    protected void attachShares() {
        if (this.getView().shares() != null) this.disposables.add(this.getView()
            .shares()
            .subscribe(
                dummy -> {
                    if (this.item != null) this.getView().share(this.item.getLink());
                },
                error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)));
    }
}
