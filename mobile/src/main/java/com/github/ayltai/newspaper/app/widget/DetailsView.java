package com.github.ayltai.newspaper.app.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
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
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.facebook.drawee.view.SimpleDraweeView;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.Video;
import com.github.ayltai.newspaper.app.view.DetailsPresenter;
import com.github.ayltai.newspaper.util.Animations;
import com.github.ayltai.newspaper.util.ContextUtils;
import com.github.ayltai.newspaper.util.DateUtils;
import com.github.ayltai.newspaper.util.ImageUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.Locatable;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.SimpleTextToSpeech;
import com.github.ayltai.newspaper.util.SnackbarUtils;
import com.github.ayltai.newspaper.util.Views;
import com.github.piasy.biv.view.BigImageView;
import com.google.auto.value.AutoValue;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import flow.ClassKey;
import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import xyz.hanks.library.bang.SmallBangView;

@SuppressWarnings("checkstyle:MethodCount")
public final class DetailsView extends ItemView implements DetailsPresenter.View {
    @AutoValue
    public abstract static class Key extends ClassKey implements Locatable, Parcelable {
        @NonNull
        public abstract NewsItem getItem();

        @Nullable
        @Override
        public abstract Point getLocation();

        @NonNull
        public static DetailsView.Key create(@NonNull final NewsItem item, @Nullable final Point location) {
            return new AutoValue_DetailsView_Key(item, location);
        }
    }

    //region Subscriptions

    private final FlowableProcessor<Irrelevant> avatarClicks       = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> sourceClicks       = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> textToSpeechClicks = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> bookmarkClicks     = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> viewOnWebClicks    = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> shareClicks        = PublishProcessor.create();
    private final FlowableProcessor<Image>      imageClicks        = PublishProcessor.create();
    private final FlowableProcessor<String>     entityClicks       = PublishProcessor.create();

    //endregion

    //region Components

    private final AppBarLayout            appBarLayout;
    private final CollapsingToolbarLayout collapsingToolbarLayout;
    private final Toolbar                 toolbar;
    private final View                    toolbarView;
    private final BigImageView            toolbarImage;
    private final TextView                toolbarTitle;
    private final View                    toolbarBackground;
    private final ViewGroup               imageContainer;
    private final NestedScrollView        scrollView;
    private final View                    progress;
    private final SimpleDraweeView        avatar;
    private final TextView                source;
    private final TextView                publishDate;
    private final TextView                title;
    private final TextView                description;
    private final ImageView               textToSpeechAction;
    private final ImageView               bookmarkAction;
    private final ImageView               viewOnWebAction;
    private final ImageView               shareAction;
    private final ViewGroup               imagesContainer;
    private final ViewGroup               videoContainer;
    private final View                    entitiesContainer;
    private final ViewGroup               entities;

    private VideoView videoView;

    //endregion

    private SmallBangView      smallBang;
    private boolean            isTtsActive;
    private SimpleTextToSpeech tts;

    public DetailsView(@NonNull final Context context) {
        super(context);

        final View view = LayoutInflater.from(context).inflate(R.layout.view_details, this, true);

        this.appBarLayout            = view.findViewById(R.id.appBarLayout);
        this.collapsingToolbarLayout = view.findViewById(R.id.collapsingToolbarLayout);
        this.toolbar                 = view.findViewById(R.id.toolbar);
        this.imageContainer          = view.findViewById(R.id.image_container);
        this.scrollView              = view.findViewById(R.id.scrollView);
        this.progress                = view.findViewById(R.id.progress);
        this.avatar                  = view.findViewById(R.id.avatar);
        this.source                  = view.findViewById(R.id.source);
        this.publishDate             = view.findViewById(R.id.publish_date);
        this.title                   = view.findViewById(R.id.title);
        this.description             = view.findViewById(R.id.description);
        this.textToSpeechAction      = view.findViewById(R.id.action_text_to_speech);
        this.smallBang               = view.findViewById(R.id.smallBang);
        this.bookmarkAction          = view.findViewById(R.id.action_bookmark);
        this.viewOnWebAction         = view.findViewById(R.id.action_view_on_web);
        this.shareAction             = view.findViewById(R.id.action_share);
        this.imagesContainer         = view.findViewById(R.id.images_container);
        this.videoContainer          = view.findViewById(R.id.video_container);
        this.entitiesContainer       = view.findViewById(R.id.entities_container);
        this.entities                = view.findViewById(R.id.entities);

        this.toolbarView       = LayoutInflater.from(this.getContext()).inflate(R.layout.widget_toolbar, this.imageContainer, false);
        this.toolbarImage      = this.toolbarView.findViewById(R.id.image);
        this.toolbarTitle      = this.toolbarView.findViewById(R.id.title);
        this.toolbarBackground = this.toolbarView.findViewById(R.id.title_background);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) ((CollapsingToolbarLayout)view.findViewById(R.id.collapsingToolbarLayout)).setExpandedTitleTextAppearance(R.style.TransparentText);

        this.setLayoutParams(Views.createMatchParentLayoutParams());
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
            this.publishDate.setText(DateUtils.toApproximateTime(this.getContext(), date.getTime()));
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
    public void setIsBookmarked(final boolean isBookmarked) {
        if (isBookmarked) {
            final Drawable drawable = ContextCompat.getDrawable(this.getContext(), R.drawable.ic_bookmark_black_24dp);
            DrawableCompat.setTint(drawable, ContextUtils.getColor(this.getContext(), R.attr.primaryColor));
            this.bookmarkAction.setImageDrawable(drawable);
            this.bookmarkAction.setClickable(false);

            if (Animations.isEnabled()) this.smallBang.likeAnimation(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(final Animator animation) {
                    DetailsView.this.bookmarkAction.setClickable(true);
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
            this.subscribeImage(this.toolbarImage, images.get(0));
            ImageUtils.translateToFacesCenter(this.toolbarImage);

            if (TextUtils.isEmpty(images.get(0).getDescription())) {
                this.toolbarBackground.setVisibility(View.GONE);
            } else {
                this.toolbarTitle.setText(Html.fromHtml(images.get(0).getDescription()));
                this.toolbarBackground.setVisibility(View.VISIBLE);
            }

            this.imageContainer.addView(this.toolbarView);

            this.appBarLayout.setExpanded(true, false);

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
    }

    @Override
    public void addEntity(@NonNull final String name, @NonNull final String wikiLink) {
        this.entitiesContainer.setVisibility(View.VISIBLE);

        final TextView entity = (TextView)LayoutInflater.from(this.getContext()).inflate(R.layout.view_text_option, this.entities, false);
        entity.setText(name);
        entity.setOnClickListener(view -> this.entityClicks.onNext(wikiLink));

        this.entities.addView(entity);
    }

    //endregion

    //region Methods

    @Override
    public void showProgress(final boolean show) {
        this.progress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void textToSpeech() {
        this.updateTextToSpeechUi(!this.isTtsActive);

        if (this.isTtsActive) {
            final Activity activity = this.getActivity();

            if (activity == null) {
                this.updateTextToSpeechUi(false);
            } else {
                this.manageDisposable(SimpleTextToSpeech.builder()
                    .addLocale(new Locale("yue", "HK"))
                    .addLocale(new Locale("zh", "HK"))
                    .setOnNotSupported(() -> {
                        this.updateTextToSpeechUi(false);

                        SnackbarUtils.show(this, R.string.error_tts_not_supported, Snackbar.LENGTH_LONG);
                    })
                    .setOnInitError(error -> {
                        this.updateTextToSpeechUi(false);

                        SnackbarUtils.show(this, R.string.error_tts_not_initialized, Snackbar.LENGTH_LONG);

                        return Irrelevant.INSTANCE;
                    })
                    .setOnUtteranceCompleted(() -> {
                        if (this.isTtsActive) this.textToSpeech();
                    })
                    .build(activity)
                    .compose(RxUtils.applySingleBackgroundToMainSchedulers())
                    .subscribe(tts -> {
                        if (this.isTtsActive) {
                            this.tts = tts;

                            this.tts.setText(this.title.getText());
                            this.tts.addText(this.description.getText());
                        } else {
                            this.updateTextToSpeechUi(false);
                            if (this.tts != null) this.tts.shutdown();
                        }
                    }));
            }
        } else {
            if (this.tts != null) this.tts.shutdown();
        }
    }

    private void updateTextToSpeechUi(final boolean active) {
        this.isTtsActive = active;
        this.textToSpeechAction.setImageResource(this.isTtsActive ? R.drawable.ic_volume_up_black_24dp : R.drawable.ic_volume_off_black_24dp);
    }

    @Override
    public void viewOnWeb(@NonNull final String url) {
        this.getContext().startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW, Uri.parse(url)), this.getContext().getText(R.string.view_via)));
    }

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

    @Nullable
    @Override
    public Flowable<Irrelevant> textToSpeechClicks() {
        return this.textToSpeechClicks;
    }

    @NonNull
    @Override
    public Flowable<Irrelevant> bookmarkClicks() {
        return this.bookmarkClicks;
    }

    @Nullable
    @Override
    public Flowable<Irrelevant> viewOnWebClicks() {
        return this.viewOnWebClicks;
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
    public Flowable<Irrelevant> videoClicks() {
        return this.videoView == null ? null : this.videoView.videoClicks();
    }

    @Nullable
    @Override
    public Flowable<String> entityClicks() {
        return this.entityClicks;
    }

    //endregion

    //region Lifecycle

    @CallSuper
    @Override
    public void onAttachedToWindow() {
        this.toolbarImage.getSSIV().setImage(ImageSource.resource(R.drawable.thumbnail_placeholder));

        this.avatar.setOnClickListener(view -> this.avatarClicks.onNext(Irrelevant.INSTANCE));
        this.source.setOnClickListener(view -> this.sourceClicks.onNext(Irrelevant.INSTANCE));
        this.textToSpeechAction.setOnClickListener(view -> this.textToSpeechClicks.onNext(Irrelevant.INSTANCE));
        this.bookmarkAction.setOnClickListener(view -> this.bookmarkClicks.onNext(Irrelevant.INSTANCE));
        this.viewOnWebAction.setOnClickListener(view -> this.viewOnWebClicks.onNext(Irrelevant.INSTANCE));
        this.shareAction.setOnClickListener(view -> this.shareClicks.onNext(Irrelevant.INSTANCE));

        this.entitiesContainer.setVisibility(View.GONE);
        this.entities.removeAllViews();

        super.onAttachedToWindow();

        this.scrollView.scrollTo(0, 0);

        final Activity activity = this.getActivity();

        if (activity instanceof AppCompatActivity) {
            final AppCompatActivity appCompatActivity = (AppCompatActivity)activity;

            appCompatActivity.setSupportActionBar(this.toolbar);
            if (appCompatActivity.getSupportActionBar() != null) appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @CallSuper
    @Override
    public void onDetachedFromWindow() {
        if (this.videoView != null) this.removeView(this.videoView);

        if (this.isTtsActive) this.textToSpeech();

        super.onDetachedFromWindow();
    }

    //endregion

    private void subscribeImage(@NonNull final BigImageView imageView, @NonNull final Image image) {
        imageView.getSSIV().setMaxScale(Constants.IMAGE_ZOOM_MAX);
        imageView.getSSIV().setPanEnabled(false);
        imageView.getSSIV().setZoomEnabled(false);

        imageView.showImage(Uri.parse(image.getUrl()));

        imageView.setOnClickListener(view -> this.imageClicks.onNext(image));
        ((View)imageView.getParent()).setOnClickListener(view -> this.imageClicks.onNext(image));
    }
}
