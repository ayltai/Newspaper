package com.github.ayltai.newspaper.item;

import java.util.Collections;
import java.util.Date;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.Presenter;
import com.github.ayltai.newspaper.data.Feed;
import com.github.ayltai.newspaper.data.FeedManager;
import com.github.ayltai.newspaper.list.ListScreen;
import com.github.ayltai.newspaper.rss.Item;
import com.github.ayltai.newspaper.util.ItemUtils;

import io.realm.Realm;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

public class ItemPresenter extends Presenter<ItemPresenter.View> {
    public interface View extends Presenter.View {
        void setTitle(@Nullable String title);

        void setDescription(@Nullable String description);

        void setSource(@Nullable String source);

        void setLink(@Nullable String link);

        void setPublishDate(long publishDate);

        void setThumbnail(@Nullable String thumbnail, @Constants.ListViewType int type);

        void setIsBookmarked(boolean isBookmarked);

        @Nullable Observable<Void> clicks();

        @Nullable Observable<Void> zooms();

        @Nullable Observable<Boolean> bookmarks();

        void showItem(@NonNull ListScreen.Key parentKey, @NonNull Item item);

        void showOriginalMedia(@NonNull String url);
    }

    //region Variables

    private final Realm realm;

    private CompositeSubscription subscriptions;
    private ListScreen.Key        parentKey;
    private Item                  item;
    private int                   type = Constants.LIST_VIEW_TYPE_DEFAULT;

    //endregion

    public ItemPresenter(@NonNull final Realm realm) {
        this.realm = realm;
    }

    public final void bind(@Nullable final ListScreen.Key parentKey, @NonNull final Item item, @Constants.ListViewType final int type) {
        this.parentKey = parentKey;
        this.item      = item;
        this.type      = type;

        if (this.isViewAttached()) {
            this.getView().setTitle(this.item.getTitle());
            this.getView().setDescription(this.item.getDescription());
            this.getView().setSource(this.item.getSource());
            this.getView().setLink(this.item.getLink());
            this.getView().setThumbnail(this.item.getMediaUrl(), this.type);

            if (this.getView().bookmarks() != null) {
                this.getFeedManager().getFeed(Constants.SOURCE_BOOKMARK)
                    .subscribe(feed -> this.getView().setIsBookmarked(feed.contains(this.item)), error -> FirebaseCrash.logcat(Log.ERROR, this.getClass().getName(), error.getMessage()));
            }

            final Date publishDate = this.item.getPublishDate();
            this.getView().setPublishDate(publishDate == null ? 0 : publishDate.getTime());
        }
    }

    //region Lifecycle

    @Override
    public final void onViewAttached(@NonNull final ItemPresenter.View view) {
        super.onViewAttached(view);

        if (this.item != null) this.bind(this.parentKey, this.item, this.type);

        this.attachEvents();
    }

    @Override
    public final void onViewDetached() {
        super.onViewDetached();

        if (this.subscriptions != null && this.subscriptions.hasSubscriptions()) {
            this.subscriptions.unsubscribe();
            this.subscriptions = null;
        }
    }

    //endregion

    @VisibleForTesting
    FeedManager getFeedManager() {
        return new FeedManager(this.realm);
    }

    @VisibleForTesting
    void updateFeed(@NonNull final Feed feed, final boolean bookmark) {
        final int index = feed.indexOf(this.item);

        this.realm.beginTransaction();

        if (bookmark) {
            if (index == -1) {
                feed.getItems().add(this.item);
                Collections.sort(feed.getItems());
            }
        } else {
            if (index > -1) feed.getItems().remove(index);
        }

        this.realm.copyToRealmOrUpdate(feed);
        this.realm.commitTransaction();
    }

    private void attachEvents() {
        if (this.subscriptions == null) this.subscriptions = new CompositeSubscription();

        if (this.getView().clicks() != null) this.subscriptions.add(this.getView().clicks().subscribe(dummy -> {
            if (this.parentKey != null) this.getView().showItem(this.parentKey, this.item);
        }, error -> FirebaseCrash.logcat(Log.ERROR, this.getClass().getName(), error.getMessage())));

        if (this.getView().zooms() != null) this.subscriptions.add(this.getView().zooms().subscribe(dummy -> {
            if (this.item != null && this.item.getMediaUrl() != null) this.getView().showOriginalMedia(ItemUtils.getOriginalMediaUrl(this.item.getMediaUrl()));
        }, error -> FirebaseCrash.logcat(Log.ERROR, this.getClass().getName(), error.getMessage())));

        if (this.getView().bookmarks() != null) this.subscriptions.add(this.getView().bookmarks().subscribe(bookmark -> this.getFeedManager().getFeed(Constants.SOURCE_BOOKMARK).subscribe(feed -> this.updateFeed(feed, bookmark), error -> FirebaseCrash.logcat(Log.ERROR, this.getClass().getName(), error.getMessage())), error -> FirebaseCrash.logcat(Log.ERROR, this.getClass().getName(), error.getMessage())));
    }
}
