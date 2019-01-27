package com.github.ayltai.newspaper.view;

import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import android.graphics.Point;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.github.ayltai.newspaper.data.model.Image;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.data.model.Video;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.Optional;

import io.reactivex.Flowable;

public class ItemPresenter<V extends ItemPresenter.View> extends ModelPresenter<Item, V> {
    public interface View extends Presenter.View {
        @UiThread
        void setIcon(@Nonnull @NonNull @lombok.NonNull String iconUrl);

        @UiThread
        void setSource(@Nullable CharSequence source);

        @UiThread
        void setPublishDate(@Nullable Date date);

        @UiThread
        void setTitle(@Nullable CharSequence title);

        @UiThread
        void setDescription(@Nullable CharSequence description);

        @UiThread
        void setLink(@Nullable CharSequence link);

        @UiThread
        void setImages(@Nonnull @NonNull @lombok.NonNull List<Image> images);

        @UiThread
        void setVideos(@Nonnull @NonNull @lombok.NonNull List<Video> videos);

        @UiThread
        void setIsRead(boolean isRead);

        @UiThread
        void setIsBookmarked(boolean isBookmarked);

        @Nullable
        Flowable<Optional<Point>> clicks();

        @Nullable
        Flowable<Irrelevant> iconClicks();

        @Nullable
        Flowable<Irrelevant> sourceClicks();

        @Nullable
        Flowable<Irrelevant> publishDateClicks();

        @Nullable
        Flowable<Irrelevant> titleClicks();

        @Nullable
        Flowable<Irrelevant> descriptionClicks();

        @Nullable
        Flowable<Irrelevant> linkClicks();

        @Nullable
        Flowable<Integer> imageClicks();

        @Nullable
        Flowable<Integer> videoClicks();

        @Nullable
        Flowable<Irrelevant> bookmarkClicks();
    }

    @UiThread
    @Override
    public void bindModel() {
        if (this.getView() != null && this.getModel() != null) {
            this.getView().setIsRead(this.getModel().isRead());
            this.getView().setIcon(this.getModel().getSource().getImageUrl());
            this.getView().setSource(this.getModel().getSource().getDisplayName());
            this.getView().setPublishDate(this.getModel().getPublishDate());
            this.getView().setTitle(this.getModel().getTitle());
            this.getView().setDescription(this.getModel().getDescription());
            this.getView().setLink(this.getModel().getUrl());
            this.getView().setIsBookmarked(this.getModel().isBookmarked());
            this.getView().setImages(this.getModel().getImages());
            this.getView().setVideos(this.getModel().getVideos());
        }
    }

    @CallSuper
    @Override
    public void onViewAttached(@NonNull final V view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        final Flowable<Optional<Point>> clicks = view.clicks();
        if (clicks != null) this.manageDisposable(clicks.subscribe(this::onClick));

        final Flowable<Irrelevant> avatarClicks = view.iconClicks();
        if (avatarClicks != null) this.manageDisposable(avatarClicks.subscribe(irrelevant -> this.onIconClick()));

        final Flowable<Irrelevant> sourceClicks = view.sourceClicks();
        if (sourceClicks != null) this.manageDisposable(sourceClicks.subscribe(irrelevant -> this.onSourceClick()));

        final Flowable<Irrelevant> publishDateClicks = view.publishDateClicks();
        if (publishDateClicks != null) this.manageDisposable(publishDateClicks.subscribe(irrelevant -> this.onPublishDateClick()));

        final Flowable<Irrelevant> titleClicks = view.titleClicks();
        if (titleClicks != null) this.manageDisposable(titleClicks.subscribe(irrelevant -> this.onTitleClick()));

        final Flowable<Irrelevant> descriptionClicks = view.descriptionClicks();
        if (descriptionClicks != null) this.manageDisposable(descriptionClicks.subscribe(irrelevant -> this.onDescriptionClick()));

        final Flowable<Irrelevant> linkClicks = view.linkClicks();
        if (linkClicks != null) this.manageDisposable(linkClicks.subscribe(irrelevant -> this.onLinkClick()));

        final Flowable<Irrelevant> bookmarkClicks = view.bookmarkClicks();

        if (bookmarkClicks != null) this.manageDisposable(bookmarkClicks.subscribe(irrelevant -> this.onBookmarkClick()));

        final Flowable<Integer> imageClicks = view.imageClicks();
        if (imageClicks != null) this.manageDisposable(imageClicks.subscribe(index -> this.onImageClick(this.getModel().getImages().get(index))));
        final Flowable<Integer> videoClicks = view.videoClicks();
        if (videoClicks != null) this.manageDisposable(videoClicks.subscribe(index -> this.onVideoClick(this.getModel().getVideos().get(index))));
    }

    @CallSuper
    protected void onClick(@NonNull final Optional<Point> location) {
        if (this.getView() != null) {
            this.getView().setIsRead(true);

            final Item item = this.getModel();

            //if (!DevUtils.isRunningUnitTest()) Flow.get(this.getView().getContext()).set(DetailsView.Key.create(item, location.isPresent() ? location.get() : null));
        }
    }

    protected void onIconClick() {
    }

    protected void onSourceClick() {
    }

    protected void onPublishDateClick() {
    }

    protected void onTitleClick() {
    }

    protected void onDescriptionClick() {
    }

    protected void onLinkClick() {
    }

    protected void onBookmarkClick() {
    }

    protected void onImageClick(@Nonnull @NonNull @lombok.NonNull final Image image) {
    }

    protected void onVideoClick(@Nonnull @NonNull @lombok.NonNull final Video video) {
    }
}
