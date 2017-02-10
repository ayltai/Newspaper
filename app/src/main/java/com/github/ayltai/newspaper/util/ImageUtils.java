package com.github.ayltai.newspaper.util;

import java.io.File;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.DefaultExecutorSupplier;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.net.BaseHttpClient;

import okhttp3.OkHttpClient;

public final class ImageUtils {
    private ImageUtils() {
    }

    public static void configure(@NonNull final SubsamplingScaleImageView imageView) {
        imageView.setPanEnabled(false);
        imageView.setZoomEnabled(false);
        imageView.setQuickScaleEnabled(false);
        imageView.setMaxScale(Constants.MAX_IMAGE_SCALE);
        imageView.setParallelLoadingEnabled(true);
    }

    @NonNull
    public static BitmapFactory.Options createOptions(@NonNull final File file, final int maxWidth, final int maxHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        options.inSampleSize = 1;
        while (options.outWidth / options.inSampleSize > maxWidth || options.outHeight / options.inSampleSize > maxHeight) options.inSampleSize *= 2;

        options.inJustDecodeBounds = false;

        return options;
    }

    public static void initFresco(@NonNull final Context context) {
        if (!Fresco.hasBeenInitialized()) Fresco.initialize(context, OkHttpImagePipelineConfigFactory.newBuilder(context, new OkHttpClient.Builder()
            .connectTimeout(BaseHttpClient.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(BaseHttpClient.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(BaseHttpClient.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build())
            .setDownsampleEnabled(true)
            .setExecutorSupplier(new DefaultExecutorSupplier(Runtime.getRuntime().availableProcessors()))
            .build());
    }
}
