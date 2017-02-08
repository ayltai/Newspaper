package com.github.ayltai.newspaper.graphics;

import com.github.piasy.biv.loader.ImageLoader;

import dagger.Module;
import dagger.Provides;

@Module
public final class GraphicsModule {
    @Provides
    public ImageLoader provideImageLoader() {
        return FrescoImageLoader.getInstance();
    }
}
