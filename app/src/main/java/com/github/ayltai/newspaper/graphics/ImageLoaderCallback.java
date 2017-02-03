package com.github.ayltai.newspaper.graphics;

import java.io.File;

import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.util.ImageUtils;
import com.github.piasy.biv.loader.ImageLoader;

public final class ImageLoaderCallback implements ImageLoader.Callback {
    private final ImageView imageView;

    public ImageLoaderCallback(@NonNull final ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    public void onCacheHit(final File image) {
        this.imageView.post(() -> this.imageView.setImageBitmap(BitmapFactory.decodeFile(image.getAbsolutePath(), ImageUtils.createOptions(image, Constants.MAX_IMAGE_WIDTH, Constants.MAX_IMAGE_HEIGHT))));
    }

    @SuppressWarnings("WrongThread")
    @Override
    public void onCacheMiss(final File image) {
        this.onCacheHit(image);
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
