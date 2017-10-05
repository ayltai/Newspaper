package com.github.ayltai.newspaper.app.view;

import java.util.Date;
import java.util.List;

import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import com.github.ayltai.newspaper.analytics.AnalyticsModule;
import com.github.ayltai.newspaper.analytics.ClickEvent;
import com.github.ayltai.newspaper.analytics.DaggerAnalyticsComponent;
import com.github.ayltai.newspaper.app.data.model.FeaturedItem;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.Source;
import com.github.ayltai.newspaper.app.data.model.SourceFactory;
import com.github.ayltai.newspaper.app.data.model.Video;
import com.github.ayltai.newspaper.app.screen.DetailsScreen;
import com.github.ayltai.newspaper.app.config.AppConfig;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.view.Presenter;
import com.github.ayltai.newspaper.view.binding.Binder;
import com.github.ayltai.newspaper.view.binding.PresentationBinder;

import flow.Flow;
import io.reactivex.Flowable;

public class ItemPresenter<V extends ItemPresenter.View> extends PresentationBinder<Item, V> implements Binder<V> {
    public interface View extends Presenter.View {
        @UiThread
        void setAvatar(@DrawableRes int avatar);

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
        void setImages(@NonNull List<Image> images);

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
        Flowable<Irrelevant> bookmarkClicks();

        @Nullable
        Flowable<Image> imageClicks();

        @Nullable
        Flowable<Irrelevant> videoClick();
    }

    @UiThread
    @Override
    public void bindModel(final Item model) {
        super.bindModel(model);

        if (this.getView() != null && model != null) {
            this.getView().setAvatar(SourceFactory.getInstance(this.getView().getContext()).getSource(model.getSource()).getAvatar());
            this.getView().setSource(Source.toDisplayName(model.getSource()));
            this.getView().setPublishDate(model.getPublishDate());
            this.getView().setTitle(model.getTitle());
            this.getView().setDescription(model.getDescription());
            this.getView().setLink(model.getLink());
            this.getView().setIsBookmarked(model.isBookmarked());
            this.getView().setImages(model.getImages());
            this.getView().setVideo(model.getVideo());
        }
    }

    protected void onClick() {
        if (this.getView() != null) {
            final Item item = this.getModel();

            AppConfig.setVideoPlaying(false);
            AppConfig.setVideoSeekPosition(0);

            if (item instanceof FeaturedItem) DaggerAnalyticsComponent.builder()
                .analyticsModule(new AnalyticsModule(this.getView().getContext()))
                .build()
                .eventLogger()
                .logEvent(new ClickEvent()
                    .setElementName("Featured"));

            Flow.get(this.getView().getContext()).set(DetailsScreen.Key.create(item instanceof NewsItem ? (NewsItem)item : (NewsItem)((FeaturedItem)item).getItem()));
        }
    }

    @CallSuper
    protected void onAvatarClick() {
        if (this.getView() != null) DaggerAnalyticsComponent.builder()
            .analyticsModule(new AnalyticsModule(this.getView().getContext()))
            .build()
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("Avatar"));
    }

    @CallSuper
    protected void onSourceClick() {
        if (this.getView() != null) DaggerAnalyticsComponent.builder()
            .analyticsModule(new AnalyticsModule(this.getView().getContext()))
            .build()
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("Source"));
    }

    @CallSuper
    protected void onPublishDateClick() {
        if (this.getView() != null) DaggerAnalyticsComponent.builder()
            .analyticsModule(new AnalyticsModule(this.getView().getContext()))
            .build()
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("Publish Date"));
    }

    @CallSuper
    protected void onTitleClick() {
        if (this.getView() != null) DaggerAnalyticsComponent.builder()
            .analyticsModule(new AnalyticsModule(this.getView().getContext()))
            .build()
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("Title"));
    }

    @CallSuper
    protected void onDescriptionClick() {
        if (this.getView() != null) DaggerAnalyticsComponent.builder()
            .analyticsModule(new AnalyticsModule(this.getView().getContext()))
            .build()
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("Description"));
    }

    protected void onLinkClick() {
    }

    @CallSuper
    protected void onBookmarkClick() {
        if (this.getView() != null) DaggerAnalyticsComponent.builder()
            .analyticsModule(new AnalyticsModule(this.getView().getContext()))
            .build()
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("Bookmark"));
    }

    @CallSuper
    protected void onImageClick(@NonNull final Image image) {
        if (this.getView() != null) DaggerAnalyticsComponent.builder()
            .analyticsModule(new AnalyticsModule(this.getView().getContext()))
            .build()
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("Image"));
    }

    @CallSuper
    protected void onVideoClick() {
        if (this.getView() != null) DaggerAnalyticsComponent.builder()
            .analyticsModule(new AnalyticsModule(this.getView().getContext()))
            .build()
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("Video"));
    }

    @SuppressWarnings("CyclomaticComplexity")
    @CallSuper
    @Override
    public void onViewAttached(@NonNull final V view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        final Flowable<Irrelevant> clicks = view.clicks();
        if (clicks != null) this.manageDisposable(clicks.subscribe(irrelevant -> this.onClick()));

        final Flowable<Irrelevant> avatarClicks = view.avatarClicks();
        if (avatarClicks != null) this.manageDisposable(avatarClicks.subscribe(irrelevant -> this.onAvatarClick()));

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

        final Flowable<Image> imageClicks = view.imageClicks();
        if (imageClicks != null) this.manageDisposable(imageClicks.subscribe(this::onImageClick));

        final Flowable<Irrelevant> videoClick = view.videoClick();
        if (videoClick != null) this.manageDisposable(videoClick.subscribe(irrelevant -> this.onVideoClick()));

        this.bindModel(this.getModel());
    }
}
