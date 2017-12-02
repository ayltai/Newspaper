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
        image.getSSIV().setOnImageEventListener(new SubsamplingScaleImageView.DefaultOnImageEventListener() {
            @Override
            public void onReady() {
                Single.<PointF>create(emitter -> emitter.onSuccess(DaggerImageComponent.builder()
                    .imageModule(new ImageModule(image.getContext()))
                    .build()
                    .faceCenterFinder()
                    .findFaceCenter(image.getCurrentImageFile())))
                    .compose(RxUtils.applySingleBackgroundToMainSchedulers())
                    .subscribe(
                        center -> image.getSSIV().setScaleAndCenter(image.getSSIV().getScale(), center),
                        error -> {
                            if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
                        }
                    );
            }

            @Override
            public void onPreviewLoadError(@NonNull final Exception e) {
                if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
            }

            @Override
            public void onImageLoadError(@NonNull final Exception e) {
                if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
            }

            @Override
            public void onTileLoadError(@NonNull final Exception e) {
                if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
            }
        });
    }
}
