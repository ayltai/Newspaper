package com.github.ayltai.newspaper.media;

import javax.inject.Singleton;

import android.support.annotation.NonNull;

import com.github.piasy.biv.loader.ImageLoader;

import dagger.Component;

@Singleton
@Component(modules = { ImageModule.class })
public interface ImageComponent {
    @NonNull
    ImageLoader imageLoader();

    @NonNull
    FaceCenterFinder faceCenterFinder();
}
