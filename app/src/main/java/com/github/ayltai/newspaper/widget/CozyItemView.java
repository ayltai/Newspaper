package com.github.ayltai.newspaper.widget;

import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.drawee.view.SimpleDraweeView;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.data.model.Image;
import com.github.ayltai.newspaper.util.DateUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.piasy.biv.view.BigImageView;
import com.jakewharton.rxbinding2.view.RxView;

public final class CozyItemView extends ItemView {
    //region Variables

    private final View             container;
    private final SimpleDraweeView icon;
    private final TextView         title;
    private final TextView         description;
    private final TextView         source;
    private final TextView         publishDate;
    private final BigImageView     image;

    //endregion

    public CozyItemView(@Nonnull @NonNull @lombok.NonNull final Context context) {
        super(context);

        final View view = LayoutInflater.from(context).inflate(R.layout.view_item_cozy, this, false);

        this.container   = view.findViewById(R.id.container);
        this.icon        = view.findViewById(R.id.source_icon);
        this.title       = view.findViewById(R.id.title);
        this.description = view.findViewById(R.id.description);
        this.source      = view.findViewById(R.id.source_name);
        this.publishDate = view.findViewById(R.id.publish_date);
        this.image       = view.findViewById(R.id.image);

        this.addView(view);
    }

    @Override
    public void setIcon(@Nonnull @NonNull @lombok.NonNull final String iconUrl) {
        this.icon.setImageURI(iconUrl);
    }

    @Override
    public void setTitle(@Nullable final CharSequence title) {
        this.title.setText(title);
    }

    @Override
    public void setDescription(@Nullable final CharSequence description) {
        this.description.setText(description);
    }

    @Override
    public void setSource(@Nullable final CharSequence source) {
        this.source.setText(source);
    }

    @Override
    public void setPublishDate(@Nullable final Date date) {
        this.publishDate.setText(DateUtils.getHumanReadableDate(this.getContext(), date));
    }

    @Override
    public void setImages(@Nonnull @NonNull @lombok.NonNull final List<Image> images) {
        if (!images.isEmpty()) this.image.showImage(Uri.parse(images.get(0).getImageUrl()));
    }

    @Override
    public void setIsRead(final boolean isRead) {
        super.setIsRead(isRead);
    }

    @CallSuper
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        this.manageDisposable(RxView.clicks(this.icon).subscribe(view -> this.iconClicks.onNext(Irrelevant.INSTANCE)));
        this.manageDisposable(RxView.clicks(this.title).subscribe(view -> this.titleClicks.onNext(Irrelevant.INSTANCE)));
        this.manageDisposable(RxView.clicks(this.description).subscribe(view -> this.descriptionClicks.onNext(Irrelevant.INSTANCE)));
        this.manageDisposable(RxView.clicks(this.source).subscribe(view -> this.sourceClicks.onNext(Irrelevant.INSTANCE)));
        this.manageDisposable(RxView.clicks(this.publishDate).subscribe(view -> this.publishDateClicks.onNext(Irrelevant.INSTANCE)));
        this.manageDisposable(RxView.clicks(this.image).subscribe(view -> this.imageClicks.onNext(0)));
    }
}
