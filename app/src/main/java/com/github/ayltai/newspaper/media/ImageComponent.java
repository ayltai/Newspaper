package com.github.ayltai.newspaper.media;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import androidx.annotation.NonNull;

import com.github.piasy.biv.loader.ImageLoader;

import dagger.Component;

@Singleton
@Component(modules = { ImageModule.class })
public interface ImageComponent {
    @Nonnull
    @NonNull
    ImageLoader imageLoader();
}
