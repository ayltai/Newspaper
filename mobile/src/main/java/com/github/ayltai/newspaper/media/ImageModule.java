package com.github.ayltai.newspaper.media;

import javax.inject.Singleton;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.vision.face.FaceDetector;

import com.github.ayltai.newspaper.Constants;
import com.github.piasy.biv.loader.ImageLoader;

import dagger.Module;
import dagger.Provides;

@Module
public final class ImageModule {
    private final Context context;

    public ImageModule(@NonNull final Context context) {
        this.context = context;
    }

    @Singleton
    @Provides
    ImageLoader provideImageLoader() {
        return FrescoImageLoader.getInstance(this.context);
    }

    @Singleton
    @Provides
    FaceCenterFinder provideFaceCenterFinder() {
        return new FaceCenterFinder(new FaceDetector.Builder(this.context)
            .setMinFaceSize(Constants.FACE_DETECTION_RATIO_MIN)
            .setTrackingEnabled(false)
            .build());
    }
}
