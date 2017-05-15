package com.github.ayltai.newspaper.item;

import java.util.Date;
import java.util.List;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
import com.github.ayltai.newspaper.model.Source;
import com.github.ayltai.newspaper.model.Video;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

public abstract class BaseItemPresenter extends Presenter<BaseItemPresenter.View> {
    public interface View extends Presenter.View {
        void setTitle(@Nullable String title);

        void setDescription(@Nullable String description);

        void setSource(@Nullable String source);

        void setLink(@Nullable String link);

        void setPublishDate(long publishDate);

        void setThumbnail(@Nullable String thumbnail, @Constants.ListViewType int type);

        void setThumbnails(@NonNull List<Image> images);

        void setVideo(@Nullable Video video);

        void setIsBookmarked(boolean isBookmarked);

        @Nullable Flowable<Object> clicks();

        @Nullable Flowable<Integer> zooms();

        @Nullable Flowable<Boolean> bookmarks();

        @Nullable Flowable<Object> shares();

        void showItem(@NonNull ListScreen.Key parentKey, @NonNull Item item);

        void showImage(@NonNull String url);

        void share(@NonNull String url);
    }

    //region Variables

    private final Realm realm;

    protected CompositeDisposable disposables;
    protected ListScreen.Key      parentKey;
    protected Item                item;

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
            this.bindView();

            if (this.disposables == null) this.disposables = new CompositeDisposable();

            if (this.showFullDescription && !this.item.isFullDescription()) this.disposables.add(ClientFactory.getInstance(this.getView().getContext()).getClient(this.item.getSource()).updateItem(this.item.clone())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .filter(updatedItem -> updatedItem != null)
                .subscribe(
                    updatedItem -> {
                        if (this.item.getDescription() == null || this.item.getDescription().length() == 0 || (updatedItem.getDescription() != null && updatedItem.getDescription().length() > 0)) {
                            this.update(updatedItem);

                            this.getView().setDescription(this.item.getDescription());

                            if (this.item.getVideo() != null) this.getView().setVideo(this.item.getVideo());

                            if (!this.item.getImages().isEmpty()) {
                                this.getView().setThumbnail(this.item.getImages().first().getUrl(), this.type);
                                this.getView().setThumbnails(this.item.getImages());
                            }

                            this.bus().send(new ImagesUpdatedEvent());
                        }
                    },
                    error -> this.log().w(this.getClass().getSimpleName(), error.getMessage(), error)));
        }
    }

    private void bindView() {
        if (this.getView() == null) throw new NullPointerException("View is null");

        this.getView().setTitle(this.item.getTitle());
        this.getView().setDescription(this.showFullDescription && !this.item.isFullDescription() ? this.getView().getContext().getString(R.string.loading_indicator) : this.item.getDescription());
        this.getView().setSource(Source.toDisplayName(this.item.getSource()));
        this.getView().setLink(this.item.getLink());
        this.getView().setThumbnail(this.item.getImages().isEmpty() ? null : this.item.getImages().first().getUrl(), this.type);
        this.getView().setThumbnails(this.item.getImages());
        this.getView().setVideo(this.item.getVideo());
        if (this.getView().bookmarks() != null) this.getView().setIsBookmarked(this.item.isBookmarked());

        final Date publishDate = this.item.getPublishDate();
        this.getView().setPublishDate(publishDate == null ? 0 : publishDate.getTime());
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

        if (this.disposables != null && !this.disposables.isDisposed() && this.disposables.size() > 0) {
            this.disposables.dispose();
            this.disposables = null;
        }
    }

    //endregion

    @NonNull
    /* protected final */ ItemManager getItemManager() {
        return new ItemManager(this.realm);
    }

    /* protected final */ void update(@NonNull final Item item) {
        final Realm realm = Realm.getDefaultInstance();

        try {
            realm.beginTransaction();

            this.item.setDescription(item.getDescription());
            this.item.setIsFullDescription(item.isFullDescription());
            this.item.getImages().clear();
            this.item.getImages().addAll(item.getImages());
            this.item.setVideo(item.getVideo());

            realm.insertOrUpdate(this.item);
            realm.commitTransaction();
        } finally {
            realm.close();
        }
    }

    /* protected final */ void update(final boolean isBookmarked) {
        final Realm realm = Realm.getDefaultInstance();

        try {
            realm.beginTransaction();

            this.item.setBookmarked(isBookmarked);

            realm.insertOrUpdate(this.item);
            realm.commitTransaction();
        } finally {
            realm.close();
        }
    }

    //region Event handlers

    private void attachEvents() {
        if (this.disposables == null) this.disposables = new CompositeDisposable();

        this.attachClicks();
        this.attachZooms();
        this.attachBookmarks();
        this.attachShares();
    }

    protected abstract void attachClicks();

    private void attachZooms() {
        if (this.getView() == null) return;

        if (this.getView().zooms() != null) this.disposables.add(this.getView().zooms().subscribe(index -> {
            if (this.item != null && !this.item.getImages().isEmpty() && this.item.getImages().size() > index) this.getView().showImage(this.item.getImages().get(index).getUrl());
        }, error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)));
    }

    protected abstract void attachBookmarks();

    protected abstract void attachShares();

    //endregion
}
