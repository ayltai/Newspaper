package com.github.ayltai.newspaper.util;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.Log;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.ayltai.newspaper.media.DaggerImageComponent;
import com.github.ayltai.newspaper.media.ImageModule;
import com.github.piasy.biv.view.BigImageView;

import io.reactivex.Single;

public final class ImageUtils {
    private ImageUtils() {
    }

    public static void translateToFacesCenter(@NonNull final BigImageView image) {
        image.getSSIV().setOnImageEventListener(new SubsamplingScaleImageView.OnImageEventListener() {
            @Override
            public void onReady() {
                Single.<PointF>create(emitter -> emitter.onSuccess(DaggerImageComponent.builder()
                    .imageModule(new ImageModule(image.getContext()))
                    .build()
                    .faceCenterFinder()
                    .findFaceCenter(image.getCurrentImageFile())))
                    .compose(RxUtils.applySingleBackgroundToMainSchedulers())
                    .subscribe(
                        center -> image.getSSIV().setScaleAndCenter(image.getSSIV().getScale(), new PointF(center.x, center.y)),
                        error -> {
                            if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
                        }
                    );
            }

            @Override
            public void onImageLoaded() {
            }

            @Override
            public void onPreviewLoadError(final Exception e) {
                if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
            }

            @Override
            public void onImageLoadError(final Exception e) {
                if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
            }

            @Override
            public void onTileLoadError(final Exception e) {
                if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
            }

            @Override
            public void onPreviewReleased() {
            }
        });
    }
}
