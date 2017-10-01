package com.github.ayltai.newspaper.media;

import java.io.File;

import com.github.piasy.biv.loader.ImageLoader;

public abstract class BaseImageLoaderCallback implements ImageLoader.Callback {
    @Override
    public void onCacheHit(final File file) {
    }

    @Override
    public void onCacheMiss(final File file) {
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onProgress(final int progress) {
    }

    @Override
    public void onFinish() {
    }
}
