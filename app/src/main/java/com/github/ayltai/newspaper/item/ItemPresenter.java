package com.github.ayltai.newspaper.item;

import java.util.Collections;
import java.util.Date;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.ShareEvent;
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

        @Nullable Observable<Void> shares();

        void showItem(@NonNull ListScreen.Key parentKey, @NonNull Item item);

        void showOriginalMedia(@NonNull String url);

        void share(@NonNull String url);
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
                    .subscribe(feed -> this.getView().setIsBookmarked(feed.contains(this.item)), error -> this.log().e(this.getClass().getName(), error.getMessage(), error));
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

    @VisibleForTesting
    Answers getAnswers() {
        return Answers.getInstance();
    }

    //region Event handlers

    private void attachEvents() {
        if (this.subscriptions == null) this.subscriptions = new CompositeSubscription();

        this.attachClicks();
        this.attachZooms();
        this.attachBookmarks();
        this.attachShares();
    }

    private void attachClicks() {
        if (this.getView().clicks() != null) this.subscriptions.add(this.getView().clicks().subscribe(dummy -> {
            if (this.parentKey != null) {
                this.getView().showItem(this.parentKey, this.item);

                this.getAnswers().logContentView(new ContentViewEvent()
                    .putContentId(this.item.getGuid())
                    .putContentName(this.item.getTitle())
                    .putContentType(this.item.getClass().getName()));
            }
        }, error -> this.log().e(this.getClass().getName(), error.getMessage(), error)));
    }

    private void attachZooms() {
        if (this.getView().zooms() != null) this.subscriptions.add(this.getView().zooms().subscribe(dummy -> {
            if (this.item != null && this.item.getMediaUrl() != null) this.getView().showOriginalMedia(ItemUtils.getOriginalMediaUrl(this.item.getMediaUrl()));
        }, error -> this.log().e(this.getClass().getName(), error.getMessage(), error)));
    }

    private void attachBookmarks() {
        if (this.getView().bookmarks() != null) this.subscriptions.add(this.getView().bookmarks().subscribe(bookmark -> this.getFeedManager().getFeed(Constants.SOURCE_BOOKMARK).subscribe(feed -> {
            this.updateFeed(feed, bookmark);

            this.getAnswers().logCustom(new CustomEvent(bookmark ? "Bookmark (Add)" : "Bookmark (Remove)")
                .putCustomAttribute("contentId", this.item.getGuid())
                .putCustomAttribute("contentName", this.item.getTitle())
                .putCustomAttribute("contentType", this.item.getClass().getName()));
        }, error -> this.log().e(this.getClass().getName(), error.getMessage(), error)), error -> this.log().e(this.getClass().getName(), error.getMessage(), error)));
    }

    private void attachShares() {
        if (this.getView().shares() != null) this.subscriptions.add(this.getView().shares().subscribe(dummy -> {
            if (this.item != null && this.item.getLink() != null) {
                this.getView().share(this.item.getLink());

                this.getAnswers().logShare(new ShareEvent()
                    .putContentId(this.item.getGuid())
                    .putContentName(this.item.getTitle())
                    .putContentType(this.item.getClass().getName()));
            }
        }, error -> this.log().e(this.getClass().getName(), error.getMessage(), error)));
    }

    //endregion
}
