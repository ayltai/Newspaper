package com.github.ayltai.newspaper.graphics;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.util.ImageUtils;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.piasy.biv.loader.ImageLoader;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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

        @SuppressWarnings("checkstyle:illegalcatch")
        @Override
        public void run() {
            Single.<Bitmap>create(emitter -> emitter.onSuccess(BitmapFactory.decodeFile(this.image.getAbsolutePath(), ImageUtils.createOptions(this.image, Constants.MAX_IMAGE_WIDTH, Constants.MAX_IMAGE_HEIGHT))))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                    bitmap -> {
                        try {
                            this.imageView.setImageBitmap(bitmap);
                        } catch (final Exception e) {
                            LogUtils.getInstance().w(this.getClass().getSimpleName(), e.getMessage(), e);
                        }
                    },
                    error -> LogUtils.getInstance().w(this.getClass().getSimpleName(), error.getMessage(), error));
        }
    }
}
