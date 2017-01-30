package com.github.ayltai.newspaper.item;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.github.ayltai.newspaper.Constants;

import io.realm.Realm;

public class ItemPresenter extends BaseItemPresenter {
    @Inject
    public ItemPresenter(@NonNull final Realm realm) {
        super(realm);
    }

    @Override
    protected void attachClicks() {
        if (this.getView().clicks() != null) this.subscriptions.add(this.getView().clicks().subscribe(dummy -> {
            if (this.parentKey != null) this.getView().showItem(this.parentKey, this.item);
        }, error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)));
    }

    @Override
    protected void attachBookmarks() {
        if (this.getView().bookmarks() != null) this.subscriptions.add(this.getView().bookmarks()
            .subscribe(bookmark -> this.getFeedManager().getFeed(Constants.SOURCE_BOOKMARK)
                .subscribe(feed -> {
                    final int index = feed.indexOf(this.item);
                    this.updateFeed(feed, bookmark);
                    this.bus().send(Pair.create(index, this.item));
                }, error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)), error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)));
    }

    @Override
    protected void attachShares() {
        if (this.getView().shares() != null) this.subscriptions.add(this.getView().shares().subscribe(dummy -> {
            if (this.item != null && this.item.getLink() != null) this.getView().share(this.item.getLink());
        }, error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)));
    }
}
