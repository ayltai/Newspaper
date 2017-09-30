package com.github.ayltai.newspaper.app.widget;

import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.piasy.biv.view.BigImageView;
import com.jakewharton.rxbinding2.view.RxView;

public final class ImageView extends ItemView {
    public static final int VIEW_TYPE = R.id.view_type_item_image;

    private final BigImageView image;

    public ImageView(@NonNull final Context context) {
        super(context);

        final View view = LayoutInflater.from(context).inflate(R.layout.view_news_cozy_image, this, true);

        this.container = view.findViewById(R.id.container);
        this.image     = view.findViewById(R.id.image);

        this.image.getSSIV().setMaxScale(Constants.IMAGE_ZOOM_MAX);
        this.image.getSSIV().setPanEnabled(false);
        this.image.getSSIV().setZoomEnabled(false);
    }

    @Override
    public void setImages(@NonNull final List<Image> images) {
        if (images.isEmpty()) {
            this.image.setVisibility(View.GONE);
        } else {
            this.image.setVisibility(View.VISIBLE);
            this.image.showImage(Uri.parse(images.get(0).getUrl()));
        }
    }

    @CallSuper
    @Override
    protected void onAttachedToWindow() {
        this.manageDisposable(RxView.clicks(this.image).subscribe(irrelevant -> this.clicks.onNext(Irrelevant.INSTANCE)));

        super.onAttachedToWindow();
    }
}
