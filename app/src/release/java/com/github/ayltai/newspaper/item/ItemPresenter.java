package com.github.ayltai.newspaper.item;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.google.firebase.analytics.FirebaseAnalytics;

import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.ShareEvent;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.util.AnalyticsUtils;

import io.realm.Realm;

public class ItemPresenter extends BaseItemPresenter {
    @Inject
    public ItemPresenter(@NonNull final Realm realm) {
        super(realm);
    }

    @Override
    protected void attachClicks() {
        if (this.getView().clicks() != null) this.subscriptions.add(this.getView().clicks().subscribe(dummy -> {
            if (this.parentKey != null) {
                this.getView().showItem(this.parentKey, this.item);

                this.answers().logContentView(AnalyticsUtils.applyAttributes(new ContentViewEvent(), this.item));
                this.analytics().logEvent(FirebaseAnalytics.Event.VIEW_ITEM, AnalyticsUtils.createBundle(this.item));
            }
        }, error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)));
    }

    @Override
    protected void attachBookmarks() {
        if (this.getView().bookmarks() != null) this.subscriptions.add(this.getView().bookmarks()
            .subscribe(bookmark -> this.getItemManager().getItems(null, this.parentKey == null ? new String[0] : new String[] { Constants.CATEGORY_BOOKMARK })
                .subscribe(items -> {
                    final int index = items.indexOf(this.item);
                    this.updateFeed(items, bookmark);
                    this.bus().send(Pair.create(index, this.item));

                    this.answers().logCustom(AnalyticsUtils.applyAttributes(new CustomEvent(bookmark ? Constants.ANALYTICS_BOOKMARK_ADD : Constants.ANALYTICS_BOOKMARK_REMOVE), this.item));
                    this.analytics().logEvent(bookmark ? Constants.ANALYTICS_BOOKMARK_ADD : Constants.ANALYTICS_BOOKMARK_REMOVE, AnalyticsUtils.createBundle(this.item));
                }, error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)), error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)));
    }

    @Override
    protected void attachShares() {
        if (this.getView().shares() != null) this.subscriptions.add(this.getView().shares().subscribe(dummy -> {
            if (this.item != null && this.item.getLink() != null) {
                this.getView().share(this.item.getLink());

                this.answers().logShare(AnalyticsUtils.applyAttributes(new ShareEvent(), this.item));
                this.analytics().logEvent(FirebaseAnalytics.Event.SHARE, AnalyticsUtils.createBundle(this.item));
            }
        }, error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)));
    }
}
