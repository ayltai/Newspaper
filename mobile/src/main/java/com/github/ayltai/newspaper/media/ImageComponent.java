package com.github.ayltai.newspaper.media;

import com.github.piasy.biv.loader.ImageLoader;

import dagger.Component;

@Component(modules = { ImageModule.class })
public interface ImageComponent {
    ImageLoader imageLoader();
}
