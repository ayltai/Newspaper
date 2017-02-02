package com.github.ayltai.newspaper.graphics;

import android.content.Context;
import android.support.annotation.NonNull;

import com.github.piasy.biv.loader.ImageLoader;
import com.github.piasy.biv.loader.fresco.FrescoImageLoader;

import dagger.Module;
import dagger.Provides;

@Module
public final class GraphicsModule {
    private final Context context;

    public GraphicsModule(@NonNull final Context context) {
        this.context = context;
    }

    @Provides
    public ImageLoader provideImageLoader() {
        return FrescoImageLoader.with(this.context);
    }
}
