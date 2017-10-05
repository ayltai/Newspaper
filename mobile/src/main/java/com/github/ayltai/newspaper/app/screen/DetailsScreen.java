package com.github.ayltai.newspaper.app.screen;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.auto.value.AutoValue;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.facebook.drawee.view.SimpleDraweeView;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.config.ConfigModule;
import com.github.ayltai.newspaper.app.config.DaggerConfigComponent;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.Video;
import com.github.ayltai.newspaper.app.widget.ItemView;
import com.github.ayltai.newspaper.app.widget.VideoView;
import com.github.ayltai.newspaper.media.FrescoImageLoader;
import com.github.ayltai.newspaper.util.Animations;
import com.github.ayltai.newspaper.util.ContextUtils;
import com.github.ayltai.newspaper.util.DateUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.TestUtils;
import com.github.ayltai.newspaper.util.ViewUtils;
import com.github.ayltai.newspaper.view.ScreenPresenter;
import com.github.piasy.biv.view.BigImageView;
import com.gjiazhe.panoramaimageview.GyroscopeObserver;
import com.gjiazhe.panoramaimageview.PanoramaImageView;
import com.jakewharton.rxbinding2.view.RxView;
import com.stfalcon.frescoimageviewer.ImageViewer;

import flow.ClassKey;
import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import xyz.hanks.library.SmallBang;
import xyz.hanks.library.SmallBangListener;

public final class DetailsScreen extends ItemView implements DetailsPresenter.View, ScreenPresenter.View {
    @AutoValue
    public abstract static class Key extends ClassKey implements Parcelable {
        @NonNull
        public abstract NewsItem getItem();

        @NonNull
        public static DetailsScreen.Key create(@NonNull final NewsItem item) {
            return new AutoValue_DetailsScreen_Key(item);
        }
    }

    //region Subscriptions

    private final FlowableProcessor<Irrelevant> avatarClicks    = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> sourceClicks    = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> bookmarkClicks  = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> shareClicks     = PublishProcessor.create();
    private final FlowableProcessor<Image>      imageClicks     = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> backNavigations = PublishProcessor.create();

    //endregion

    //region Components

    private final AppBarLayout            appBarLayout;
    private final CollapsingToolbarLayout collapsingToolbarLayout;
    private final Toolbar                 toolbar;
    private final View                    toolbarView;
    private final BigImageView            toolbarImage;
    private final PanoramaImageView       panoramaImageView;
    private final TextView                toolbarTitle;
    private final View                    toolbarBackground;
    private final ViewGroup               imageContainer;
    private final ViewGroup               container;
    private final SimpleDraweeView        avatar;
    private final TextView                source;
    private final TextView                publishDate;
    private final TextView                title;
    private final TextView                description;
    private final ImageView               bookmarkAction;
    private final ImageView               shareAction;
    private final ViewGroup               imagesContainer;
    private final ViewGroup               videoContainer;

    private VideoView videoView;

    //endregion

    private GyroscopeObserver gyroscopeObserver;
    private SmallBang         smallBang;
    private boolean           isPanoramaEnabled;
    private boolean           hasAnimated;

    public DetailsScreen(@NonNull final Context context) {
        super(context);

        final View view = LayoutInflater.from(context).inflate(R.layout.screen_news_details, this, true);

        this.appBarLayout            = view.findViewById(R.id.appBarLayout);
        this.collapsingToolbarLayout = view.findViewById(R.id.collapsingToolbarLayout);
        this.toolbar                 = view.findViewById(R.id.toolbar);
        this.imageContainer          = view.findViewById(R.id.image_container);
        this.container               = view.findViewById(R.id.container);
        this.avatar                  = view.findViewById(R.id.avatar);
        this.source                  = view.findViewById(R.id.source);
        this.publishDate             = view.findViewById(R.id.publish_date);
        this.title                   = view.findViewById(R.id.title);
        this.description             = view.findViewById(R.id.description);
        this.bookmarkAction          = view.findViewById(R.id.action_bookmark);
        this.shareAction             = view.findViewById(R.id.action_share);
        this.imagesContainer         = view.findViewById(R.id.images_container);
        this.videoContainer          = view.findViewById(R.id.video_container);

        final Activity activity = this.getActivity();
        this.isPanoramaEnabled = activity == null
            ? Constants.PANORAMA_DEFAULT
            : DaggerConfigComponent.builder()
                .configModule(new ConfigModule(activity))
                .build()
                .userConfig()
                .isPanoramaEnabled();

        this.toolbarView       = LayoutInflater.from(this.getContext()).inflate(this.isPanoramaEnabled ? R.layout.widget_toolbar_panorama : R.layout.widget_toolbar, this.imageContainer, false);
        this.toolbarImage      = this.isPanoramaEnabled ? null : this.toolbarView.findViewById(R.id.image);
        this.panoramaImageView = this.isPanoramaEnabled ? this.toolbarView.findViewById(R.id.image) : null;
        this.toolbarTitle      = this.toolbarView.findViewById(R.id.title);
        this.toolbarBackground = this.toolbarView.findViewById(R.id.title_background);

        if (this.isPanoramaEnabled) {
            this.gyroscopeObserver = new GyroscopeObserver();
            this.panoramaImageView.setGyroscopeObserver(this.gyroscopeObserver);

            if (TestUtils.isRunningInstrumentedTest()) this.panoramaImageView.setEnablePanoramaMode(false);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) ((CollapsingToolbarLayout)view.findViewById(R.id.collapsingToolbarLayout)).setExpandedTitleTextAppearance(R.style.TransparentText);

        this.setLayoutParams(ViewUtils.createMatchParentLayoutParams());
    }

    //region Properties

    @Override
    public void setAvatar(@DrawableRes final int avatar) {
        this.avatar.setImageResource(avatar);
    }

    @Override
    public void setSource(@Nullable final CharSequence source) {
        if (TextUtils.isEmpty(source)) {
            this.source.setVisibility(View.GONE);
        } else {
            this.source.setVisibility(View.VISIBLE);
            this.source.setText(source);
        }
    }

    @Override
    public void setPublishDate(@Nullable final Date date) {
        if (date == null) {
            this.publishDate.setVisibility(View.GONE);
        } else {
            this.publishDate.setVisibility(View.VISIBLE);
            this.publishDate.setText(DateUtils.getTimeAgo(this.getContext(), date.getTime()));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setTitle(@Nullable final CharSequence title) {
        this.collapsingToolbarLayout.setTitle(title);

        if (TextUtils.isEmpty(title)) {
            this.title.setVisibility(View.GONE);
        } else {
            this.title.setVisibility(View.VISIBLE);
            this.title.setText(Html.fromHtml(title.toString()));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setDescription(@Nullable final CharSequence description) {
        if (TextUtils.isEmpty(description)) {
            this.description.setVisibility(View.GONE);
        } else {
            this.description.setVisibility(View.VISIBLE);
            this.description.setText(Html.fromHtml(description.toString()));
        }
    }

    @Override
    public void setLink(@Nullable final CharSequence link) {
        // TODO
    }

    @Override
    public void setIsBookmarked(final boolean isBookmarked) {
        if (isBookmarked) {
            final Drawable drawable = ContextCompat.getDrawable(this.getContext(), R.drawable.ic_bookmark_black_24dp);
            DrawableCompat.setTint(drawable, ContextUtils.getColor(this.getContext(), R.attr.primaryColor));
            this.bookmarkAction.setImageDrawable(drawable);
            this.bookmarkAction.setClickable(false);

            this.smallBang.bang(this.bookmarkAction, new SmallBangListener() {
                @Override
                public void onAnimationStart() {
                }

                @Override
                public void onAnimationEnd() {
                    DetailsScreen.this.bookmarkAction.setClickable(true);
                }
            });
        } else {
            final Drawable drawable = ContextCompat.getDrawable(this.getContext(), R.drawable.ic_bookmark_border_black_24dp);
            DrawableCompat.setTint(drawable, ContextUtils.getColor(this.getContext(), R.attr.textColorHint));
            this.bookmarkAction.setImageDrawable(drawable);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setImages(@NonNull final List<Image> images) {
        this.imageContainer.removeAllViews();
        this.imagesContainer.removeAllViews();

        if (!images.isEmpty()) {
            if (this.isPanoramaEnabled) {
                this.subscribeImage(this.panoramaImageView, images.get(0));
            } else {
                this.subscribeImage(this.toolbarImage, images.get(0));
            }

            this.appBarLayout.setExpanded(true, true);

            if (TextUtils.isEmpty(images.get(0).getDescription())) {
                this.toolbarBackground.setVisibility(View.GONE);
            } else {
                this.toolbarTitle.setText(Html.fromHtml(images.get(0).getDescription()));
                this.toolbarBackground.setVisibility(View.VISIBLE);
            }

            this.imageContainer.addView(this.toolbarView);

            if (images.size() > 1) {
                for (final Image image : images.subList(1, images.size() - 1)) {
                    final View         view        = LayoutInflater.from(this.getContext()).inflate(R.layout.widget_image, this.imagesContainer, false);
                    final BigImageView imageView   = view.findViewById(R.id.image);
                    final TextView     description = view.findViewById(R.id.description);

                    this.subscribeImage(imageView, image);

                    if (!TextUtils.isEmpty(image.getDescription())) description.setText(Html.fromHtml(image.getDescription()));

                    this.imagesContainer.addView(view);
                }
            }
        }
    }

    @Override
    public void setVideo(@Nullable final Video video) {
        this.videoContainer.removeAllViews();

        if (video != null) {
            this.videoView = new VideoView(this.getContext());
            this.videoView.setVideo(video);

            this.videoContainer.addView(this.videoView);
        }

        if (!TestUtils.isRunningInstrumentedTest() && !this.hasAnimated) {
            this.hasAnimated = true;

            Animations.animateViewGroup(this.container);
        }
    }

    //endregion

    //region Methods

    @Override
    public void share(@NonNull final String url) {
        this.getContext().startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, url).setType("text/plain"), this.getContext().getText(R.string.share_to)));
    }

    @Override
    public void showImage(@NonNull final String url) {
        new ImageViewer.Builder<>(this.getContext(), new String[] { url })
            .allowSwipeToDismiss(false)
            .show();
    }

    @Override
    public boolean goBack() {
        this.backNavigations.onNext(Irrelevant.INSTANCE);

        return false;
    }

    //endregion

    //region Events

    @NonNull
    @Override
    public Flowable<Irrelevant> avatarClicks() {
        return this.avatarClicks;
    }

    @NonNull
    @Override
    public Flowable<Irrelevant> sourceClicks() {
        return this.sourceClicks;
    }

    @NonNull
    @Override
    public Flowable<Irrelevant> bookmarkClicks() {
        return this.bookmarkClicks;
    }

    @NonNull
    @Override
    public Flowable<Irrelevant> shareClicks() {
        return this.shareClicks;
    }

    @NonNull
    @Override
    public Flowable<Image> imageClicks() {
        return this.imageClicks;
    }

    @Nullable
    @Override
    public Flowable<Irrelevant> videoClick() {
        return this.videoView == null ? null : this.videoView.videoClick();
    }

    //endregion

    //region Lifecycle

    @CallSuper
    @Override
    protected void onAttachedToWindow() {
        final Activity activity = this.getActivity();
        this.smallBang = activity == null ? null : SmallBang.attach2Window(activity);

        this.hasAnimated = false;

        if (this.isPanoramaEnabled) {
            this.gyroscopeObserver.register(this.getContext());
            this.panoramaImageView.setImageDrawable(null);
        } else {
            this.toolbarImage.getSSIV().setImage(ImageSource.resource(R.drawable.thumbnail_placeholder));
        }

        this.manageDisposable(RxView.clicks(this.avatar).subscribe(irrelevant -> this.avatarClicks.onNext(Irrelevant.INSTANCE)));
        this.manageDisposable(RxView.clicks(this.source).subscribe(irrelevant -> this.sourceClicks.onNext(Irrelevant.INSTANCE)));
        this.manageDisposable(RxView.clicks(this.bookmarkAction).subscribe(irrelevant -> this.bookmarkClicks.onNext(Irrelevant.INSTANCE)));
        this.manageDisposable(RxView.clicks(this.shareAction).subscribe(irrelevant -> this.shareClicks.onNext(Irrelevant.INSTANCE)));

        super.onAttachedToWindow();

        if (activity instanceof AppCompatActivity) {
            final AppCompatActivity appCompatActivity = (AppCompatActivity)activity;

            appCompatActivity.setSupportActionBar(this.toolbar);
            if (appCompatActivity.getSupportActionBar() != null) appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @CallSuper
    @Override
    protected void onDetachedFromWindow() {
        if (this.videoView != null) this.removeView(this.videoView);

        if (this.isPanoramaEnabled) this.gyroscopeObserver.unregister();

        super.onDetachedFromWindow();

        this.smallBang = null;
    }

    //endregion

    private void subscribeImage(@NonNull final BigImageView imageView, @NonNull final Image image) {
        imageView.getSSIV().setMaxScale(Constants.IMAGE_ZOOM_MAX);
        imageView.getSSIV().setPanEnabled(false);
        imageView.getSSIV().setZoomEnabled(false);

        imageView.showImage(Uri.parse(image.getUrl()));

        imageView.setOnClickListener(view -> this.imageClicks.onNext(image));
        this.manageDisposable(RxView.clicks((View)imageView.getParent()).subscribe(irrelevant -> this.imageClicks.onNext(image)));
    }

    private void subscribeImage(@NonNull final PanoramaImageView imageView, @NonNull final Image image) {
        FrescoImageLoader.loadImage(image.getUrl())
            .compose(RxUtils.applyMaybeBackgroundToMainSchedulers())
            .subscribe(
                imageView::setImageBitmap,
                error -> {
                    if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
                }
            );
        imageView.setOnClickListener(view -> this.imageClicks.onNext(image));
        this.manageDisposable(RxView.clicks((View)imageView.getParent()).subscribe(irrelevant -> this.imageClicks.onNext(image)));
    }
}
