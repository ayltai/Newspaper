package com.github.ayltai.newspaper.app.view;

import android.app.Activity;
import android.graphics.Point;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import com.github.ayltai.newspaper.analytics.Attribute;
import com.github.ayltai.newspaper.analytics.ClickEvent;
import com.github.ayltai.newspaper.app.ComponentFactory;
import com.github.ayltai.newspaper.app.config.AppConfig;
import com.github.ayltai.newspaper.app.data.model.FeaturedItem;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.Source;
import com.github.ayltai.newspaper.app.data.model.SourceFactory;
import com.github.ayltai.newspaper.app.data.model.Video;
import com.github.ayltai.newspaper.app.widget.DetailsView;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.Optional;
import com.github.ayltai.newspaper.view.Presenter;
import com.github.ayltai.newspaper.view.binding.Binder;
import com.github.ayltai.newspaper.view.binding.BindingPresenter;

import java.util.Date;
import java.util.List;

import flow.Flow;
import io.reactivex.Flowable;

public class ItemPresenter<V extends ItemPresenter.View> extends BindingPresenter<Item, V> implements Binder<V> {
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

        @UiThread
        void addEntity(@NonNull String name, @NonNull String wikiLink);

        @UiThread
        void setIsRead(boolean isRead);

        @Nullable
        Flowable<Optional<Point>> clicks();

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
        Flowable<Irrelevant> videoClicks();

        @Nullable
        Flowable<String> entityClicks();
    }

    private AppConfig appConfig;

    @UiThread
    @Override
    public void bindModel() {
        if (this.getView() != null && this.getModel() != null) {
            this.getView().setIsRead(this.getModel().isFullDescription());
            this.getView().setAvatar(SourceFactory.getInstance(this.getView().getContext()).getSource(this.getModel().getSource()).getAvatar());
            this.getView().setSource(Source.toDisplayName(this.getModel().getSource()));
            this.getView().setPublishDate(this.getModel().getPublishDate());
            this.getView().setTitle(this.getModel().getTitle());
            this.getView().setDescription(this.getModel().getDescription());
            this.getView().setLink(this.getModel().getLink());
            this.getView().setIsBookmarked(this.getModel().isBookmarked());
            this.getView().setImages(this.getModel().getImages());
            this.getView().setVideo(this.getModel().getVideo());
        }
    }

    private void onClick(@NonNull final Optional<Point> location) {
        this.initAppConfig();

        if (this.getView() != null) {
            this.getView().setIsRead(true);

            final Item item = this.getModel();

            this.appConfig.setVideoPlaying(false);
            this.appConfig.setVideoSeekPosition(0);

            ComponentFactory.getInstance()
                .getAnalyticsComponent(this.getView().getContext())
                .eventLogger()
                .logEvent(new ClickEvent()
                    .setElementName(item instanceof FeaturedItem ? "Featured" : "Non-featured"));

            if (!DevUtils.isRunningUnitTest()) Flow.get(this.getView().getContext()).set(DetailsView.Key.create(item instanceof NewsItem ? (NewsItem)item : (NewsItem)((FeaturedItem)item).getItem(), location.isPresent() ? location.get() : null));
        }
    }

    @CallSuper
    private void onAvatarClick() {
        if (this.getView() != null) ComponentFactory.getInstance()
            .getAnalyticsComponent(this.getView().getContext())
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("Avatar"));
    }

    @CallSuper
    private void onSourceClick() {
        if (this.getView() != null) ComponentFactory.getInstance()
            .getAnalyticsComponent(this.getView().getContext())
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("Source"));
    }

    @CallSuper
    private void onPublishDateClick() {
        if (this.getView() != null) ComponentFactory.getInstance()
            .getAnalyticsComponent(this.getView().getContext())
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("Publish Date"));
    }

    @CallSuper
    private void onTitleClick() {
        if (this.getView() != null) ComponentFactory.getInstance()
            .getAnalyticsComponent(this.getView().getContext())
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("Title"));
    }

    @CallSuper
    private void onDescriptionClick() {
        if (this.getView() != null) ComponentFactory.getInstance()
            .getAnalyticsComponent(this.getView().getContext())
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("Description"));
    }

    private void onLinkClick() {
    }

    @CallSuper
    protected void onBookmarkClick() {
        if (this.getView() != null) ComponentFactory.getInstance()
            .getAnalyticsComponent(this.getView().getContext())
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("Bookmark"));
    }

    @CallSuper
    protected void onImageClick(@NonNull final Image image) {
        if (this.getView() != null) ComponentFactory.getInstance()
            .getAnalyticsComponent(this.getView().getContext())
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("Image"));
    }

    @CallSuper
    private void onVideoClick() {
        if (this.getView() != null) ComponentFactory.getInstance()
            .getAnalyticsComponent(this.getView().getContext())
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("Video"));
    }

    @CallSuper
    protected void onEntityClick(@NonNull final String wikiLink) {
        if (this.getView() != null) ComponentFactory.getInstance()
            .getAnalyticsComponent(this.getView().getContext())
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("Entity")
                .addAttribute(new Attribute("URL", wikiLink)));
    }

    @CallSuper
    @Override
    public void onViewAttached(@NonNull final V view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        final Flowable<Optional<Point>> clicks = view.clicks();
        if (clicks != null) this.manageDisposable(clicks.subscribe(this::onClick));

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

        final Flowable<Irrelevant> videoClicks = view.videoClicks();
        if (videoClicks != null) this.manageDisposable(videoClicks.subscribe(irrelevant -> this.onVideoClick()));

        final Flowable<String> entityClicks = view.entityClicks();
        if (entityClicks != null) this.manageDisposable(entityClicks.subscribe(this::onEntityClick));
    }

    private void initAppConfig() {
        if (this.appConfig == null) {
            final Activity activity = this.getView() == null ? null : this.getView().getActivity();
            if (activity != null) this.appConfig = ComponentFactory.getInstance()
                .getConfigComponent(activity)
                .appConfig();
        }
    }
}
