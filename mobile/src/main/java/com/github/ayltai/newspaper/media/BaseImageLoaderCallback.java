package com.github.ayltai.newspaper.media;

import java.io.File;

import android.util.Log;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.piasy.biv.loader.ImageLoader;

public class BaseImageLoaderCallback implements ImageLoader.Callback {
    @Override
    public void onCacheHit(final File image) {
    }

    @Override
    public void onCacheMiss(final File image) {
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

    @Override
    public void onSuccess(final File image) {
    }

    @Override
    public void onFail(final Exception error) {
        if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
    }
}
