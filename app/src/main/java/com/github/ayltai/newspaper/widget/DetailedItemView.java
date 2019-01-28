package com.github.ayltai.newspaper.widget;

import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Parcelable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.facebook.drawee.view.SimpleDraweeView;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.data.model.Image;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.data.model.Video;
import com.github.ayltai.newspaper.util.DateUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.view.DetailedItemPresenter;
import com.github.ayltai.newspaper.view.Locatable;
import com.github.ayltai.newspaper.view.ModelKey;
import com.github.piasy.biv.view.BigImageView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.auto.value.AutoValue;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import xyz.hanks.library.bang.SmallBangView;

public final class DetailedItemView extends ItemView implements DetailedItemPresenter.View {
    @AutoValue
    public abstract static class Key extends ModelKey<Item> implements Locatable, Parcelable {
        @Nonnull
        @NonNull
        public abstract Item getModel();

        @Nonnull
        @NonNull
        @Override
        public abstract Point getLocation();

        @Nonnull
        @NonNull
        public static DetailedItemView.Key create(@Nonnull @NonNull @lombok.NonNull final Item model, @Nullable final Point location) {
            return new AutoValue_DetailedItemView_Key(model, location);
        }
    }

    //region Subscriptions

    private final FlowableProcessor<Irrelevant> textToSpeechClicks = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> viewOnWebClicks    = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> shareClicks        = PublishProcessor.create();

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
    private final SimpleDraweeView        sourceIcon;
    private final TextView                sourceName;
    private final TextView                publishDate;
    private final TextView                title;
    private final TextView                description;
    private final ImageView               textToSpeechButton;
    private final ImageView               bookmarkButton;
    private final ImageView               viewOnWebButton;
    private final ImageView               shareButton;
    private final ViewGroup               imagesContainer;
    private final ViewGroup               videoContainer;

    private VideoView videoView;

    //endregion

    private SmallBangView      smallBang;
    private boolean            isTtsActive;
    //private SimpleTextToSpeech tts;

    public DetailedItemView(@Nonnull @NonNull @lombok.NonNull final Context context) {
        super(context);

        final View view = LayoutInflater.from(context).inflate(R.layout.view_details, this, true);

        this.appBarLayout            = view.findViewById(R.id.appBarLayout);
        this.collapsingToolbarLayout = view.findViewById(R.id.collapsingToolbarLayout);
        this.toolbar                 = view.findViewById(R.id.toolbar);
        this.imageContainer          = view.findViewById(R.id.image_container);
        this.scrollView              = view.findViewById(R.id.scrollView);
        this.sourceIcon              = view.findViewById(R.id.avatar);
        this.sourceName              = view.findViewById(R.id.source);
        this.publishDate             = view.findViewById(R.id.publish_date);
        this.title                   = view.findViewById(R.id.title);
        this.description             = view.findViewById(R.id.description);
        this.textToSpeechButton      = view.findViewById(R.id.action_text_to_speech);
        this.smallBang               = view.findViewById(R.id.smallBang);
        this.bookmarkButton          = view.findViewById(R.id.action_bookmark);
        this.viewOnWebButton         = view.findViewById(R.id.action_view_on_web);
        this.shareButton             = view.findViewById(R.id.action_share);
        this.imagesContainer         = view.findViewById(R.id.images_container);
        this.videoContainer          = view.findViewById(R.id.video_container);

        this.toolbarView       = LayoutInflater.from(this.getContext()).inflate(R.layout.widget_toolbar, this.imageContainer, false);
        this.toolbarImage      = this.toolbarView.findViewById(R.id.image);
        this.toolbarTitle      = this.toolbarView.findViewById(R.id.title);
        this.toolbarBackground = this.toolbarView.findViewById(R.id.title_background);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) ((CollapsingToolbarLayout)view.findViewById(R.id.collapsingToolbarLayout)).setExpandedTitleTextAppearance(R.style.TransparentText);

        this.updateLayout(BaseView.LAYOUT_SCREEN);
    }

    //region Properties

    @Override
    public void setIcon(@Nonnull @NonNull @lombok.NonNull final String iconUrl) {
        this.sourceIcon.setImageURI(Constants.BASE_URL + iconUrl.substring(1));
    }

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
    public void setSource(@Nullable final CharSequence source) {
        if (TextUtils.isEmpty(source)) {
            this.sourceName.setVisibility(View.GONE);
        } else {
            this.sourceName.setVisibility(View.VISIBLE);
            this.sourceName.setText(source);
        }
    }

    @Override
    public void setPublishDate(@Nullable final Date date) {
        if (date == null) {
            this.publishDate.setVisibility(View.GONE);
        } else {
            this.publishDate.setVisibility(View.VISIBLE);
            this.publishDate.setText(DateUtils.getHumanReadableDate(this.getContext(), date));
        }
    }

    @Override
    public void setImages(@Nonnull @NonNull @lombok.NonNull final List<Image> images) {
        // TODO
    }

    @Override
    public void setVideos(@Nullable final List<Video> video) {
        // TODO
    }

    @Override
    public void setIsBookmarked(final boolean isBookmarked) {
        // TODO
    }

    //endregion

    //region Events

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> iconClicks() {
        return this.iconClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> sourceClicks() {
        return this.sourceClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> publishDateClicks() {
        return this.publishDateClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> titleClicks() {
        return this.titleClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> descriptionClicks() {
        return this.descriptionClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> linkClicks() {
        return this.linkClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Integer> imageClicks() {
        return this.imageClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Integer> videoClicks() {
        return this.videoClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> bookmarkClicks() {
        return this.bookmarkClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> textToSpeechClicks() {
        return this.textToSpeechClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> viewOnWebClicks() {
        return this.viewOnWebClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> shareClicks() {
        return this.shareClicks;
    }

    //endregion

    @Override
    public void textToSpeech() {
        // TODO
    }

    @Override
    public void viewOnWeb(@NonNull @Nonnull @lombok.NonNull final String url) {
        // TODO
    }

    @Override
    public void share(@NonNull @Nonnull @lombok.NonNull final String url) {
        // TODO
    }

    @Override
    public void showImage(@NonNull @Nonnull @lombok.NonNull final String url) {
        if (this.toolbarImage.getSSIV() != null) {
            this.toolbarImage.getSSIV().setImage(ImageSource.resource(R.drawable.thumbnail_placeholder));
        }
    }

    @CallSuper
    @Override
    public void onAttachedToWindow() {
        this.sourceIcon.setOnClickListener(view -> this.iconClicks.onNext(Irrelevant.INSTANCE));
        this.sourceName.setOnClickListener(view -> this.sourceClicks.onNext(Irrelevant.INSTANCE));
        this.textToSpeechButton.setOnClickListener(view -> this.textToSpeechClicks.onNext(Irrelevant.INSTANCE));
        this.bookmarkButton.setOnClickListener(view -> this.bookmarkClicks.onNext(Irrelevant.INSTANCE));
        this.viewOnWebButton.setOnClickListener(view -> this.viewOnWebClicks.onNext(Irrelevant.INSTANCE));
        this.shareButton.setOnClickListener(view -> this.shareClicks.onNext(Irrelevant.INSTANCE));

        super.onAttachedToWindow();

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

        super.onDetachedFromWindow();
    }
}
