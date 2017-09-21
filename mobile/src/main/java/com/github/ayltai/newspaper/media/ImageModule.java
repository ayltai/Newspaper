package com.github.ayltai.newspaper.media;

import android.content.Context;
import android.support.annotation.NonNull;

import com.github.piasy.biv.loader.ImageLoader;

import dagger.Module;
import dagger.Provides;

@Module
public final class ImageModule {
    private final Context context;

    public ImageModule(@NonNull final Context context) {
        this.context = context;
    }

    @Provides
    public ImageLoader provideImageLoader() {
        return FrescoImageLoader.getInstance(this.context);
    }
}
