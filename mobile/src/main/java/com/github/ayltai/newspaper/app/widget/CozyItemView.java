package com.github.ayltai.newspaper.app.widget;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.util.DateUtils;
import com.github.ayltai.newspaper.util.ImageUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.piasy.biv.view.BigImageView;
import com.jakewharton.rxbinding2.view.RxView;

public final class CozyItemView extends ItemView {
    public static final int VIEW_TYPE = R.id.view_type_cozy;

    //region Components

    private final SimpleDraweeView avatar;
    private final TextView         source;
    private final TextView         publishDate;
    private final BigImageView     image;
    private final TextView         title;
    private final TextView         description;

    //endregion

    public CozyItemView(@NonNull final Context context) {
        super(context);

        final View view = LayoutInflater.from(context).inflate(R.layout.view_news_cozy, this, true);

        this.container   = view.findViewById(R.id.container);
        this.avatar      = view.findViewById(R.id.avatar);
        this.source      = view.findViewById(R.id.source);
        this.publishDate = view.findViewById(R.id.publish_date);
        this.image       = view.findViewById(R.id.image);
        this.title       = view.findViewById(R.id.title);
        this.description = view.findViewById(R.id.description);

        this.image.getSSIV().setMaxScale(Constants.IMAGE_ZOOM_MAX);
        this.image.getSSIV().setPanEnabled(false);
        this.image.getSSIV().setZoomEnabled(false);
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

    @VisibleForTesting
    protected CharSequence getSource() {
        return this.source.getText();
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

    @VisibleForTesting
    public String getPublishDate() {
        return this.publishDate.getText().toString();
    }

    @Override
    public void setImages(@NonNull final List<Image> images) {
        if (images.isEmpty()) {
            this.image.setVisibility(View.GONE);
        } else {
            ImageUtils.translateToFacesCenter(this.image);

            this.image.setVisibility(View.VISIBLE);
            this.image.showImage(Uri.parse(images.get(0).getUrl()));
        }
    }

    @VisibleForTesting
    protected int getImageVisibility() {
        return this.image.getVisibility();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setTitle(@Nullable final CharSequence title) {
        if (TextUtils.isEmpty(title)) {
            this.title.setVisibility(View.GONE);
        } else {
            this.title.setVisibility(View.VISIBLE);
            this.title.setText(Html.fromHtml(title.toString()));
        }
    }

    @VisibleForTesting
    protected CharSequence getTitle() {
        return this.title.getText();
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

    @VisibleForTesting
    protected CharSequence getDescription() {
        return this.description.getText();
    }

    @Override
    public void setIsRead(final boolean isRead) {
        this.title.setAlpha(isRead ? Constants.ALPHA_READ : 1);
        this.description.setAlpha(isRead ? Constants.ALPHA_READ : 1);
        this.avatar.setAlpha(isRead ? Constants.ALPHA_READ : 1);
        this.source.setAlpha(isRead ? Constants.ALPHA_READ : 1);
        this.publishDate.setAlpha(isRead ? Constants.ALPHA_READ : 1);
        this.image.setAlpha(isRead ? Constants.ALPHA_READ : 1);
    }

    //endregion

    @CallSuper
    @Override
    public void onAttachedToWindow() {
        this.manageDisposable(RxView.clicks(this.image).subscribe(irrelevant -> this.clicks.onNext(Irrelevant.INSTANCE)));

        super.onAttachedToWindow();
    }
}
