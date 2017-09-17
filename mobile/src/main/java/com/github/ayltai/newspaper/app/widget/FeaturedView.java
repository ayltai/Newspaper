package com.github.ayltai.newspaper.app.widget;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSources;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.data.model.Image;
import com.github.ayltai.newspaper.util.Optional;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.Single;

public final class FeaturedView extends ItemView {
    public static final int VIEW_TYPE = R.id.view_type_featured;

    //region Components

    private final KenBurnsView image;
    private final TextView     title;

    //endregion

    public FeaturedView(@NonNull final Context context) {
        super(context);

        final View view = LayoutInflater.from(context).inflate(R.layout.view_news_featured, this, true);

        this.container = view.findViewById(R.id.container);
        this.image     = view.findViewById(R.id.featured_image);
        this.title     = view.findViewById(R.id.title);
    }

    //region Properties

    @Override
    public void setTitle(@Nullable final CharSequence title) {
        if (TextUtils.isEmpty(title)) {
            this.title.setVisibility(View.GONE);
        } else {
            this.title.setVisibility(View.VISIBLE);
            this.title.setText(title);
        }
    }

    @SuppressWarnings("IllegalCatch")
    @Override
    public void setImages(@NonNull final List<Image> images) {
        if (images.isEmpty()) {
            this.image.setVisibility(View.GONE);
        } else {
            if (TestUtils.isLoggable()) Log.d(this.getClass().getSimpleName(), "Featured image = " + images.get(0).getUrl());

            final DataSource<CloseableReference<CloseableImage>> source = Fresco.getImagePipeline().fetchDecodedImage(ImageRequest.fromUri(images.get(0).getUrl()), false);

            Single.<CloseableReference<CloseableImage>>create(
                emitter -> {
                    try {
                        emitter.onSuccess(DataSources.waitForFinalResult(source));
                    } catch (final Throwable error) {
                        emitter.onError(error);
                    } finally {
                        source.close();
                    }
                })
                .compose(RxUtils.applySingleBackgroundSchedulers())
                .map(reference -> {
                    if (reference.isValid()) {
                        final CloseableImage image = reference.get();

                        // TODO: Checks image size to avoid out-of-memory error

                        if (image instanceof CloseableBitmap) return Optional.of(((CloseableBitmap)image).getUnderlyingBitmap());
                    }

                    return Optional.<Bitmap>empty();
                })
                .compose(RxUtils.applySingleBackgroundToMainSchedulers())
                .subscribe(
                    bitmap -> {
                        if (bitmap.isPresent()) this.image.setImageBitmap(bitmap.get());
                    },
                    error -> {
                        if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
                    }
                );

            this.image.setVisibility(View.VISIBLE);
        }
    }

    //endregion
}
