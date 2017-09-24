package com.github.ayltai.newspaper.app.screen;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.auto.value.AutoValue;

import com.facebook.drawee.view.SimpleDraweeView;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.widget.ItemView;
import com.github.ayltai.newspaper.data.model.Image;
import com.github.ayltai.newspaper.data.model.NewsItem;
import com.github.ayltai.newspaper.data.model.Video;
import com.github.ayltai.newspaper.util.ContextUtils;
import com.github.ayltai.newspaper.util.DateUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.ViewUtils;
import com.github.ayltai.newspaper.view.ScreenPresenter;
import com.github.piasy.biv.view.BigImageView;
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
    private final FlowableProcessor<Irrelevant> videoClicks     = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> backNavigations = PublishProcessor.create();

    //endregion

    //region Components

    private final CollapsingToolbarLayout collapsingToolbarLayout;
    private final Toolbar                 toolbar;
    private final View                    toolbarView;
    private final BigImageView            toolbarImage;
    private final TextView                toolbarTitle;
    private final View                    toolbarBackground;
    private final ViewGroup               imageContainer;
    private final SimpleDraweeView        avatar;
    private final TextView                source;
    private final TextView                publishDate;
    private final TextView                title;
    private final TextView                description;
    private final ImageView               bookmarkAction;
    private final ImageView               shareAction;
    private final ViewGroup               imagesContainer;
    private final ViewGroup               videoContainer;

    //endregion

    private SmallBang smallBang;

    public DetailsScreen(@NonNull final Context context) {
        super(context);

        final View view = LayoutInflater.from(context).inflate(R.layout.screen_news_details, this, true);

        this.collapsingToolbarLayout = view.findViewById(R.id.collapsingToolbarLayout);
        this.toolbar                 = view.findViewById(R.id.toolbar);
        this.imageContainer          = view.findViewById(R.id.image_container);
        this.avatar                  = view.findViewById(R.id.avatar);
        this.source                  = view.findViewById(R.id.source);
        this.publishDate             = view.findViewById(R.id.publish_date);
        this.title                   = view.findViewById(R.id.title);
        this.description             = view.findViewById(R.id.description);
        this.bookmarkAction          = view.findViewById(R.id.action_bookmark);
        this.shareAction             = view.findViewById(R.id.action_share);
        this.imagesContainer         = view.findViewById(R.id.images_container);
        this.videoContainer          = view.findViewById(R.id.video_container);

        this.toolbarView       = LayoutInflater.from(this.getContext()).inflate(R.layout.widget_toolbar, this.imageContainer, false);
        this.toolbarImage      = this.toolbarView.findViewById(R.id.image);
        this.toolbarTitle      = this.toolbarView.findViewById(R.id.title);
        this.toolbarBackground = this.toolbarView.findViewById(R.id.title_background);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) this.collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.TransparentText);

        this.setLayoutParams(ViewUtils.createMatchParentLayoutParams());
    }

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

    @Override
    public void setTitle(@Nullable final CharSequence title) {
        if (TextUtils.isEmpty(title)) {
            this.title.setVisibility(View.GONE);
            this.collapsingToolbarLayout.setTitle("");
        } else {
            this.title.setVisibility(View.VISIBLE);
            this.title.setText(title);
            this.collapsingToolbarLayout.setTitle(title);
        }
    }

    @Override
    public void setDescription(@Nullable final CharSequence description) {
        if (TextUtils.isEmpty(description)) {
            this.description.setVisibility(View.GONE);
        } else {
            this.description.setVisibility(View.VISIBLE);
            this.description.setText(description);
        }
    }

    @Override
    public void setLink(@Nullable final CharSequence link) {
        // TODO
    }

    @Override
    public void setIsBookmarked(final boolean isBookmarked) {
        if (isBookmarked) {
            this.bookmarkAction.setImageResource(R.drawable.ic_bookmark_black_24dp);
            this.bookmarkAction.setImageTintList(ColorStateList.valueOf(ContextUtils.getColor(this.getContext(), R.attr.primaryColor)));
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
            this.bookmarkAction.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
            this.bookmarkAction.setImageTintList(ColorStateList.valueOf(ContextUtils.getColor(this.getContext(), R.attr.textColorHint)));
        }
    }

    @Override
    public void setImages(@NonNull final List<Image> images) {
        this.imageContainer.removeAllViews();
        this.imagesContainer.removeAllViews();

        if (!images.isEmpty()) {
            this.subscribeImage(this.toolbarImage, images.get(0));

            if (TextUtils.isEmpty(images.get(0).getDescription())) {
                this.toolbarBackground.setVisibility(View.GONE);
            } else {
                this.toolbarTitle.setText(images.get(0).getDescription());
                this.toolbarBackground.setVisibility(View.VISIBLE);
            }

            this.imageContainer.addView(this.toolbarView);

            if (images.size() > 1) {
                for (final Image image : images.subList(1, images.size() - 1)) {
                    final View         view        = LayoutInflater.from(this.getContext()).inflate(R.layout.widget_image, this.imagesContainer, false);
                    final BigImageView imageView   = view.findViewById(R.id.image);
                    final TextView     description = view.findViewById(R.id.description);

                    this.subscribeImage(imageView, image);

                    if (!TextUtils.isEmpty(image.getDescription())) description.setText(image.getDescription());

                    this.imagesContainer.addView(view);
                }
            }
        }
    }

    @Override
    public void setVideo(@Nullable final Video video) {
        this.videoContainer.removeAllViews();

        // TODO
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

    @Override
    public boolean goBack() {
        this.backNavigations.onNext(Irrelevant.INSTANCE);

        return false;
    }

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

    @NonNull
    @Override
    public Flowable<Irrelevant> videoClick() {
        return this.videoClicks;
    }

    //endregion

    @CallSuper
    @Override
    protected void onAttachedToWindow() {
        final Activity activity = this.getActivity();
        this.smallBang = activity == null ? null : SmallBang.attach2Window(activity);

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
        super.onDetachedFromWindow();

        this.smallBang = null;
    }

    private void subscribeImage(@NonNull final BigImageView imageView, @NonNull final Image image) {
        imageView.getSSIV().setMaxScale(Constants.IMAGE_ZOOM_MAX);
        imageView.getSSIV().setPanEnabled(false);
        imageView.getSSIV().setZoomEnabled(false);

        imageView.showImage(Uri.parse(image.getUrl()));

        imageView.setOnClickListener(view -> this.imageClicks.onNext(image));
        this.manageDisposable(RxView.clicks((View)imageView.getParent()).subscribe(irrelevant -> this.imageClicks.onNext(image)));
    }
}
