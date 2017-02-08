package com.github.ayltai.newspaper.graphics;

import java.io.File;

import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.util.ImageUtils;
import com.github.piasy.biv.loader.ImageLoader;

public final class ImageLoaderCallback implements ImageLoader.Callback {
    private static Handler HANDLER = new Handler(Looper.getMainLooper());

    private final ImageView imageView;

    public ImageLoaderCallback(@NonNull final ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    public void onCacheHit(final File image) {
        ImageLoaderCallback.HANDLER.post(new ImageLoaderCallback.CallbackRunnable(this.imageView, image));
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

    private static final class CallbackRunnable implements Runnable {
        private final ImageView imageView;
        private final File      image;

        CallbackRunnable(@NonNull final ImageView imageView, @NonNull final File image) {
            this.imageView = imageView;
            this.image     = image;
        }

        @Override
        public void run() {
            this.imageView.setImageBitmap(BitmapFactory.decodeFile(this.image.getAbsolutePath(), ImageUtils.createOptions(this.image, Constants.MAX_IMAGE_WIDTH, Constants.MAX_IMAGE_HEIGHT)));
        }
    }
}
