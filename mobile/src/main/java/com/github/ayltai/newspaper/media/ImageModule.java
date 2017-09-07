package com.github.ayltai.newspaper.media;

import javax.inject.Singleton;

import com.github.piasy.biv.loader.ImageLoader;

import dagger.Module;
import dagger.Provides;

@Module
public final class ImageModule {
    @Singleton
    @Provides
    static ImageLoader provideImageLoader() {
        return FrescoImageLoader.getInstance();
    }
}
