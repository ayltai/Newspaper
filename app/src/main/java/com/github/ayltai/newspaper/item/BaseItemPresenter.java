package com.github.ayltai.newspaper.item;

import java.util.Date;
import java.util.List;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.Configs;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.Presenter;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.client.ClientFactory;
import com.github.ayltai.newspaper.data.ItemManager;
import com.github.ayltai.newspaper.list.ListScreen;
import com.github.ayltai.newspaper.main.ImagesUpdatedEvent;
import com.github.ayltai.newspaper.model.Image;
import com.github.ayltai.newspaper.model.Item;

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

        void setThumbnails(@NonNull List<Image> images);

        void setIsBookmarked(boolean isBookmarked);

        @Nullable Observable<Void> clicks();

        @Nullable Observable<Integer> zooms();

        @Nullable Observable<Boolean> bookmarks();

        @Nullable Observable<Void> shares();

        void showItem(@NonNull ListScreen.Key parentKey, @NonNull Item item);

        void showMedia(@NonNull String url);

        void share(@NonNull String url);
    }

    //region Variables

    private final Realm realm;

    protected CompositeSubscription subscriptions;
    protected ListScreen.Key        parentKey;
    protected Item                  item;

    private int     type = Configs.getDefaultListViewType();
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

        if (this.isViewAttached()) {
            if (BuildConfig.DEBUG) this.log().d(this.getClass().getName(), "link = " + this.item.getLink());

            if (this.getView() == null) throw new NullPointerException("View is null");

            this.getView().setTitle(this.item.getTitle());
            this.getView().setDescription(this.showFullDescription && !this.item.isFullDescription() ? this.getView().getContext().getString(R.string.loading_indicator) : this.item.getDescription());
            this.getView().setSource(this.item.getSource());
            this.getView().setLink(this.item.getLink());
            this.getView().setThumbnail(this.item.getImages().isEmpty() ? null : this.item.getImages().first().getUrl(), this.type);
            this.getView().setThumbnails(this.item.getImages());

            if (this.getView().bookmarks() != null) {
                this.getItemManager()
                    .getItems(null, parentKey == null ? new String[0] : new String[] { parentKey.getCategory() })
                    .subscribe(
                        items -> this.getView().setIsBookmarked(items.contains(this.item)),
                        error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error));
            }

            final Date publishDate = this.item.getPublishDate();
            this.getView().setPublishDate(publishDate == null ? 0 : publishDate.getTime());

            if (this.subscriptions == null) this.subscriptions = new CompositeSubscription();

            if (this.showFullDescription && !this.item.isFullDescription()) this.subscriptions.add(ClientFactory.getInstance(this.getView().getContext()).getClient(this.item.getSource()).updateItem(this.item.clone())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                    updatedItem -> {
                        if (updatedItem != null) {
                            this.item.setDescription(updatedItem.getDescription());
                            this.item.setIsFullDescription(updatedItem.isFullDescription());
                            this.item.getImages().clear();
                            this.item.getImages().addAll(updatedItem.getImages());

                            this.getView().setDescription(this.item.getDescription());

                            if (!this.item.getImages().isEmpty()) {
                                this.getView().setThumbnail(this.item.getImages().first().getUrl(), this.type);
                                this.getView().setThumbnails(this.item.getImages());
                            }

                            this.update();

                            this.bus().send(new ImagesUpdatedEvent());
                        }
                    },
                    error -> this.log().w(this.getClass().getSimpleName(), error.getMessage(), error)));
        }
    }

    //region Lifecycle

    @Override
    public final void onViewAttached(@NonNull final BaseItemPresenter.View view) {
        super.onViewAttached(view);

        if (this.item != null) this.bind(this.parentKey, this.item, this.type, this.showFullDescription);

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

    @NonNull
    /* protected final */ ItemManager getItemManager() {
        return new ItemManager(this.realm);
    }

    /* protected final */ void updateItem(@NonNull final Item item, final boolean bookmark) {
        this.realm.beginTransaction();

        item.setBookmarked(bookmark);

        this.realm.copyToRealmOrUpdate(item);
        this.realm.commitTransaction();
    }

    /* private */ void update() {
        final Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(this.item);
        realm.commitTransaction();
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
        if (this.getView() == null) return;

        if (this.getView().zooms() != null) this.subscriptions.add(this.getView().zooms().subscribe(index -> {
            if (this.item != null && !this.item.getImages().isEmpty() && this.item.getImages().size() > index) this.getView().showMedia(this.item.getImages().get(index).getUrl());
        }, error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)));
    }

    protected abstract void attachBookmarks();

    protected abstract void attachShares();

    //endregion
}
