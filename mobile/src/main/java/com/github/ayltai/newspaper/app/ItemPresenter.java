package com.github.ayltai.newspaper.app;

import java.util.Collection;
import java.util.Date;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import com.github.ayltai.newspaper.data.model.Image;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.data.model.Video;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.view.Presenter;
import com.github.ayltai.newspaper.view.binding.Binder;
import com.github.ayltai.newspaper.view.binding.PresentationBinder;

import io.reactivex.Flowable;

public class ItemPresenter extends PresentationBinder<Item, ItemPresenter.View> implements Binder<ItemPresenter.View> {
    public interface View extends Presenter.View {
        @UiThread
        void setAvatar(@Nullable String avatarUri);

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
        void setIsBookmarked(boolean isBookmarked);

        @UiThread
        void setImages(@NonNull Collection<Image> images);

        @UiThread
        void setVideo(@Nullable Video video);

        @Nullable
        Flowable<Irrelevant> clicks();

        @Nullable
        Flowable<Irrelevant> avatarClicks();

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
        Flowable<Boolean> bookmarkClicks();

        @Nullable
        Flowable<Image> imageClicks();

        @Nullable
        Flowable<Irrelevant> videoClick();
    }

    @UiThread
    @Override
    public void bindModel(final Item model) {
        super.bindModel(model);

        if (this.getView() != null) {
            this.getView().setAvatar(null); // TODO
            this.getView().setSource(model.getSource());
            this.getView().setPublishDate(model.getPublishDate());
            this.getView().setTitle(model.getTitle());
            this.getView().setDescription(model.getDescription());
            this.getView().setLink(model.getLink());
            this.getView().setIsBookmarked(model.isBookmarked());
            this.getView().setImages(model.getImages());
            this.getView().setVideo(model.getVideo());

            final Flowable<Irrelevant> clicks = this.getView().clicks();
            if (clicks != null) this.manageDisposable(clicks.subscribe(irrelevant -> this.onClick()));

            final Flowable<Irrelevant> avatarClicks = this.getView().avatarClicks();
            if (avatarClicks != null) this.manageDisposable(avatarClicks.subscribe(irrelevant -> this.onAvatarClick()));

            final Flowable<Irrelevant> sourceClicks = this.getView().sourceClicks();
            if (sourceClicks != null) this.manageDisposable(sourceClicks.subscribe(irrelevant -> this.onSourceClick()));

            final Flowable<Irrelevant> publishDateClicks = this.getView().publishDateClicks();
            if (publishDateClicks != null) this.manageDisposable(publishDateClicks.subscribe(irrelevant -> this.onPublishDateClick()));

            final Flowable<Irrelevant> titleClicks = this.getView().titleClicks();
            if (titleClicks != null) this.manageDisposable(titleClicks.subscribe(irrelevant -> this.onTitleClick()));

            final Flowable<Irrelevant> descriptionClicks = this.getView().descriptionClicks();
            if (descriptionClicks != null) this.manageDisposable(descriptionClicks.subscribe(irrelevant -> this.onDescriptionClick()));

            final Flowable<Irrelevant> linkClicks = this.getView().linkClicks();
            if (linkClicks != null) this.manageDisposable(linkClicks.subscribe(irrelevant -> this.onLinkClick()));

            final Flowable<Boolean> bookmarkClicks = this.getView().bookmarkClicks();
            if (bookmarkClicks != null) this.manageDisposable(bookmarkClicks.subscribe(this::onBookmarkClick));

            final Flowable<Image> imageClicks = this.getView().imageClicks();
            if (imageClicks != null) this.manageDisposable(imageClicks.subscribe(this::onImageClick));

            final Flowable<Irrelevant> videoClick = this.getView().videoClick();
            if (videoClick != null) this.manageDisposable(videoClick.subscribe(irrelevant -> this.onVideoClick()));
        }
    }

    protected void onClick() {
    }

    protected void onAvatarClick() {
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

    protected void onBookmarkClick(final boolean isBookmarked) {
    }

    protected void onImageClick(@NonNull final Image image) {
    }

    protected void onVideoClick() {
    }
}
