package com.github.ayltai.newspaper.item;

import java.util.Collections;
import java.util.Date;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.Configs;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.Presenter;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.data.Feed;
import com.github.ayltai.newspaper.data.FeedManager;
import com.github.ayltai.newspaper.list.ListScreen;
import com.github.ayltai.newspaper.rss.Item;
import com.github.ayltai.newspaper.util.ItemUtils;

import io.realm.Realm;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public abstract class BaseItemPresenter extends Presenter<BaseItemPresenter.View> {
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

        @Nullable Observable<Void> shares();

        void showItem(@NonNull ListScreen.Key parentKey, @NonNull Item item);

        void showOriginalMedia(@NonNull String url);

        void share(@NonNull String url);
    }

    //region Variables

    private final Realm realm;

    protected CompositeSubscription subscriptions;
    protected ListScreen.Key        parentKey;
    protected Item                  item;

    private int     type = Configs.getDefaultListViewType();
    private boolean isBound;
    private boolean showFullDescription;

    //endregion

    protected BaseItemPresenter(@NonNull final Realm realm) {
        this.realm = realm;
    }

    public final void bind(@Nullable final ListScreen.Key parentKey, @NonNull final Item item, @Constants.ListViewType final int type, final boolean showFullDescription) {
        this.parentKey           = parentKey;
        this.item                = item;
        this.type                = type;
        this.showFullDescription = showFullDescription;

        if (this.isViewAttached() && !this.isBound) {
            this.isBound = true;

            if (BuildConfig.DEBUG) this.log().d(this.getClass().getName(), "guid = " + this.item.getGuid());

            this.getView().setTitle(this.item.getTitle());
            this.getView().setDescription(this.showFullDescription ? this.getView().getContext().getString(R.string.loading_indicator) : this.item.getDescription());
            this.getView().setSource(this.item.getSource());
            this.getView().setLink(this.item.getLink());
            this.getView().setThumbnail(this.item.getMediaUrl(), this.type);

            if (this.getView().bookmarks() != null) {
                this.getFeedManager()
                    .getFeed(Constants.SOURCE_BOOKMARK)
                    .subscribe(
                        feed -> this.getView().setIsBookmarked(feed.contains(this.item)),
                        error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error));
            }

            final Date publishDate = this.item.getPublishDate();
            this.getView().setPublishDate(publishDate == null ? 0 : publishDate.getTime());

            if (this.subscriptions == null) this.subscriptions = new CompositeSubscription();

            if (this.showFullDescription) this.subscriptions.add(ItemUtils.getFullDescription(this.getView().getContext(), this.item)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                    description -> this.getView().setDescription(description),
                    error -> this.log().w(this.getClass().getSimpleName(), error.getMessage(), error)));
        }
    }

    //region Lifecycle

    @Override
    public final void onViewAttached(@NonNull final BaseItemPresenter.View view) {
        super.onViewAttached(view);

        if (this.item != null && !this.isBound) this.bind(this.parentKey, this.item, this.type, this.showFullDescription);

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
    @NonNull
    /* public final */ FeedManager getFeedManager() {
        return new FeedManager(this.realm);
    }

    @VisibleForTesting
    /* public final */ void updateFeed(@NonNull final Feed feed, final boolean bookmark) {
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

    //region Event handlers

    private void attachEvents() {
        if (this.subscriptions == null) this.subscriptions = new CompositeSubscription();

        this.attachClicks();
        this.attachZooms();
        this.attachBookmarks();
        this.attachShares();
    }

    protected abstract void attachClicks();

    private void attachZooms() {
        if (this.getView().zooms() != null) this.subscriptions.add(this.getView().zooms().subscribe(dummy -> {
            if (this.item != null && this.item.getMediaUrl() != null) this.getView().showOriginalMedia(ItemUtils.getOriginalMediaUrl(this.item.getMediaUrl()));
        }, error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)));
    }

    protected abstract void attachBookmarks();

    protected abstract void attachShares();

    //endregion
}
